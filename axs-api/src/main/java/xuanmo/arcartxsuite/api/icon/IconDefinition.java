package xuanmo.arcartxsuite.api.icon;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * 通用图标定义，描述一个 UI 物品图标的来源和显示属性。
 * <p>
 * 支持多种图标来源：原版材质、MythicMobs/NeigeItems/Overture/MMOItems 外部物品、
 * 自定义 JSON、头颅纹理、皮革染色等，由 {@link IconResolver} 解析为 ItemStack
 * 并序列化为客户端可识别的 itemJson 字符串。
 * <p>
 * 该类原为 Menu 模块的 {@code MenuIconDefinition}，因 Lottery 等模块同样需要
 * 结构化图标定义（支持 custom-model-data / NBT 贴图等），提升至 axs-api 共享。
 *
 * @param material        原版材质名称
 * @param amount          物品数量
 * @param name            显示名称
 * @param lore            物品 Lore 行列表
 * @param customModelData 自定义模型数据 ID（用于资源包自定义贴图）
 * @param source          外部物品来源类型（mythic/neige/overture/mmo）
 * @param sourceId        外部物品 ID
 * @param mmoType         MMOItems 物品类型
 * @param mmoId           MMOItems 物品 ID
 * @param json            自定义物品 JSON
 * @param texture         图标纹理标识，写入 NBT {@code icon} 标签
 * @param textureUrl      纹理 URL，写入 NBT {@code url} 标签
 * @param nbt             自定义 NBT 键值对
 * @param glow            是否附魔发光
 * @param skullTexture    头颅纹理（Base64 或 URL）
 * @param color           皮革染色颜色（十六进制）
 */
public record IconDefinition(
    String material,
    int amount,
    String name,
    java.util.List<String> lore,
    int customModelData,
    String source,
    String sourceId,
    String mmoType,
    String mmoId,
    String json,
    String texture,
    String textureUrl,
    java.util.Map<String, String> nbt,
    boolean glow,
    String skullTexture,
    String color
) {

    /**
     * 判断该图标定义是否包含有效图标（JSON、外部物品或材质至少一项非空）。
     *
     * @return {@code true} 表示有有效图标
     */
    public boolean hasIcon() {
        if (json != null && !json.isBlank()) {
            return true;
        }
        if (source != null && !source.isBlank() && sourceId != null && !sourceId.isBlank()) {
            return true;
        }
        if (mmoType != null && !mmoType.isBlank() && mmoId != null && !mmoId.isBlank()) {
            return true;
        }
        return material != null && !material.isBlank();
    }

    /**
     * 从配置节加载图标定义。
     *
     * @param section 图标配置节
     * @return 解析后的图标定义，无有效图标时返回 {@code null}
     */
    @Nullable
    public static IconDefinition load(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        IconDefinition icon = new IconDefinition(
            section.getString("material", ""),
            Math.max(1, section.getInt("amount", 1)),
            section.getString("name", ""),
            section.getStringList("lore"),
            section.getInt("custom-model-data", section.getInt("customModelData", 0)),
            section.getString("source", ""),
            section.getString("id", section.getString("item-id", "")),
            section.getString("mmo-type", ""),
            section.getString("mmo-id", ""),
            section.getString("json", ""),
            section.getString("texture", ""),
            section.getString("texture-url", section.getString("url", "")),
            loadNbt(section),
            section.getBoolean("glow", false),
            section.getString("skull-texture", section.getString("skullTexture", "")),
            section.getString("color", "")
        );
        return icon.hasIcon() ? icon : null;
    }

    private static java.util.Map<String, String> loadNbt(ConfigurationSection section) {
        ConfigurationSection nbtSection = section.getConfigurationSection("nbt");
        if (nbtSection == null) {
            return java.util.Map.of();
        }
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (String key : nbtSection.getKeys(false)) {
            Object value = nbtSection.get(key);
            if (value != null) {
                map.put(key, String.valueOf(value));
            }
        }
        return map;
    }
}
