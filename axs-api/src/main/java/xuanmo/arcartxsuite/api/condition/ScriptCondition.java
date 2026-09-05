package xuanmo.arcartxsuite.api.condition;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record ScriptCondition(
    ScriptConditionKind kind,
    String placeholder,
    ScriptConditionOperator operator,
    String value,
    String script,
    String raw
) {

    private static final Logger LOGGER = Logger.getLogger("ArcartXSuite");

    private static final Pattern INLINE_PAPI_PATTERN = Pattern.compile(
        "^(%[^%]+%)\\s+(==|!=|>=|<=|>|<|contains|regex)\\s+(.+)$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern INLINE_CONTEXT_PATTERN = Pattern.compile(
        "^(\\{[^}]+\\})\\s+(==|!=|>=|<=|>|<|contains|regex)\\s+(.+)$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern INLINE_ARIA_PREFIX = Pattern.compile("^aria\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INLINE_JS_PREFIX = Pattern.compile("^js\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static ScriptCondition papi(String placeholder, ScriptConditionOperator operator, String value, String raw) {
        return new ScriptCondition(ScriptConditionKind.PAPI, placeholder, operator, value, null, raw);
    }

    public static ScriptCondition aria(String script, String raw) {
        return new ScriptCondition(ScriptConditionKind.ARIA, null, null, null, script, raw);
    }

    public static ScriptCondition js(String script, String raw) {
        return new ScriptCondition(ScriptConditionKind.JS, null, null, null, script, raw);
    }

    /** 上下文变量条件：左侧为 {variable}，右侧可为 {variable} 或字面量，评估时从变量表取值。 */
    public static ScriptCondition context(String placeholder, ScriptConditionOperator operator, String value, String raw) {
        return new ScriptCondition(ScriptConditionKind.CONTEXT, placeholder, operator, value, null, raw);
    }

    @Nullable
    public static ScriptCondition parseInline(String inline) {
        if (inline == null || inline.isBlank()) {
            return null;
        }
        String trimmed = inline.trim();
        Matcher jsMatcher = INLINE_JS_PREFIX.matcher(trimmed);
        if (jsMatcher.matches()) {
            String script = jsMatcher.group(1).trim();
            return script.isBlank() ? null : js(script, trimmed);
        }
        Matcher ariaMatcher = INLINE_ARIA_PREFIX.matcher(trimmed);
        if (ariaMatcher.matches()) {
            String script = ariaMatcher.group(1).trim();
            return script.isBlank() ? null : aria(script, trimmed);
        }
        Matcher contextMatcher = INLINE_CONTEXT_PATTERN.matcher(trimmed);
        if (contextMatcher.matches()) {
            return context(
                contextMatcher.group(1).trim(),
                ScriptConditionOperator.parse(contextMatcher.group(2).trim()),
                contextMatcher.group(3).trim(),
                trimmed
            );
        }
        Matcher papiMatcher = INLINE_PAPI_PATTERN.matcher(trimmed);
        if (!papiMatcher.matches()) {
            return null;
        }
        return papi(
            papiMatcher.group(1).trim(),
            ScriptConditionOperator.parse(papiMatcher.group(2).trim()),
            papiMatcher.group(3).trim(),
            trimmed
        );
    }

    @Nullable
    public static ScriptCondition fromSection(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        // 别名收敛：type 为主字段，kind 为废弃别名
        String type = section.getString("type", "");
        if (type.isBlank()) {
            String kind = section.getString("kind");
            if (kind != null && !kind.isBlank()) {
                LOGGER.warning("[ScriptCondition] 配置使用了废弃别名 'kind'，请改用 'type'。当前路径: " + section.getCurrentPath());
                type = kind.trim();
            } else {
                type = "";
            }
        } else {
            type = type.trim();
        }
        if ("aria".equalsIgnoreCase(type)) {
            return parseAriaSection(section);
        }
        if ("js".equalsIgnoreCase(type)) {
            return parseJsSection(section);
        }
        String jsInline = firstNonBlank(
            section.getString("js")
        );
        if (jsInline != null) {
            return js(jsInline, jsInline);
        }
        String ariaInline = firstNonBlank(
            section.getString("aria")
        );
        if (ariaInline != null) {
            return aria(ariaInline, ariaInline);
        }
        // script 为主字段，expression/code 为废弃别名
        String script = section.getString("script");
        if (script == null || script.isBlank()) {
            script = resolveAlias(section, "expression", "script");
        }
        if (script == null || script.isBlank()) {
            script = resolveAlias(section, "code", "script");
        }
        if (script != null && isBlank(section.getString("placeholder")) && isBlank(section.getString("placeholders"))) {
            return aria(script.trim(), script.trim());
        }
        // expr 为主字段（内联表达式），expression 在此处也是别名
        String inline = section.getString("expr");
        if (inline == null || inline.isBlank()) {
            inline = resolveAlias(section, "expression", "expr");
        }
        if (inline != null) {
            ScriptCondition parsed = parseInline(inline.trim());
            if (parsed != null) {
                return parsed;
            }
        }
        // placeholder 为主字段，placeholders 为废弃别名
        String placeholder = section.getString("placeholder", "");
        if (placeholder == null || placeholder.isBlank()) {
            placeholder = resolveAlias(section, "placeholders", "placeholder");
        }
        if (placeholder == null || placeholder.isBlank()) {
            return null;
        }
        placeholder = placeholder.trim();
        if (!placeholder.startsWith("%") || !placeholder.endsWith("%")) {
            placeholder = "%" + placeholder + "%";
        }
        // operator 为主字段，op 为废弃别名
        String operator = section.getString("operator", "");
        if (operator == null || operator.isBlank()) {
            operator = resolveAlias(section, "op", "operator");
        }
        if (operator == null || operator.isBlank()) {
            operator = "==";
        }
        String value = section.getString("value", "");
        String raw = placeholder + " " + operator + " " + value;
        return papi(placeholder, ScriptConditionOperator.parse(operator), value, raw);
    }

    /**
     * 读取废弃别名字段并打 warning 日志。
     *
     * @param section     配置节
     * @param aliasName   废弃别名
     * @param canonical   推荐的主字段名
     * @return 别名值（trimmed），若不存在则 null
     */
    @Nullable
    private static String resolveAlias(ConfigurationSection section, String aliasName, String canonical) {
        String value = section.getString(aliasName);
        if (value != null && !value.isBlank()) {
            LOGGER.warning("[ScriptCondition] 配置使用了废弃别名 '" + aliasName + "'，请改用 '" + canonical + "'。当前路径: " + section.getCurrentPath());
            return value.trim();
        }
        return null;
    }

    @Nullable
    private static ScriptCondition parseAriaSection(ConfigurationSection section) {
        String script = firstNonBlank(
            section.getString("script"),
            resolveAlias(section, "expression", "script"),
            resolveAlias(section, "code", "script"),
            section.getString("aria")
        );
        if (script == null) {
            return null;
        }
        return aria(script, script);
    }

    @Nullable
    private static ScriptCondition parseJsSection(ConfigurationSection section) {
        String script = firstNonBlank(
            section.getString("script"),
            resolveAlias(section, "expression", "script"),
            resolveAlias(section, "code", "script"),
            section.getString("js")
        );
        if (script == null) {
            return null;
        }
        return js(script, script);
    }

    public String serialize() {
        if (kind == ScriptConditionKind.JS) {
            String payload = script == null ? "" : script;
            // 新格式：js\tR:<script>（脚本中的 \t 转义为 \\t，保持可读）
            return "js\tR:" + escapeTab(payload);
        }
        if (kind == ScriptConditionKind.ARIA) {
            String payload = script == null ? "" : script;
            return "aria\tR:" + escapeTab(payload);
        }
        if (kind == ScriptConditionKind.CONTEXT) {
            return raw == null ? "" : raw;
        }
        String ph = placeholder == null ? "" : placeholder;
        String val = value == null ? "" : value;
        ScriptConditionOperator op = operator == null ? ScriptConditionOperator.EQ : operator;
        return ph + "\t" + op.configKey() + "\t" + val;
    }

    /** 将脚本中的 {@code \t} 转义为 {@code \\t}，避免与序列化分隔符冲突。 */
    private static String escapeTab(String text) {
        return text.replace("\t", "\\t");
    }

    /** 还原 {@code \\t} 为 {@code \t}。 */
    private static String unescapeTab(String text) {
        return text.replace("\\t", "\t");
    }

    @Nullable
    public static ScriptCondition deserialize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String trimmed = rawValue.trim();
        if (trimmed.startsWith("js\t")) {
            String payload = trimmed.substring(3);
            // 新格式 R: 前缀 → 直接可读
            if (payload.startsWith("R:")) {
                return js(unescapeTab(payload.substring(2)), trimmed);
            }
            // 旧格式：Base64 编码，向后兼容
            try {
                String script = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
                return js(script, trimmed);
            } catch (IllegalArgumentException exception) {
                return js(payload, trimmed);
            }
        }
        if (trimmed.startsWith("aria\t")) {
            String payload = trimmed.substring(5);
            if (payload.startsWith("R:")) {
                return aria(unescapeTab(payload.substring(2)), trimmed);
            }
            try {
                String script = new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
                return aria(script, trimmed);
            } catch (IllegalArgumentException exception) {
                return aria(payload, trimmed);
            }
        }
        ScriptCondition inline = parseInline(trimmed);
        if (inline != null) {
            return inline;
        }
        String[] parts = trimmed.split("\t", 3);
        if (parts.length < 3) {
            return null;
        }
        return papi(parts[0], ScriptConditionOperator.parse(parts[1]), parts[2], trimmed);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
