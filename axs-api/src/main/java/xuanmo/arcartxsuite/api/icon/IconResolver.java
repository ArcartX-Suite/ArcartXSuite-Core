package xuanmo.arcartxsuite.api.icon;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ItemBridgeAPI;
import xuanmo.arcartxsuite.api.item.ItemSourceRegistry;

/**
 * 通用图标解析器，将 {@link IconDefinition} 转换为 ItemStack 或 itemJson 字符串。
 * <p>
 * 支持原版材质、外部物品来源（MythicMobs/NeigeItems/Overture/MMOItems）、
 * 自定义 NBT 标签、头颅纹理和皮革染色等图标属性。
 * <p>
 * 该类原为 Menu 模块的 {@code MenuIconResolver}，提升至 axs-api 共享。
 * 占位符解析通过构造时注入的 {@code placeholderResolver} 解耦：
 * Menu 模块传入带 PlaceholderAPI 的解析器，其他模块（如 Lottery）可不传（原样返回）。
 */
public final class IconResolver {

    private static final Pattern SKIN_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(.*?)\"");

    private final ItemBridgeAPI itemStackBridge;
    private final ItemSourceRegistry itemSourceRegistry;
    private final BiFunction<Player, String, String> placeholderResolver;

    /**
     * 构造图标解析器，不解析占位符。
     *
     * @param itemStackBridge    ItemStack 桥接 API，可为 null
     * @param itemSourceRegistry 物品来源注册表，可为 null
     */
    public IconResolver(ItemBridgeAPI itemStackBridge, ItemSourceRegistry itemSourceRegistry) {
        this(itemStackBridge, itemSourceRegistry, null);
    }

    /**
     * 构造图标解析器。
     *
     * @param itemStackBridge    ItemStack 桥接 API，可为 null
     * @param itemSourceRegistry 物品来源注册表，可为 null
     * @param placeholderResolver 占位符解析函数（player, text）→ 解析后文本，为 null 时不解析
     */
    public IconResolver(ItemBridgeAPI itemStackBridge, ItemSourceRegistry itemSourceRegistry,
                        @Nullable BiFunction<Player, String, String> placeholderResolver) {
        this.itemStackBridge = itemStackBridge;
        this.itemSourceRegistry = itemSourceRegistry;
        this.placeholderResolver = placeholderResolver;
    }

    /**
     * 将图标定义解析为 itemJson 字符串，供 UI 引擎渲染。
     *
     * @param player 目标玩家，用于占位符解析
     * @param icon   图标定义，为 null 或无有效图标时返回空字符串
     * @return itemJson 字符串
     */
    public String resolveItemJson(Player player, @Nullable IconDefinition icon) {
        if (icon == null || !icon.hasIcon()) {
            return "";
        }
        if (icon.json() != null && !icon.json().isBlank()) {
            return icon.json();
        }
        ItemStack stack = resolveItemStack(player, icon);
        if (stack == null || itemStackBridge == null) {
            return "";
        }
        stack = applyCustomNbt(stack, icon);
        return itemStackBridge.itemToJson(stack).orElse("");
    }

    /**
     * 将 ItemStack 序列化为 itemJson 字符串，供 UI 引擎渲染。
     *
     * @param stack 物品堆，为 null 时返回空字符串
     * @return itemJson 字符串
     */
    public String itemToJson(@Nullable ItemStack stack) {
        if (stack == null || itemStackBridge == null) {
            return "";
        }
        return itemStackBridge.itemToJson(stack).orElse("");
    }

    private ItemStack applyCustomNbt(ItemStack stack, IconDefinition icon) {
        if (stack == null) {
            return stack;
        }
        if (icon.nbtString() != null && !icon.nbtString().isBlank()) {
            try {
                ItemStack modified =
                    Bukkit.getUnsafe().modifyItemStack(stack.clone(), icon.nbtString());
                if (modified != null) {
                    stack = modified;
                }
            } catch (Exception ignored) {
                // SNBT 解析失败时保留原物品
            }
        }
        if (itemStackBridge == null) {
            return stack;
        }
        if (icon.texture() != null && !icon.texture().isBlank()) {
            stack = itemStackBridge.putStringTag(stack, "icon", icon.texture());
        }
        if (icon.textureUrl() != null && !icon.textureUrl().isBlank()) {
            stack = itemStackBridge.putStringTag(stack, "url", icon.textureUrl());
        }
        if (icon.nbt() != null) {
            for (java.util.Map.Entry<String, String> entry : icon.nbt().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && !key.isBlank() && value != null) {
                    stack = itemStackBridge.putStringTag(stack, key, value);
                }
            }
        }
        return stack;
    }

    @Nullable
    private ItemStack resolveItemStack(Player player, IconDefinition icon) {
        ItemStack generated = generateExternalItem(player, icon);
        if (generated != null) {
            return generated;
        }
        Material material = Material.matchMaterial(icon.material());
        if (material == null || material.isAir()) {
            return null;
        }
        ItemStack stack = new ItemStack(material, Math.max(1, icon.amount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (icon.name() != null && !icon.name().isBlank()) {
                meta.setDisplayName(colorize(applyPlaceholders(player, icon.name())));
            }
            if (icon.lore() != null && !icon.lore().isEmpty()) {
                meta.setLore(icon.lore().stream()
                    .map(line -> colorize(applyPlaceholders(player, line)))
                    .toList());
            }
            if (icon.customModelData() > 0) {
                meta.setCustomModelData(icon.customModelData());
            }
            applyGlow(meta, icon);
            applySkullTexture(meta, icon);
            applyColor(meta, icon);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String applyPlaceholders(Player player, String input) {
        if (placeholderResolver == null || input == null) {
            return input;
        }
        return placeholderResolver.apply(player, input);
    }

    private void applyGlow(ItemMeta meta, IconDefinition icon) {
        if (!icon.glow()) {
            return;
        }
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
        if (enchantment == null) {
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft("infinity"));
        }
        if (enchantment != null) {
            meta.addEnchant(enchantment, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    private void applySkullTexture(ItemMeta meta, IconDefinition icon) {
        if (icon.skullTexture() == null || icon.skullTexture().isBlank() || !(meta instanceof SkullMeta skullMeta)) {
            return;
        }
        String url = extractSkinUrl(icon.skullTexture().trim());
        if (url == null) {
            return;
        }
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new java.net.URI(url).toURL());
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
        } catch (java.net.URISyntaxException | java.net.MalformedURLException | RuntimeException exception) {
            // malformed texture: leave the head as-is
        }
    }

    @Nullable
    private static String extractSkinUrl(String raw) {
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
        Matcher matcher = SKIN_URL_PATTERN.matcher(decoded);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void applyColor(ItemMeta meta, IconDefinition icon) {
        if (icon.color() == null || icon.color().isBlank() || !(meta instanceof LeatherArmorMeta leatherMeta)) {
            return;
        }
        Color parsed = parseColor(icon.color().trim());
        if (parsed != null) {
            leatherMeta.setColor(parsed);
        }
    }

    @Nullable
    private static Color parseColor(String raw) {
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;
        if (hex.length() != 6) {
            return null;
        }
        try {
            return Color.fromRGB(Integer.parseInt(hex, 16));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Nullable
    private ItemStack generateExternalItem(Player player, IconDefinition icon) {
        if (itemSourceRegistry == null) {
            return null;
        }
        String source = icon.source() == null ? "" : icon.source().trim().toLowerCase(Locale.ROOT);
        if (!source.isBlank() && icon.sourceId() != null && !icon.sourceId().isBlank()) {
            return switch (source) {
                case "mythic", "mythicmobs" -> itemSourceRegistry.generateMythicItem(icon.sourceId(), icon.amount());
                case "neige", "neigeitems" -> itemSourceRegistry.generateNeigeItem(icon.sourceId(), icon.amount());
                case "overture" -> itemSourceRegistry.generateOvertureItem(icon.sourceId(), player, icon.amount());
                case "mmo", "mmoitems" -> {
                    if (icon.mmoType() != null && !icon.mmoType().isBlank()
                        && icon.mmoId() != null && !icon.mmoId().isBlank()) {
                        yield itemSourceRegistry.generateMmoItem(icon.mmoType(), icon.mmoId(), icon.amount());
                    }
                    String[] parts = icon.sourceId().split("[:;]", 2);
                    if (parts.length == 2) {
                        yield itemSourceRegistry.generateMmoItem(parts[0], parts[1], icon.amount());
                    }
                    yield null;
                }
                default -> null;
            };
        }
        if (icon.mmoType() != null && !icon.mmoType().isBlank()
            && icon.mmoId() != null && !icon.mmoId().isBlank()) {
            return itemSourceRegistry.generateMmoItem(icon.mmoType(), icon.mmoId(), icon.amount());
        }
        return null;
    }

    private static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
