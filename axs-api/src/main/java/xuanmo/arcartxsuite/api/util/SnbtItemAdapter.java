package xuanmo.arcartxsuite.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * 跨版本 SNBT 物品适配器。
 * <p>
 * 1.20.4 及以下：直接调用 {@code Bukkit.getUnsafe().modifyItemStack()}，
 * 该方法用 {@code MojangsonParser.parseTag()} 解析纯 SNBT {@code {display:{...}}} 并应用到物品。
 * <p>
 * 1.20.5+：{@code modifyItemStack} 内部改用 {@code ItemParser.parse()}，
 * 期望 {@code item_id[components]} 命令格式而非纯 SNBT，旧格式 {@code {display:{...}}} 会抛异常静默失败。
 * 本适配器在高版本上改用 {@code TagParser} 解析 SNBT → 构造旧格式物品 NBT →
 * DFU（DataFixerUpper）自动将旧 NBT 标签转换为 Data Components → 解析为新 ItemStack。
 * <p>
 * 反射调用 NMS/DFU 类，编译时仅需 spigot-api 1.20.1，运行时按服务端版本自动选择路径。
 * 遵循 {@link InventoryViewCompat} 的反射兼容模式。
 *
 * @since 1.3.0
 */
public final class SnbtItemAdapter {

    /** 1.20.1 的 DataVersion，作为 DFU 转换的源版本（旧 NBT 格式的最后稳定版本） */
    private static final int LEGACY_DATA_VERSION = 2586;

    /** 1.20.5 的 DataVersion，组件系统引入的版本 */
    private static final int COMPONENT_INTRO_VERSION = 2865;

    private SnbtItemAdapter() {
    }

    // ─── 公开 API ────────────────────────────────────────────────

    /**
     * 将 SNBT 字符串应用到物品上（替换物品的 NBT/组件），返回新物品。
     * <p>
     * 低版本：{@code modifyItemStack} 直接解析 SNBT 并应用。
     * <p>
     * 高版本：通过 DFU 将旧 NBT 格式自动转换为 Data Components，
     * 使 {@code display.Name} → {@code custom_name}、{@code Enchantments} → {@code enchantments} 等正确生效。
     * <p>
     * 失败时保留原物品（不抛异常），与原 {@code modifyItemStack} 的容错行为一致。
     *
     * @param item 目标物品，为 null 时直接返回 null
     * @param snbt SNBT 字符串（如 {@code "{display:{Name:'{\"text\":\"测试\"}'},Enchantments:[...]}" }）
     * @return 应用了 SNBT 的新物品，或原物品（失败时）
     */
    public static ItemStack applySnbt(ItemStack item, String snbt) {
        if (item == null) {
            return null;
        }
        if (snbt == null || snbt.isBlank()) {
            return item;
        }
        if (isModernVersion()) {
            try {
                ItemStack result = applySnbtModern(item, snbt);
                if (result != null) {
                    return result;
                }
            } catch (Throwable ignored) {
                // 高版本反射失败，回退到 legacy（会静默失败但不崩溃）
            }
        }
        // Legacy 路径（1.20.4 及以下，或高版本回退）
        try {
            ItemStack modified = Bukkit.getUnsafe().modifyItemStack(item.clone(), snbt);
            return modified != null ? modified : item;
        } catch (Exception ignored) {
            return item;
        }
    }

    // ─── 版本检测 ────────────────────────────────────────────────

    private static volatile Boolean modernCache;

    /** 判断服务端版本是否 >= 1.20.5（modifyItemStack 行为变更的版本） */
    private static boolean isModernVersion() {
        if (modernCache != null) {
            return modernCache;
        }
        synchronized (SnbtItemAdapter.class) {
            if (modernCache != null) {
                return modernCache;
            }
            modernCache = detectModernVersion();
            return modernCache;
        }
    }

    private static boolean detectModernVersion() {
        String version;
        try {
            version = Bukkit.getBukkitVersion();
        } catch (Throwable ignored) {
            return false;
        }
        if (version == null || version.isBlank()) {
            return false;
        }
        // version 形如 "1.20.1-R0.1-SNAPSHOT" 或 "1.21.1-R0.1-SNAPSHOT"
        String clean = version.split("-")[0].trim();
        String[] parts = clean.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
            // 1.20.5+ 或 1.21+
            return (major == 1 && minor >= 21) || (major == 1 && minor == 20 && patch >= 5);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    // ─── 高版本实现（反射） ──────────────────────────────────────

    /**
     * 1.20.5+ 的 SNBT 应用实现。
     * <p>
     * 步骤：
     * 1. TagParser 解析 SNBT 字符串 → CompoundTag（旧 NBT 标签内容）
     * 2. 构造完整旧格式物品 NBT：{id:"minecraft:xxx", Count:1b, tag:{旧NBT}, DataVersion:2586}
     * 3. DFU update() 将旧格式 NBT 转换为当前版本的 Data Components 格式
     * 4. 解析转换后的 NBT → NMS ItemStack
     * 5. 转换为 Bukkit ItemStack，保留原物品数量
     */
    private static ItemStack applySnbtModern(ItemStack item, String snbt) throws Throwable {
        // 1. 解析 SNBT → CompoundTag
        Object snbtTag = parseSnbt(snbt);
        if (snbtTag == null) {
            return null;
        }

        // 2. 构造旧格式物品 NBT
        String materialId = item.getType().getKey().toString(); // "minecraft:stone"
        Object oldNbt = constructOldItemNbt(materialId, snbtTag);

        // 3. DFU 转换
        Object convertedTag = dfuConvert(oldNbt);
        if (convertedTag == null) {
            return null;
        }

        // 4. 解析为 NMS ItemStack
        Object nmsItem = parseNmsItemStack(convertedTag);
        if (nmsItem == null || isNmsItemStackEmpty(nmsItem)) {
            return null;
        }

        // 5. 转换为 Bukkit ItemStack
        ItemStack result = asBukkitCopy(nmsItem);
        if (result == null || result.getType().isAir()) {
            return null;
        }

        // 6. 保留原物品数量
        result.setAmount(item.getAmount());
        return result;
    }

    // ─── 反射辅助方法 ────────────────────────────────────────────

    /** 获取 CraftBukkit 包路径（1.20.1 带 v1_20_R1 后缀，1.21+ 无后缀） */
    private static String craftPkg() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    /** 解析 SNBT 字符串为 CompoundTag，兼容 parseTag（1.20.5~1.21.4）和 parseCompoundFully（1.21.5+） */
    private static Object parseSnbt(String snbt) throws Throwable {
        Class<?> tagParserClass = Class.forName("net.minecraft.nbt.TagParser");
        // 优先尝试 parseTag（1.20.5 ~ 1.21.4）
        Object result = tryInvokeStatic(tagParserClass, "parseTag", snbt);
        if (result == null) {
            // 1.21.5+ 改名为 parseCompoundFully
            result = tryInvokeStatic(tagParserClass, "parseCompoundFully", snbt);
        }
        return result;
    }

    /** 构造旧格式物品 NBT：{id:"minecraft:xxx", Count:1b, tag:{snbtContent}, DataVersion:2586} */
    private static Object constructOldItemNbt(String materialId, Object snbtTag) throws Throwable {
        Class<?> compoundTagClass = Class.forName("net.minecraft.nbt.CompoundTag");
        Class<?> tagClass = Class.forName("net.minecraft.nbt.Tag");

        Object oldNbt = compoundTagClass.getDeclaredConstructor().newInstance();
        Method putString = compoundTagClass.getMethod("putString", String.class, String.class);
        Method putByte = compoundTagClass.getMethod("putByte", String.class, byte.class);
        Method putInt = compoundTagClass.getMethod("putInt", String.class, int.class);
        Method put = compoundTagClass.getMethod("put", String.class, tagClass);

        putString.invoke(oldNbt, "id", materialId);
        putByte.invoke(oldNbt, "Count", (byte) 1);
        put.invoke(oldNbt, "tag", snbtTag);
        putInt.invoke(oldNbt, "DataVersion", LEGACY_DATA_VERSION);
        return oldNbt;
    }

    /**
     * DFU 转换：将旧格式 NBT 转换为当前版本的 Data Components 格式。
     * <p>
     * 等效 NMS 代码：
     * <pre>{@code
     * Dynamic<Tag> input = new Dynamic<>(NbtOps.INSTANCE, oldNbt);
     * Dynamic<Tag> output = DataFixers.getDataFixer()
     *     .update(References.ITEM_STACK, input, LEGACY_DATA_VERSION, currentDataVersion);
     * CompoundTag converted = (CompoundTag) output.getValue();
     * }</pre>
     */
    private static Object dfuConvert(Object oldNbt) throws Throwable {
        // DataFixers.getDataFixer()
        Class<?> dataFixersClass = Class.forName("net.minecraft.util.datafix.DataFixers");
        Method getDataFixer = dataFixersClass.getMethod("getDataFixer");
        Object dataFixer = getDataFixer.invoke(null);

        // References.ITEM_STACK
        Class<?> referencesClass = Class.forName("net.minecraft.util.datafix.fixes.References");
        Field itemStackField = referencesClass.getField("ITEM_STACK");
        Object itemStackRef = itemStackField.get(null);

        // NbtOps.INSTANCE
        Class<?> nbtOpsClass = Class.forName("net.minecraft.nbt.NbtOps");
        Field nbtOpsInstance = nbtOpsClass.getField("INSTANCE");
        Object nbtOps = nbtOpsInstance.get(null);

        // new Dynamic(NbtOps.INSTANCE, oldNbt)
        Class<?> dynamicOpsClass = Class.forName("com.mojang.serialization.DynamicOps");
        Class<?> dynamicClass = Class.forName("com.mojang.serialization.Dynamic");
        Constructor<?> dynamicCtor = dynamicClass.getConstructor(dynamicOpsClass, Object.class);
        Object dynamic = dynamicCtor.newInstance(nbtOps, oldNbt);

        // 获取当前 DataVersion
        int currentVersion = getCurrentDataVersion();

        // dataFixer.update(References.ITEM_STACK, dynamic, LEGACY_DATA_VERSION, currentVersion)
        Class<?> typeReferenceClass = Class.forName("com.mojang.datafixers.TypeReference");
        Method update = dataFixer.getClass().getMethod("update", typeReferenceClass, dynamicClass, int.class, int.class);
        Object convertedDynamic = update.invoke(dataFixer, itemStackRef, dynamic, LEGACY_DATA_VERSION, currentVersion);

        // convertedDynamic.getValue() → CompoundTag
        Method getValue = dynamicClass.getMethod("getValue");
        return getValue.invoke(convertedDynamic);
    }

    /**
     * 解析转换后的 NBT 为 NMS ItemStack。
     * <p>
     * 兼容多版本的解析方法：
     * - 1.20.5 ~ 1.21.4：ItemStack.parseOptional(HolderLookup.Provider, CompoundTag) → ItemStack
     * - 1.21.5：ItemStack.parse(HolderLookup.Provider, Tag) → Optional&lt;ItemStack&gt;
     * - 1.21.8+：ItemStack.CODEC.parse(RegistryOps, Tag) → DataResult&lt;ItemStack&gt;
     */
    private static Object parseNmsItemStack(Object compoundTag) throws Throwable {
        Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
        String pkg = craftPkg();

        // 获取 registry
        Object registry = getMinecraftRegistry(pkg);

        // 尝试 1：ItemStack.parseOptional(HolderLookup.Provider, CompoundTag) → ItemStack
        // 适用于 1.20.5 ~ 1.21.4
        Object result = tryParseOptional(nmsItemStackClass, registry, compoundTag);
        if (result != null) {
            return result;
        }

        // 尝试 2：ItemStack.parse(HolderLookup.Provider, Tag) → Optional<ItemStack>
        // 适用于 1.21.5 ~ 1.21.7
        result = tryParseOptionalReturnType(nmsItemStackClass, registry, compoundTag);
        if (result != null) {
            return result;
        }

        // 尝试 3：ItemStack.CODEC.parse(RegistryOps, Tag) → DataResult<ItemStack>
        // 适用于 1.21.8+
        result = tryCodecParse(nmsItemStackClass, registry, compoundTag);
        return result;
    }

    /** 获取 MinecraftRegistry（HolderLookup.Provider） */
    private static Object getMinecraftRegistry(String pkg) throws Throwable {
        try {
            Class<?> craftRegistryClass = Class.forName(pkg + ".CraftRegistry");
            Method getMinecraftRegistry = craftRegistryClass.getMethod("getMinecraftRegistry");
            return getMinecraftRegistry.invoke(null);
        } catch (ClassNotFoundException ignored) {
            // 某些版本可能类名不同，尝试无包后缀
            Class<?> craftRegistryClass = Class.forName("org.bukkit.craftbukkit.CraftRegistry");
            Method getMinecraftRegistry = craftRegistryClass.getMethod("getMinecraftRegistry");
            return getMinecraftRegistry.invoke(null);
        }
    }

    /** 尝试 ItemStack.parseOptional(Provider, CompoundTag) */
    private static Object tryParseOptional(Class<?> itemStackClass, Object registry, Object tag) throws Throwable {
        try {
            Class<?> providerClass = Class.forName("net.minecraft.core.HolderLookup$Provider");
            Class<?> compoundTagClass = Class.forName("net.minecraft.nbt.CompoundTag");
            Method parseOptional = itemStackClass.getMethod("parseOptional", providerClass, compoundTagClass);
            return parseOptional.invoke(null, registry, tag);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /** 尝试 ItemStack.parse(Provider, Tag) → Optional<ItemStack> */
    private static Object tryParseOptionalReturnType(Class<?> itemStackClass, Object registry, Object tag) throws Throwable {
        try {
            Class<?> providerClass = Class.forName("net.minecraft.core.HolderLookup$Provider");
            Class<?> tagClass = Class.forName("net.minecraft.nbt.Tag");
            Method parse = itemStackClass.getMethod("parse", providerClass, tagClass);
            Object optional = parse.invoke(null, registry, tag);
            return unwrapOptional(optional);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /** 尝试 ItemStack.CODEC.parse(RegistryOps, Tag) → DataResult<ItemStack> */
    private static Object tryCodecParse(Class<?> itemStackClass, Object registry, Object tag) throws Throwable {
        try {
            // 获取 CODEC 字段
            Field codecField = itemStackClass.getField("CODEC");
            Object codec = codecField.get(null);

            // 构造 RegistryOps
            Class<?> nbtOpsClass = Class.forName("net.minecraft.nbt.NbtOps");
            Field nbtOpsInstance = nbtOpsClass.getField("INSTANCE");
            Object nbtOps = nbtOpsInstance.get(null);

            Class<?> registryOpsClass = Class.forName("net.minecraft.resources.RegistryOps");
            // RegistryOps.create(NbtOps.INSTANCE, registry)
            Method create = registryOpsClass.getMethod("create",
                Class.forName("com.mojang.serialization.DynamicOps"), Object.class);
            Object ops = create.invoke(null, nbtOps, registry);

            // codec.parse(ops, tag) → DataResult
            Method parse = codec.getClass().getMethod("parse",
                Class.forName("com.mojang.serialization.DynamicOps"), Object.class);
            Object dataResult = parse.invoke(codec, ops, tag);

            // DataResult.result() → Optional
            Method resultMethod = dataResult.getClass().getMethod("result");
            Object optional = resultMethod.invoke(dataResult);
            return unwrapOptional(optional);
        } catch (NoSuchMethodException | NoSuchFieldException ignored) {
            return null;
        }
    }

    /** 从 Optional 中解包值，空则返回 null */
    private static Object unwrapOptional(Object optional) throws Throwable {
        if (optional == null) {
            return null;
        }
        Method isEmpty = optional.getClass().getMethod("isEmpty");
        boolean empty = (boolean) isEmpty.invoke(optional);
        if (empty) {
            return null;
        }
        Method orElse = optional.getClass().getMethod("orElse", Object.class);
        return orElse.invoke(optional, (Object) null);
    }

    /** 获取当前服务端的 DataVersion */
    private static int getCurrentDataVersion() {
        try {
            return Bukkit.getUnsafe().getDataVersion();
        } catch (Throwable ignored) {
            // 回退：使用组件引入版本作为目标（最低可用版本）
            return COMPONENT_INTRO_VERSION;
        }
    }

    /** 检查 NMS ItemStack 是否为空（EMPTY） */
    private static boolean isNmsItemStackEmpty(Object nmsItem) throws Throwable {
        Class<?> itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
        Field emptyField = itemStackClass.getField("EMPTY");
        Object empty = emptyField.get(null);
        return nmsItem == empty;
    }

    /** NMS ItemStack → Bukkit ItemStack */
    private static ItemStack asBukkitCopy(Object nmsItem) throws Throwable {
        String pkg = craftPkg();
        Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
        try {
            Class<?> craftItemStackClass = Class.forName(pkg + ".inventory.CraftItemStack");
            Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
        } catch (ClassNotFoundException ignored) {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
        }
    }

    /** 尝试调用静态方法，方法不存在时返回 null（不抛异常） */
    private static Object tryInvokeStatic(Class<?> clazz, String methodName, Object... args) throws Throwable {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }
            Method method = clazz.getMethod(methodName, paramTypes);
            return method.invoke(null, args);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
