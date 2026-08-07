package xuanmo.arcartxsuite.api.combat;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.util.AttributeResolver;

/**
 * 实体战斗元数据解析工具，统一处理生物的显示名、类型、MythicMobs 标识与生命值解析。
 * <p>
 * MythicMobs 为 compileOnly 依赖，运行时可能不存在，因此相关方法均通过懒检测避免
 * {@link LinkageError} / {@link NoClassDefFoundError}。
 */
public final class EntityCombatMetadata {

    private EntityCombatMetadata() {}

    // ─── MythicMobs availability ─────────────────────────────
    // compileOnly 依赖：运行时 MythicMobs 可能不存在，需懒检测避免 NoClassDefFoundError

    private static volatile Boolean mythicAvailable;

    /**
     * 判断 MythicMobs 插件是否已安装且可用，结果会被缓存。
     *
     * @return {@code true} 表示 MythicMobs 存在
     */
    private static boolean mythicAvailable() {
        Boolean cached = mythicAvailable;
        if (cached != null) {
            return cached;
        }
        boolean present = Bukkit.getPluginManager().getPlugin("MythicBukkit") != null
            || Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
        mythicAvailable = present;
        return present;
    }

    // ─── Display name ────────────────────────────────────────

    /**
     * 解析实体的显示名称，优先级：玩家名 > MythicMobs id > 自定义名 > 实体类型名。
     *
     * @param entity      目标生物，可为 null
     * @param mythicMobId 显式指定的 MythicMobs id，可为 null
     * @return 去色后的显示名；实体为 null 时返回 "Unknown"
     */
    public static String resolveDisplayName(LivingEntity entity, String mythicMobId) {
        if (entity == null) {
            return "Unknown";
        }
        if (entity instanceof Player player) {
            return player.getName();
        }
        if (mythicMobId != null && !mythicMobId.isBlank()) {
            return mythicMobId;
        }
        String customName = entity.getCustomName();
        if (customName != null && !customName.isBlank()) {
            return ChatColor.stripColor(customName);
        }
        return entity.getType().name();
    }

    // ─── Entity type helpers ─────────────────────────────────

    /**
     * 获取实体的类型名（如 "ZOMBIE"），实体为 null 时返回空串。
     *
     * @param entity 目标生物
     * @return 实体类型名大写形式
     */
    public static String resolveEntityType(LivingEntity entity) {
        return entity == null ? "" : entity.getType().name();
    }

    /**
     * 将实体类型字符串标准化为大写并去除首尾空白。
     *
     * @param entityType 原始类型字符串，可为 null
     * @return 标准化后的类型名；null 返回空串
     */
    public static String normalizeEntityType(String entityType) {
        return entityType == null ? "" : entityType.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 将实体类型枚举格式化为人类可读名称（如 "ZOMBIE" → "Zombie"，"ENDER_DRAGON" → "Ender Dragon"）。
     *
     * @param type 实体类型，可为 null
     * @return 格式化后的名称；null 返回空串
     */
    public static String formatEntityTypeName(EntityType type) {
        if (type == null) return "";
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    // ─── MythicMobs integration (direct API) ────────────────

    /**
     * 标准化 MythicMobs id，去除首尾空白，null 返回空串。
     *
     * @param mythicMobId 原始 id
     * @return 标准化后的 id
     */
    public static String normalizeMythicMobId(String mythicMobId) {
        return mythicMobId == null ? "" : mythicMobId.trim();
    }

    /**
     * 解析实体对应的 MythicMobs id，MythicMobs 不可用时返回空串。
     *
     * @param entity 目标生物
     * @return MythicMobs 内部名；无法解析时返回空串
     */
    public static String resolveMythicMobId(LivingEntity entity) {
        if (!mythicAvailable()) return "";
        ActiveMob activeMob = resolveActiveMob(entity);
        return activeMob == null ? "" : nullToEmpty(resolveMythicMobIdFrom(activeMob));
    }

    /**
     * 通过 MythicMobs API 获取实体对应的 {@link ActiveMob} 实例。
 * <p>
 * MythicMobs 不可用或实体非 Mythic 生物时返回 null，所有异常被吞掉以避免影响调用方。
     *
     * @param entity 目标生物
     * @return ActiveMob 实例；不存在或出错时返回 null
     */
    public static ActiveMob resolveActiveMob(LivingEntity entity) {
        if (entity == null || !mythicAvailable()) return null;
        try {
            return MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);
        } catch (Exception | LinkageError ignored) {
            return null;
        }
    }

    /**
     * 从 {@link ActiveMob} 提取 MythicMobs 内部名。
     *
     * @param activeMob MythicMobs 活动生物实例，可为 null
     * @return 内部名；无法获取时返回空串
     */
    public static String resolveMythicMobIdFrom(ActiveMob activeMob) {
        if (activeMob == null) return "";
        try {
            if (activeMob.getType() != null && activeMob.getType().getInternalName() != null) {
                return activeMob.getType().getInternalName();
            }
        } catch (Exception | LinkageError ignored) {}
        return "";
    }

    // ─── Health resolution ───────────────────────────────────

    /**
     * 解析实体最大生命值，优先取 MythicMobs 的最大生命值，其次取 Bukkit 属性，兜底 20。
     *
     * @param entity    目标生物
     * @param activeMob 对应的 MythicMobs 实例，可为 null
     * @return 最大生命值，至少为 1.0
     */
    public static double resolveMaxHealth(LivingEntity entity, ActiveMob activeMob) {
        if (activeMob != null && activeMob.getEntity() != null) {
            try {
                return Math.max(activeMob.getEntity().getMaxHealth(), 1.0D);
            } catch (Exception | LinkageError ignored) {}
        }
        if (entity != null) {
            return AttributeResolver.getMaxHealth(entity);
        }
        return 20.0;
    }

    /**
     * 解析实体当前生命值，优先取 MythicMobs 当前血量，其次取 Bukkit 当前血量，并夹取到 [0, maxHealth]。
     *
     * @param entity    目标生物
     * @param maxHealth 最大生命值上限
     * @param activeMob 对应的 MythicMobs 实例，可为 null
     * @return 当前生命值，范围 [0, maxHealth]；实体为 null 时返回 0
     */
    public static double resolveCurrentHealth(LivingEntity entity, double maxHealth, ActiveMob activeMob) {
        if (activeMob != null && activeMob.getEntity() != null) {
            try {
                return Math.max(0.0D, Math.min(activeMob.getEntity().getHealth(), maxHealth));
            } catch (Exception | LinkageError ignored) {}
        }
        if (entity != null) {
            return Math.min(entity.getHealth(), maxHealth);
        }
        return 0.0;
    }

    // ─── Util ────────────────────────────────────────────────

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
