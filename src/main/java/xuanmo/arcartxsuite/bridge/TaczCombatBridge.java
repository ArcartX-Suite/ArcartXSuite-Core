package xuanmo.arcartxsuite.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xuanmo.arcartxsuite.api.event.TaczGunDamageEvent;

/**
 * TACZ（创世战术武器）兼容桥接，仅在混合核心（Mohist/Arclight 等）上启用。
 * <p>
 * 当 TACZ Mod 存在时，注册 Forge/NeoForge 事件监听器，将 {@code EntityHurtByGunEvent.Pre}
 * 转换为 {@link TaczGunDamageEvent} 并通过 Bukkit 事件总线广播。
 * AXS 模块可通过标准 {@code @EventHandler} 监听 {@link TaczGunDamageEvent} 获取枪械伤害信息，
 * 而无需关心 Forge 反射细节，也不会与 {@link org.bukkit.event.entity.EntityDamageByEntityEvent} 混淆。
 */
public final class TaczCombatBridge {

    private static final String TACZ_EVENT_CLASS = "com.tacz.guns.api.event.common.EntityHurtByGunEvent$Pre";
    private static final String FORGE_CLASS = "net.minecraftforge.common.MinecraftForge";
    private static final String FORGE_EVENT_BUS_INTERFACE = "net.minecraftforge.eventbus.api.IEventBus";
    private static final String FORGE_PRIORITY_CLASS = "net.minecraftforge.eventbus.api.EventPriority";
    private static final String NMS_ENTITY_CLASS = "net.minecraft.world.entity.Entity";

    private final JavaPlugin plugin;
    private final boolean debug;
    private final ClassLoader modClassLoader;
    private boolean active;

    // Forge 反射缓存
    private Method getHurtEntityMethod;
    private Method getAttackerMethod;
    private Method getBaseAmountMethod;
    private Method isHeadShotMethod;
    private Method getGunIdMethod;
    private Method getBukkitEntityMethod;

    private TaczCombatBridge(JavaPlugin plugin, boolean debug, ClassLoader modClassLoader) {
        this.plugin = plugin;
        this.debug = debug;
        this.modClassLoader = modClassLoader;
    }

    /**
     * 解析能看到 TACZ Mod / Forge / NMS 类的类加载器。
     * <p>这些类由服务端（混合核心）类加载器加载，不属于任何 Bukkit 插件加载器。
     * 薄壳重构后插件类由 ProtectedClassLoader 定义，单参 {@code Class.forName} 的
     * 调用者加载器够不到 Mod/NMS 类，因此必须显式使用服务端类加载器。
     */
    private static ClassLoader resolveModClassLoader() {
        return Bukkit.getServer().getClass().getClassLoader();
    }

    public static TaczCombatBridge tryInitialize(JavaPlugin plugin, boolean enabled, boolean debug) {
        if (!enabled) {
            xuanmo.arcartxsuite.module.AxsLog.logger().fine("[TaczCombat] TACZ 兼容已在 config.yml 中关闭。");
            return null;
        }
        ClassLoader modClassLoader = resolveModClassLoader();
        try {
            Class.forName("com.tacz.guns.GunMod", false, modClassLoader);
        } catch (ClassNotFoundException ignored) {
            if (debug) {
                xuanmo.arcartxsuite.module.AxsLog.logger().fine("[TaczCombat] TACZ Mod 未检测到，跳过桥接初始化。");
            }
            return null;
        }
        try {
            TaczCombatBridge bridge = new TaczCombatBridge(plugin, debug, modClassLoader);
            if (!bridge.registerForgeEventListener()) {
                xuanmo.arcartxsuite.module.AxsLog.logger().warning("[TaczCombat] 无法注册 Forge 事件监听器，TACZ 伤害桥接未启用。");
                return null;
            }
            bridge.active = true;
            xuanmo.arcartxsuite.module.AxsLog.logger().info("[TaczCombat] TACZ 伤害桥接已启用。");
            return bridge;
        } catch (Exception exception) {
            xuanmo.arcartxsuite.module.AxsLog.logger().log(Level.WARNING, "[TaczCombat] TACZ 桥接初始化失败。", exception);
            return null;
        }
    }

    public void shutdown() {
        if (active) {
            active = false;
            xuanmo.arcartxsuite.module.AxsLog.logger().info("[TaczCombat] TACZ 伤害桥接已关闭。");
        }
    }

    public boolean isActive() {
        return active;
    }

    // ---- Forge 事件注册 ----

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean registerForgeEventListener() {
        try {
            // 1. 加载 TACZ 事件类
            Class<?> eventPreClass = Class.forName(TACZ_EVENT_CLASS, true, modClassLoader);

            // 2. 缓存事件方法（尝试多种可能的方法名）
            getHurtEntityMethod = findMethodByNames(eventPreClass, "getHurtEntity", "getEntity", "getTarget");
            getAttackerMethod = findMethodByNames(eventPreClass, "getAttacker", "getShooter", "getPlayer");
            getBaseAmountMethod = findMethodByNames(eventPreClass, "getBaseAmount", "getAmount", "getDamage");
            isHeadShotMethod = findMethodByNames(eventPreClass, "isHeadShot", "isHeadshot");
            getGunIdMethod = findMethodByNames(eventPreClass, "getGunId", "getGunID");

            if (getHurtEntityMethod == null || getAttackerMethod == null || getBaseAmountMethod == null) {
                xuanmo.arcartxsuite.module.AxsLog.logger().warning("[TaczCombat] 无法找到 TACZ 事件所需方法: hurtEntity="
                    + (getHurtEntityMethod != null) + ", attacker=" + (getAttackerMethod != null)
                    + ", baseAmount=" + (getBaseAmountMethod != null));
                logAvailableMethods(eventPreClass);
                return false;
            }

            // 3. NMS Entity -> Bukkit Entity 转换
            Class<?> nmsEntityClass = Class.forName(NMS_ENTITY_CLASS, true, modClassLoader);
            getBukkitEntityMethod = nmsEntityClass.getMethod("getBukkitEntity");

            // 4. 获取 Forge 事件总线
            Class<?> forgeClass = Class.forName(FORGE_CLASS, true, modClassLoader);
            Field eventBusField = forgeClass.getDeclaredField("EVENT_BUS");
            Object eventBus = eventBusField.get(null);

            // 5. 获取 EventPriority.NORMAL
            Class<?> priorityClass = Class.forName(FORGE_PRIORITY_CLASS, true, modClassLoader);
            Object normalPriority = Enum.valueOf((Class<Enum>) priorityClass, "NORMAL");

            // 6. 注册监听器: addListener(EventPriority, boolean, Class<T>, Consumer<T>)
            Consumer<Object> listener = this::handleGunDamageEvent;
            Method addListenerMethod = findAddListenerMethod(eventBus, priorityClass);
            if (addListenerMethod == null) {
                xuanmo.arcartxsuite.module.AxsLog.logger().warning("[TaczCombat] 无法找到 IEventBus.addListener 方法。");
                return false;
            }

            addListenerMethod.invoke(eventBus, normalPriority, false, eventPreClass, listener);

            if (debug) {
                xuanmo.arcartxsuite.module.AxsLog.logger().info("[TaczCombat] Forge 事件监听器注册成功 -> " + TACZ_EVENT_CLASS);
            }
            return true;
        } catch (ClassNotFoundException e) {
            xuanmo.arcartxsuite.module.AxsLog.logger().warning("[TaczCombat] 未找到所需类: " + e.getMessage());
            return false;
        } catch (Exception e) {
            xuanmo.arcartxsuite.module.AxsLog.logger().log(Level.WARNING, "[TaczCombat] 注册 Forge 事件监听器失败", e);
            return false;
        }
    }

    // ---- Forge 事件回调 ----

    private void handleGunDamageEvent(Object event) {
        if (!active) {
            return;
        }
        try {
            // 1. 提取攻击者和目标（NMS Entity）
            Object nmsHurtEntity = getHurtEntityMethod.invoke(event);
            Object nmsAttacker = getAttackerMethod.invoke(event);
            Object rawAmount = getBaseAmountMethod.invoke(event);

            if (nmsHurtEntity == null || nmsAttacker == null || rawAmount == null) {
                return;
            }

            // 2. 获取伤害值
            float damage = rawAmount instanceof Number number ? number.floatValue() : 0.0F;
            if (damage <= 0.0F) {
                return;
            }

            // 3. NMS -> Bukkit Entity
            Entity bukkitTarget = (Entity) getBukkitEntityMethod.invoke(nmsHurtEntity);
            Entity bukkitAttacker = (Entity) getBukkitEntityMethod.invoke(nmsAttacker);

            if (!(bukkitAttacker instanceof Player player)) {
                return;
            }
            if (!(bukkitTarget instanceof LivingEntity livingTarget)) {
                return;
            }
            if (player.equals(livingTarget)) {
                return;
            }

            // 4. 提取额外信息
            boolean headShot = false;
            if (isHeadShotMethod != null) {
                try {
                    Object headShotResult = isHeadShotMethod.invoke(event);
                    if (headShotResult instanceof Boolean b) {
                        headShot = b;
                    }
                } catch (Exception ignored) {
                }
            }
            String gunId = "";
            if (getGunIdMethod != null) {
                try {
                    Object gunIdResult = getGunIdMethod.invoke(event);
                    if (gunIdResult != null) {
                        gunId = gunIdResult.toString();
                    }
                } catch (Exception ignored) {
                }
            }

            // 5. 在主线程广播为 TaczGunDamageEvent
            final double finalDamage = damage;
            final boolean finalHeadShot = headShot;
            final String finalGunId = gunId;
            Runnable fireEvent = () -> {
                if (!livingTarget.isDead() && livingTarget.isValid()) {
                    Bukkit.getPluginManager().callEvent(
                        new TaczGunDamageEvent(player, livingTarget, finalDamage, finalHeadShot, finalGunId)
                    );
                }
            };
            if (Bukkit.isPrimaryThread()) {
                fireEvent.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, fireEvent);
            }
        } catch (Exception e) {
            if (debug) {
                xuanmo.arcartxsuite.module.AxsLog.logger().log(Level.WARNING, "[TaczCombat] 处理 TACZ 伤害事件异常", e);
            }
        }
    }

    private void logTaczGunDamageEvent(Player attacker, LivingEntity target, double damage, boolean headShot) {
        if (debug) {
            xuanmo.arcartxsuite.module.AxsLog.logger().info("[TaczCombat] 广播 TACZ 伤害 -> attacker=" + attacker.getName()
                + ", target=" + target.getType() + "(" + target.getUniqueId() + ")"
                + ", damage=" + String.format("%.2f", damage)
                + ", headshot=" + headShot);
        }
    }

    // ---- 工具方法 ----

    private static Method findMethodByNames(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Method method = clazz.getMethod(name);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        // 尝试在父类中查找
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (String name : names) {
                try {
                    return superClass.getMethod(name);
                } catch (NoSuchMethodException ignored) {
                }
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    private static Method findAddListenerMethod(Object eventBus, Class<?> priorityClass) {
        for (Method method : eventBus.getClass().getMethods()) {
            if (!"addListener".equals(method.getName())) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 4
                && params[0] == priorityClass
                && params[1] == boolean.class
                && params[2] == Class.class
                && Consumer.class.isAssignableFrom(params[3])) {
                return method;
            }
        }
        return null;
    }

    private void logAvailableMethods(Class<?> eventClass) {
        if (!debug) {
            return;
        }
        StringBuilder sb = new StringBuilder("[TaczCombat] 可用方法列表 (").append(eventClass.getSimpleName()).append("):");
        Class<?> current = eventClass;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.getParameterCount() == 0) {
                    sb.append("\n  - ").append(current.getSimpleName()).append(".").append(m.getName())
                        .append("() -> ").append(m.getReturnType().getSimpleName());
                }
            }
            current = current.getSuperclass();
        }
        xuanmo.arcartxsuite.module.AxsLog.logger().info(sb.toString());
    }
}
