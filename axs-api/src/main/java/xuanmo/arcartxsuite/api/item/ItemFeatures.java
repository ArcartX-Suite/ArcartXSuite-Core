package xuanmo.arcartxsuite.api.item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 从 {@link ItemStack} 惰性提取的匹配特征集合。
 * <p>
 * 各特征字段在首次访问时计算并缓存，避免 matcher 仅配置部分维度时做无用提取。
 * 该类非线程安全，预期仅在匹配调用的单线程上下文中使用。
 */
final class ItemFeatures {

    private final ItemStack itemStack;
    private final Function<ItemStack, String> mythicResolver;
    private final Function<ItemStack, String> neigeResolver;
    private final Function<ItemStack, String> overtureResolver;

    private String materialId;
    private String mythicItemId;
    private String neigeItemId;
    private String overtureItemId;
    private String normalizedName;
    private List<String> loreLines;
    private Set<String> kinds;

    ItemFeatures(
        ItemStack itemStack,
        Function<ItemStack, String> mythicResolver,
        Function<ItemStack, String> neigeResolver,
        Function<ItemStack, String> overtureResolver
    ) {
        this.itemStack = itemStack;
        this.mythicResolver = mythicResolver;
        this.neigeResolver = neigeResolver;
        this.overtureResolver = overtureResolver;
    }

    String materialId() {
        if (materialId == null) {
            materialId = ItemMatcherLoader.normalizeId(itemStack.getType().name());
        }
        return materialId;
    }

    String mythicItemId() {
        if (mythicItemId == null) {
            mythicItemId = ItemMatcherLoader.normalizeId(mythicResolver.apply(itemStack));
        }
        return mythicItemId;
    }

    String neigeItemId() {
        if (neigeItemId == null) {
            neigeItemId = ItemMatcherLoader.normalizeId(neigeResolver.apply(itemStack));
        }
        return neigeItemId;
    }

    String overtureItemId() {
        if (overtureItemId == null) {
            overtureItemId = ItemMatcherLoader.normalizeId(overtureResolver.apply(itemStack));
        }
        return overtureItemId;
    }

    String normalizedName() {
        if (normalizedName == null) {
            normalizedName = normalizeToken(rawDisplayName());
        }
        return normalizedName;
    }

    List<String> loreLines() {
        if (loreLines == null) {
            loreLines = normalizedLore();
        }
        return loreLines;
    }

    Set<String> kinds() {
        if (kinds == null) {
            kinds = detectKinds();
        }
        return kinds;
    }

    private String rawDisplayName() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return itemStack.getType().name();
        }
        return meta.getDisplayName();
    }

    private List<String> normalizedLore() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || meta.getLore() == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String line : meta.getLore()) {
            result.add(normalizeToken(line));
        }
        return List.copyOf(result);
    }

    private Set<String> detectKinds() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Material material = itemStack.getType();
        String name = material.name();
        for (KindRule rule : MATERIAL_KIND_RULES) {
            if (rule.predicate().test(material)) {
                result.add(rule.kind());
            }
        }
        if (!mythicItemId().isBlank()) {
            result.add("mythic");
        }
        if (!neigeItemId().isBlank()) {
            result.add("neige");
        }
        if (!overtureItemId().isBlank()) {
            result.add("overture");
        }
        if (name.contains("INGOT") || name.contains("GEM") || name.contains("SHARD") || name.contains("ORE")) {
            result.add("material");
        }
        return result;
    }

    private static String normalizeToken(String value) {
        return ChatColor.stripColor(value == null ? "" : value).trim().toLowerCase(Locale.ROOT);
    }

    /** 材质名 → kind 的静态规则表，按声明顺序求值。 */
    private record KindRule(String kind, Predicate<Material> predicate) {}

    private static final List<KindRule> MATERIAL_KIND_RULES = List.of(
        new KindRule("block", Material::isBlock),
        new KindRule("food", Material::isEdible),
        new KindRule("consumable", Material::isEdible),
        new KindRule("weapon", m -> m.name().endsWith("_SWORD") || m.name().endsWith("_AXE")),
        new KindRule("tool", m -> m.name().endsWith("_PICKAXE") || m.name().endsWith("_SHOVEL") || m.name().endsWith("_HOE")),
        new KindRule("armor", m -> m.name().endsWith("_HELMET") || m.name().endsWith("_CHESTPLATE") || m.name().endsWith("_LEGGINGS") || m.name().endsWith("_BOOTS") || m.name().equals("SHIELD")),
        new KindRule("ranged", m -> m.name().endsWith("_BOW") || m.name().equals("BOW") || m.name().equals("CROSSBOW") || m.name().equals("TRIDENT")),
        new KindRule("potion", m -> m.name().contains("POTION")),
        new KindRule("consumable", m -> m.name().contains("POTION"))
    );
}
