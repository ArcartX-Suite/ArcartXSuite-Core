package xuanmo.arcartxsuite.api.condition;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 脚本条件比较运算符枚举，支持相等、不等、大小比较、包含与正则匹配。
 * <p>
 * 数值比较会尝试将两侧解析为 double；解析失败时退化为字符串字典序比较。
 */
public enum ScriptConditionOperator {
    EQ("=="),
    NE("!="),
    GTE(">="),
    LTE("<="),
    GT(">"),
    LT("<"),
    CONTAINS("contains"),
    REGEX("regex");

    private final String symbol;

    ScriptConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    /** 返回运算符的配置符号（如 "=="、"contains"）。 */
    public String symbol() {
        return symbol;
    }

    /** 返回运算符在配置中的键名（即枚举名，如 "EQ"、"REGEX"）。 */
    public String configKey() {
        return name();
    }

    /**
     * 用当前运算符对实际值与期望值进行求值。
     *
     * @param actual   实际值（占位符解析后），null 视为空串
     * @param expected 期望值，null 视为空串
     * @return {@code true} 表示条件成立
     */
    public boolean evaluate(String actual, String expected) {
        if (actual == null) {
            actual = "";
        }
        if (expected == null) {
            expected = "";
        }
        return switch (this) {
            case EQ -> actual.equalsIgnoreCase(expected);
            case NE -> !actual.equalsIgnoreCase(expected);
            case GTE -> compareNumeric(actual, expected) >= 0;
            case LTE -> compareNumeric(actual, expected) <= 0;
            case GT -> compareNumeric(actual, expected) > 0;
            case LT -> compareNumeric(actual, expected) < 0;
            case CONTAINS -> actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
            case REGEX -> {
                try {
                    yield Pattern.compile(expected, Pattern.CASE_INSENSITIVE).matcher(actual).find();
                } catch (Exception exception) {
                    yield false;
                }
            }
        };
    }

    /**
     * 从配置文本解析运算符，匹配符号或枚举名（大小写不敏感）。
     *
     * @param raw 原始文本，null/空白时默认返回 {@link #EQ}
     * @return 解析得到的运算符
     */
    public static ScriptConditionOperator parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return EQ;
        }
        String trimmed = raw.trim();
        for (ScriptConditionOperator operator : values()) {
            if (operator.symbol.equalsIgnoreCase(trimmed) || operator.name().equalsIgnoreCase(trimmed)) {
                return operator;
            }
        }
        return EQ;
    }

    private static int compareNumeric(String actual, String expected) {
        try {
            double actualValue = Double.parseDouble(actual.trim());
            double expectedValue = Double.parseDouble(expected.trim());
            return Double.compare(actualValue, expectedValue);
        } catch (NumberFormatException exception) {
            return actual.compareToIgnoreCase(expected);
        }
    }
}
