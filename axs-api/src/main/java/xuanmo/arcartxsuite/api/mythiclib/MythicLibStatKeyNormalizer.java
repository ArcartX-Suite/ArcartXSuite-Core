package xuanmo.arcartxsuite.api.mythiclib;

import java.util.Locale;

/**
 * MythicLib 属性键名标准化工具。
 * <p>
 * 将任意字符串转换为 MythicLib 接受的 STAT_KEY 格式：仅保留字母数字，
 * 非字母数字字符替换为下划线，合并连续下划线，去除首尾下划线，最终转大写。
 */
public final class MythicLibStatKeyNormalizer {

    private MythicLibStatKeyNormalizer() {
    }

    /**
     * 标准化属性键名。
     *
     * @param rawValue 原始键名，null 返回空串
     * @return 标准化后的大写键名；空输入返回空串
     */
    public static String normalize(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String normalized = rawValue
            .trim()
            .replaceAll("[^A-Za-z0-9]+", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_", "")
            .replaceAll("_$", "");
        return normalized.isBlank() ? "" : normalized.toUpperCase(Locale.ROOT);
    }
}
