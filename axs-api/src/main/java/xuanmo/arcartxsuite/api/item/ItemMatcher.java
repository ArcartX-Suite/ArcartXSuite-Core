package xuanmo.arcartxsuite.api.item;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 物品匹配规则记录，支持多维度匹配原版与第三方插件物品。
 * <p>
 * 所有列表字段为"或"关系——物品命中任一规则即视为匹配；空列表表示该维度不参与匹配。
 *
 * @param materialIds     原版材质 id 白名单（小写）
 * @param mythicItemIds   MythicMobs 物品 id 白名单
 * @param neigeItemIds    NeigeItems 物品 id 白名单
 * @param overtureItemIds Overture 物品 id 白名单
 * @param kinds           物品类别白名单（如 "mythic"、"neige"、"vanilla"）
 * @param nameContains    显示名需包含的子串
 * @param loreContains    Lore 需包含的子串
 * @param nbtKeys         NBT/PDC 键白名单
 * @param namePatterns    显示名正则白名单
 * @param lorePatterns    Lore 正则白名单
 */
public record ItemMatcher(
    List<String> materialIds,
    List<String> mythicItemIds,
    List<String> neigeItemIds,
    List<String> overtureItemIds,
    List<String> kinds,
    List<String> nameContains,
    List<String> loreContains,
    List<String> nbtKeys,
    List<Pattern> namePatterns,
    List<Pattern> lorePatterns
) {

    /** 返回一个所有维度均为空的匹配器（匹配任何物品时配合 emptyMatcher 使用）。 */
    public static ItemMatcher empty() {
        return new ItemMatcher(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /**
     * 判断该匹配器是否所有维度均为空（即未配置任何匹配规则）。
     *
     * @return {@code true} 表示匹配器为空，不应阻止任何物品
     */
    public boolean emptyMatcher() {
        return materialIds.isEmpty()
            && mythicItemIds.isEmpty()
            && neigeItemIds.isEmpty()
            && overtureItemIds.isEmpty()
            && kinds.isEmpty()
            && nameContains.isEmpty()
            && loreContains.isEmpty()
            && nbtKeys.isEmpty()
            && namePatterns.isEmpty()
            && lorePatterns.isEmpty();
    }
}
