package xuanmo.arcartxsuite.api.item;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * {@link ItemMatcherAPI} 的默认实现，基于多维度规则匹配物品。
 * <p>
 * 支持原版材质、MythicMobs/NeigeItems/Overture 物品 id、类别、名称/Lore 子串与正则、
 * NBT/PDC 键匹配。物品特征提取由 {@link ItemFeatures} 惰性完成，
 * NBT 反射访问由 {@link RawNbtAccess} 跨版本兼容处理。
 */
public final class ItemMatcherSupport implements ItemMatcherAPI {

    private final Function<ItemStack, String> mythicItemIdResolver;
    private final Function<ItemStack, String> neigeItemIdResolver;
    private final Function<ItemStack, String> overtureItemIdResolver;

    /**
     * 构造匹配器（兼容旧版，无 Overture 解析器）。
     *
     * @param mythicItemIdResolver MythicMobs 物品 id 解析器，null 时视为始终返回空
     * @param neigeItemIdResolver  NeigeItems 物品 id 解析器，null 时视为始终返回空
     */
    public ItemMatcherSupport(Function<ItemStack, String> mythicItemIdResolver, Function<ItemStack, String> neigeItemIdResolver) {
        this(mythicItemIdResolver, neigeItemIdResolver, null);
    }

    /**
     * 构造匹配器（完整版）。
     *
     * @param mythicItemIdResolver   MythicMobs 物品 id 解析器
     * @param neigeItemIdResolver    NeigeItems 物品 id 解析器
     * @param overtureItemIdResolver Overture 物品 id 解析器，null 时视为始终返回空
     */
    public ItemMatcherSupport(Function<ItemStack, String> mythicItemIdResolver, Function<ItemStack, String> neigeItemIdResolver, Function<ItemStack, String> overtureItemIdResolver) {
        this.mythicItemIdResolver = mythicItemIdResolver == null ? item -> "" : mythicItemIdResolver;
        this.neigeItemIdResolver = neigeItemIdResolver == null ? item -> "" : neigeItemIdResolver;
        this.overtureItemIdResolver = overtureItemIdResolver == null ? item -> "" : overtureItemIdResolver;
    }

    /**
     * 判断物品是否满足匹配器的全部维度规则。
     * <p>
     * 空匹配器或空气物品直接返回 false；各维度间为"与"关系，维度内列表为"或"关系。
     * 物品特征按需惰性提取，未配置的维度不会触发对应特征的提取开销。
     *
     * @param matcher   匹配规则
     * @param itemStack 待匹配物品
     * @return {@code true} 表示物品命中所有已配置的维度
     */
    @Override
    public boolean matches(ItemMatcher matcher, ItemStack itemStack) {
        if (matcher == null || matcher.emptyMatcher() || itemStack == null || itemStack.getType().isAir()) {
            return false;
        }

        ItemFeatures features = new ItemFeatures(
            itemStack, mythicItemIdResolver, neigeItemIdResolver, overtureItemIdResolver
        );

        if (!matcher.materialIds().isEmpty() && !matcher.materialIds().contains(features.materialId())) {
            return false;
        }
        if (!matcher.mythicItemIds().isEmpty() && !matcher.mythicItemIds().contains(features.mythicItemId())) {
            return false;
        }
        if (!matcher.neigeItemIds().isEmpty() && !matcher.neigeItemIds().contains(features.neigeItemId())) {
            return false;
        }
        if (!matcher.overtureItemIds().isEmpty() && !matcher.overtureItemIds().contains(features.overtureItemId())) {
            return false;
        }
        if (!matcher.kinds().isEmpty() && matcher.kinds().stream().noneMatch(features.kinds()::contains)) {
            return false;
        }
        if (!matcher.nameContains().isEmpty() && matcher.nameContains().stream().noneMatch(features.normalizedName()::contains)) {
            return false;
        }
        if (!matcher.loreContains().isEmpty() && matcher.loreContains().stream().noneMatch(token -> features.loreLines().stream().anyMatch(line -> line.contains(token)))) {
            return false;
        }
        if (!matcher.nbtKeys().isEmpty() && matcher.nbtKeys().stream().noneMatch(key -> hasNbtKey(itemStack, key))) {
            return false;
        }
        if (!matcher.namePatterns().isEmpty() && matcher.namePatterns().stream().noneMatch(pattern -> pattern.matcher(features.normalizedName()).find())) {
            return false;
        }
        if (!matcher.lorePatterns().isEmpty() && matcher.lorePatterns().stream().noneMatch(pattern -> features.loreLines().stream().anyMatch(line -> pattern.matcher(line).find()))) {
            return false;
        }
        return true;
    }

    private static boolean hasNbtKey(ItemStack itemStack, String configuredKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            return false;
        }
        String expected = ItemMatcherLoader.normalizeId(configuredKey);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
                if (RawNbtAccess.matchesNbtKey(expected, key.toString())
                    || RawNbtAccess.matchesNbtKey(expected, key.getKey())) {
                    return true;
                }
            }
        }
        return RawNbtAccess.contains(itemStack, expected);
    }
}
