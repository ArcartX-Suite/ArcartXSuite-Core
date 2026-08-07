package xuanmo.arcartxsuite.api.item;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * 通过反射懒加载 NMS 类，跨版本兼容地读取物品原始 NBT 键。
 * <p>
 * 支持 1.12+ 的 {@code NBTTagCompound}/{@code CompoundTag} 与 {@code NBTTagList}/{@code ListTag}，
 * 自动适配 {@code getTag}/{@code save}/{@code saveOptional}/{@code saveWithoutMetadata} 等不同版本的方法名。
 * 实例通过 {@link #resolve()} 双重检查锁懒加载，失败时返回 {@code null}，调用方应优雅降级。
 */
final class RawNbtAccess {

    private static volatile RawNbtAccess instance;

    private final Method asNmsCopy;
    private final Constructor<?> compoundConstructor;
    private final Method getTag;
    private final Method save;
    private final Class<?> compoundClass;
    private final Class<?> listClass;
    private final Method getAllKeys;
    private final Method keySet;
    private final Method get;
    private final Method listSize;
    private final Method listGet;

    private RawNbtAccess(
        Method asNmsCopy,
        Constructor<?> compoundConstructor,
        Method getTag,
        Method save,
        Class<?> compoundClass,
        Class<?> listClass,
        Method getAllKeys,
        Method keySet,
        Method get,
        Method listSize,
        Method listGet
    ) {
        this.asNmsCopy = asNmsCopy;
        this.compoundConstructor = compoundConstructor;
        this.getTag = getTag;
        this.save = save;
        this.compoundClass = compoundClass;
        this.listClass = listClass;
        this.getAllKeys = getAllKeys;
        this.keySet = keySet;
        this.get = get;
        this.listSize = listSize;
        this.listGet = listGet;
    }

    /**
     * 判断物品的原始 NBT 树中是否包含指定键（递归遍历 CompoundTag 与 ListTag）。
     *
     * @param itemStack 待检测物品
     * @param expected  已标准化的目标键（{@link ItemMatcherLoader#normalizeId} 处理后）
     * @return {@code true} 表示 NBT 树中存在匹配键；反射环境不可用时返回 {@code false}
     */
    static boolean contains(ItemStack itemStack, String expected) {
        try {
            RawNbtAccess access = resolve();
            if (access == null) {
                return false;
            }
            Object nmsItem = access.asNmsCopy.invoke(null, itemStack);
            Object root = access.readTag(nmsItem);
            if (root == null) {
                return false;
            }
            return access.contains(root, expected,
                Collections.newSetFromMap(new IdentityHashMap<>()));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * 判断已标准化的期望键与实际 NBT 键是否匹配（全等或仅 key 部分相等，忽略 namespace 前缀）。
     *
     * @param expected 已标准化的期望键
     * @param actual   原始实际键
     * @return {@code true} 表示匹配
     */
    static boolean matchesNbtKey(String expected, String actual) {
        String normalizedActual = ItemMatcherLoader.normalizeId(actual);
        if (expected.equals(normalizedActual)) {
            return true;
        }
        int expectedSeparator = expected.lastIndexOf(':');
        int actualSeparator = normalizedActual.lastIndexOf(':');
        String expectedKey = expectedSeparator >= 0
            ? expected.substring(expectedSeparator + 1) : expected;
        String actualKey = actualSeparator >= 0
            ? normalizedActual.substring(actualSeparator + 1) : normalizedActual;
        return expectedKey.equals(actualKey);
    }

    private static RawNbtAccess resolve() {
        RawNbtAccess cached = instance;
        if (cached != null) {
            return cached;
        }
        synchronized (RawNbtAccess.class) {
            cached = instance;
            if (cached != null) {
                return cached;
            }
            try {
                if (Bukkit.getServer() == null) {
                    return null;
                }
                String craftPackage = Bukkit.getServer().getClass().getPackageName();
                Class<?> craftItemStack = Class.forName(
                    craftPackage + ".inventory.CraftItemStack"
                );
                Method asNmsCopy = craftItemStack.getMethod(
                    "asNMSCopy", ItemStack.class
                );
                String craftVersion = craftPackage.substring(
                    craftPackage.lastIndexOf('.') + 1
                );
                Class<?> compoundClass = findClass(
                    "net.minecraft.nbt.CompoundTag",
                    "net.minecraft.server." + craftVersion
                        + ".NBTTagCompound"
                );
                Constructor<?> compoundConstructor =
                    compoundClass.getDeclaredConstructor();
                Method getTag = findNoArgMethod(
                    "getTag", "getTagCompound"
                );
                Method save = findSaveMethod(compoundClass);
                if (getTag == null && save == null) {
                    return null;
                }
                Class<?> listClass = findClass(
                    "net.minecraft.nbt.ListTag",
                    "net.minecraft.server." + craftVersion
                        + ".NBTTagList"
                );
                Method getAllKeys = findMethod(compoundClass, "getAllKeys");
                Method keySet = findMethod(compoundClass, "keySet");
                Method get = findMethod(compoundClass, "get", String.class);
                Method listSize = findMethod(listClass, "size");
                Method listGet = findMethod(listClass, "get", int.class);
                if (get == null || (getAllKeys == null && keySet == null)) {
                    return null;
                }
                cached = new RawNbtAccess(
                    asNmsCopy,
                    compoundConstructor,
                    getTag,
                    save,
                    compoundClass,
                    listClass,
                    getAllKeys,
                    keySet,
                    get,
                    listSize,
                    listGet
                );
                instance = cached;
                return cached;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private Object readTag(Object nmsItem) throws ReflectiveOperationException {
        if (getTag != null) {
            Object tag = getTag.invoke(nmsItem);
            if (tag != null) {
                return tag;
            }
        }
        if (save == null) {
            return null;
        }
        Object result = save.invoke(nmsItem, compoundConstructor.newInstance());
        if (compoundClass.isInstance(result)) {
            return result;
        }
        if (result != null) {
            try {
                Method resultMethod = result.getClass().getMethod("result");
                Object optional = resultMethod.invoke(result);
                if (optional instanceof java.util.Optional<?> value) {
                    return value.orElse(null);
                }
            } catch (ReflectiveOperationException ignored) {
                // Some versions return the compound directly.
            }
        }
        return null;
    }

    private boolean contains(
        Object node,
        String expected,
        Set<Object> visited
    ) throws ReflectiveOperationException {
        if (node == null || !visited.add(node)) {
            return false;
        }
        if (compoundClass.isInstance(node)) {
            Set<?> keys = keys(node);
            if (keys == null) {
                return false;
            }
            for (Object rawKey : keys) {
                String key = String.valueOf(rawKey);
                if (matchesNbtKey(expected, key)) {
                    return true;
                }
                Object child = get.invoke(node, key);
                if (contains(child, expected, visited)) {
                    return true;
                }
            }
            return false;
        }
        if (listClass.isInstance(node) && listSize != null && listGet != null) {
            int size = ((Number) listSize.invoke(node)).intValue();
            for (int index = 0; index < size; index++) {
                if (contains(listGet.invoke(node, index), expected, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<?> keys(Object compound) throws ReflectiveOperationException {
        Object result = getAllKeys != null
            ? getAllKeys.invoke(compound) : keySet.invoke(compound);
        return result instanceof Set<?> set ? set : null;
    }

    private static Class<?> findClass(String... names)
        throws ClassNotFoundException {
        for (String name : names) {
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
                // Try the next version-specific class name.
            }
        }
        throw new ClassNotFoundException(names[0]);
    }

    private static Method findNoArgMethod(String... names) {
        for (String name : names) {
            try {
                for (Method method : findNmsItemStackClass().getMethods()) {
                    if (method.getName().equals(name)
                        && method.getParameterCount() == 0) {
                        return method;
                    }
                }
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    private static Method findSaveMethod(Class<?> compoundClass) {
        try {
            for (Method method : findNmsItemStackClass().getMethods()) {
                if ((method.getName().equals("save")
                        || method.getName().equals("saveOptional")
                        || method.getName().equals("saveWithoutMetadata"))
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0]
                        .isAssignableFrom(compoundClass)) {
                    return method;
                }
            }
        } catch (Throwable ignored) {
            // Raw NBT support is optional.
        }
        return null;
    }

    private static Class<?> findNmsItemStackClass() {
        try {
            String craftPackage = Bukkit.getServer().getClass().getPackageName();
            Class<?> craftItemStack = Class.forName(
                craftPackage + ".inventory.CraftItemStack"
            );
            Method asNmsCopy = craftItemStack.getMethod(
                "asNMSCopy", ItemStack.class
            );
            return asNmsCopy.getReturnType();
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        if (type == null) {
            return null;
        }
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
