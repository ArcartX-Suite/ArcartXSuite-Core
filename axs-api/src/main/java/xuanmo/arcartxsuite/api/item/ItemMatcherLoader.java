package xuanmo.arcartxsuite.api.item;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * 物品匹配器加载工具，从 YAML 配置节解析出 {@link ItemMatcher}。
 * <p>
 * 负责字符串列表标准化（去空白、小写）与正则编译（无效正则记录警告并跳过）。
 */
public final class ItemMatcherLoader {

    private ItemMatcherLoader() {
    }

    /**
     * 从配置节加载物品匹配器。
     *
     * @param section 配置节，null 返回空匹配器
     * @param logger  日志器，用于记录无效正则
     * @param path    配置路径前缀（用于日志定位）
     * @return 解析得到的匹配器
     */
    public static ItemMatcher load(ConfigurationSection section, Logger logger, String path) {
        if (section == null) {
            return ItemMatcher.empty();
        }
        return new ItemMatcher(
            normalizeStringList(section.getStringList("material-ids")),
            normalizeStringList(section.getStringList("mythic-item-ids")),
            normalizeStringList(section.getStringList("neige-item-ids")),
            normalizeStringList(section.getStringList("overture-item-ids")),
            normalizeStringList(section.getStringList("kinds")),
            normalizeDisplayList(section.getStringList("name-contains")),
            normalizeDisplayList(section.getStringList("lore-contains")),
            normalizeStringList(section.getStringList("nbt-keys")),
            loadNbtValues(section.getConfigurationSection("nbt-values")),
            compilePatterns(section.getStringList("name-regex"), logger, path + ".name-regex"),
            compilePatterns(section.getStringList("lore-regex"), logger, path + ".lore-regex")
        );
    }

    /**
     * 标准化字符串列表：去空白、转小写、过滤空值。
     *
     * @param values 原始列表，null 返回空列表
     * @return 不可变标准化列表
     */
    public static List<String> normalizeStringList(List<String> values) {
        List<String> normalized = new ArrayList<>();
        if (values == null) {
            return List.of();
        }
        for (String value : values) {
            String normalizedValue = normalizeId(value);
            if (!normalizedValue.isBlank()) {
                normalized.add(normalizedValue);
            }
        }
        return List.copyOf(normalized);
    }

    /**
     * 将字符串列表编译为正则Pattern列表（大小写不敏感、Unicode），无效正则记录警告并跳过。
     *
     * @param values 原始正则字符串列表
     * @param logger 日志器
     * @param path   配置路径（用于日志定位）
     * @return 不可变 Pattern 列表
     */
    private static Map<String, String> loadNbtValues(ConfigurationSection section) {
        if (section == null) return Map.of();
        Map<String, String> values = new LinkedHashMap<>();
        for (String rawKey : section.getKeys(false)) {
            String key = normalizeId(rawKey);
            if (key.isBlank()) continue;
            Object rawValue = section.get(rawKey);
            values.put(key, normalizeNbtValue(rawValue == null ? "" : String.valueOf(rawValue)));
        }
        return Map.copyOf(values);
    }

    public static String normalizeNbtValue(String rawValue) {
        return rawValue == null ? "" : rawValue.trim().toLowerCase(Locale.ROOT);
    }

    public static List<Pattern> compilePatterns(List<String> values, Logger logger, String path) {
        List<Pattern> patterns = new ArrayList<>();
        if (values == null) {
            return List.of();
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                patterns.add(Pattern.compile(value, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            } catch (PatternSyntaxException exception) {
                logger.warning(path + " 存在无效正则 '" + value + "'，已跳过。");
            }
        }
        return List.copyOf(patterns);
    }

    /**
     * 标准化单个 id：去首尾空白、转小写。null 返回空串。
     *
     * @param rawValue 原始值
     * @return 标准化后的 id
     */
    public static String normalizeId(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        return rawValue.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 标准化显示文本：去除颜色码、去首尾空白、转小写。
     * <p>
     * 与 {@link #normalizeId(String)} 的区别在于额外调用 {@link ChatColor#stripColor(String)}，
     * 确保 name-contains / lore-contains 等用于匹配物品显示文本的配置项
     * 与 {@link ItemFeatures#normalizedName()} / {@link ItemFeatures#loreLines()} 的归一化方式一致。
     *
     * @param rawValue 原始值
     * @return 去除颜色码并标准化后的文本
     */
    public static String normalizeDisplayText(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        return ChatColor.stripColor(rawValue).trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 标准化显示文本列表：去除颜色码、去空白、转小写、过滤空值。
     *
     * @param values 原始列表，null 返回空列表
     * @return 不可变标准化列表
     */
    public static List<String> normalizeDisplayList(List<String> values) {
        List<String> normalized = new ArrayList<>();
        if (values == null) {
            return List.of();
        }
        for (String value : values) {
            String normalizedValue = normalizeDisplayText(value);
            if (!normalizedValue.isBlank()) {
                normalized.add(normalizedValue);
            }
        }
        return List.copyOf(normalized);
    }
}
