package xuanmo.arcartxsuite.api.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * 版本兼容的 Bukkit Attribute 解析工具。
 * <p>
 * 使用 {@code Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"))} 获取属性实例，
 * 该 API 在 Spigot 1.19.3+（含 1.20.1 和 1.21+）中均可用，
 * 无需反射、无需 try-catch、类型安全。
 */
public final class AttributeResolver {

    private AttributeResolver() {}

    private static final Attribute MAX_HEALTH = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));

    /**
     * 获取实体的最大生命值。
     *
     * @param entity 目标实体，可为 null
     * @return 最大生命值，解析失败时回退到 20.0
     */
    public static double getMaxHealth(LivingEntity entity) {
        if (entity == null) return 20.0;
        if (MAX_HEALTH != null) {
            AttributeInstance instance = entity.getAttribute(MAX_HEALTH);
            if (instance != null) return instance.getValue();
        }
        return 20.0;
    }

    /**
     * 获取实体的 AttributeInstance。
     *
     * @param entity 目标实体
     * @return AttributeInstance，解析失败时返回 null
     */
    public static AttributeInstance getMaxHealthAttribute(LivingEntity entity) {
        if (entity == null || MAX_HEALTH == null) return null;
        return entity.getAttribute(MAX_HEALTH);
    }
}
