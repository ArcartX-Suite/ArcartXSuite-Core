package xuanmo.arcartxsuite.api.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * 脚本条件加载器，从 YAML 配置节解析出 {@link ScriptCondition} 列表。
 * <p>
 * 支持三种配置形态：内联字符串列表、Map 列表、嵌套配置节。提供针对常见键名
 * （open-requirements / requirements / use-conditions / conditions / claim-conditions）
 * 的便捷方法。
 */
public final class ScriptConditionsLoader {

    private ScriptConditionsLoader() {
    }

    /**
     * 从指定配置节的多个键中加载条件列表。
 * <p>
 * 每个键会依次尝试：内联字符串列表、Map 列表、嵌套配置节三种解析方式。
     *
     * @param section 配置节，null 返回空列表
     * @param keys    要读取的键名数组
     * @return 不可变条件列表
     */
    public static @NotNull List<ScriptCondition> load(
        ConfigurationSection section,
        String... keys
    ) {
        if (section == null || keys == null || keys.length == 0) {
            return List.of();
        }
        List<ScriptCondition> conditions = new ArrayList<>();
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            appendInlineLines(conditions, section.getStringList(key));
            appendMapList(conditions, section.getMapList(key));
            ConfigurationSection nested = section.getConfigurationSection(key);
            if (nested != null) {
                appendInlineLines(conditions, nested.getStringList("list"));
                for (String childKey : nested.getKeys(false)) {
                    ConfigurationSection child = nested.getConfigurationSection(childKey);
                    if (child != null) {
                        ScriptCondition mapped = ScriptCondition.fromSection(child);
                        if (mapped != null) {
                            conditions.add(mapped);
                        }
                    }
                }
            }
        }
        return List.copyOf(conditions);
    }

    /** 加载 {@code open-requirements} 键下的条件列表。 */
    public static @NotNull List<ScriptCondition> loadOpenRequirements(
        ConfigurationSection section
    ) {
        return load(section, "open-requirements");
    }

    /** 加载 {@code requirements} 键下的条件列表（查看条件）。 */
    public static @NotNull List<ScriptCondition> loadViewConditions(
        ConfigurationSection section
    ) {
        return load(section, "requirements");
    }

    /** 加载 {@code use-conditions} 键下的条件列表（使用条件）。 */
    public static @NotNull List<ScriptCondition> loadUseConditions(
        ConfigurationSection section
    ) {
        return load(section, "use-conditions");
    }

    /** 加载 {@code conditions} 键下的条件列表（模块级条件）。 */
    public static @NotNull List<ScriptCondition> loadModuleConditions(
        ConfigurationSection section
    ) {
        return load(section, "conditions");
    }

    /**
     * 加载 {@code conditions} 键下的条件列表，并在解析结果为空但原始数据存在时
     * 记录警告日志（用于提示用户配置格式错误）。
     *
     * @param section       配置节
     * @param logger        日志器，null 时不记录
     * @param invalidPrefix 警告前缀文本
     * @return 不可变条件列表
     */
    public static @NotNull List<ScriptCondition> loadModuleConditions(
        ConfigurationSection section,
        Logger logger,
        String invalidPrefix
    ) {
        List<ScriptCondition> conditions = loadModuleConditions(section);
        if (conditions.isEmpty() && logger != null && section != null) {
            List<?> rawConditions = section.getList("conditions");
            if (rawConditions != null) {
                for (Object rawCondition : rawConditions) {
                    String line = rawCondition == null
                        ? ""
                        : String.valueOf(rawCondition).trim();
                    if (!line.isBlank()) {
                        logger.warning(invalidPrefix + line);
                    }
                }
            }
        }
        return conditions;
    }

    /** 加载 {@code claim-conditions} 键下的条件列表（领取条件）。 */
    public static @NotNull List<ScriptCondition> loadClaimConditions(
        ConfigurationSection section
    ) {
        return load(section, "claim-conditions");
    }

    /**
     * 将内联字符串行列表解析为条件列表，每行尝试内联解析或反序列化。
     *
     * @param lines 原始行列表，null/空返回空列表
     * @return 不可变条件列表
     */
    public static @NotNull List<ScriptCondition> loadInlineLines(
        List<String> lines
    ) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }
        List<ScriptCondition> conditions = new ArrayList<>();
        appendInlineLines(conditions, lines);
        return List.copyOf(conditions);
    }

    private static void appendInlineLines(
        List<ScriptCondition> target,
        List<String> lines
    ) {
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            ScriptCondition condition = ScriptCondition.parseInline(line);
            if (condition == null) {
                condition = ScriptCondition.deserialize(line);
            }
            if (condition != null) {
                target.add(condition);
            }
        }
    }

    private static void appendMapList(
        List<ScriptCondition> target,
        List<Map<?, ?>> maps
    ) {
        if (maps == null) {
            return;
        }
        for (Map<?, ?> map : maps) {
            if (map == null || map.isEmpty()) {
                continue;
            }
            MemoryConfiguration memory = new MemoryConfiguration();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    memory.set(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            ScriptCondition inline = ScriptCondition.parseInline(
                memory.getString("expr", memory.getString("expression", ""))
            );
            if (inline != null) {
                target.add(inline);
                continue;
            }
            ScriptCondition structured = ScriptCondition.fromSection(memory);
            if (structured != null) {
                target.add(structured);
            }
        }
    }
}
