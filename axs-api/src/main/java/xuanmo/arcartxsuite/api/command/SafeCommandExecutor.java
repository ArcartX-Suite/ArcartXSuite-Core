package xuanmo.arcartxsuite.api.command;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * 安全命令执行器：用占位符模板 + 参数绑定替代字符串拼接，防止命令注入。
 * <p>
 * 使用方式：
 * <pre>{@code
 * SafeCommandExecutor.builder("kick {player} {reason}")
 *     .bindPlayerName("player", playerName)
 *     .bindQuoted("reason", reason)
 *     .execute(Bukkit.getConsoleSender());
 * }</pre>
 * <p>
 * 参数绑定类型：
 * <ul>
 *   <li>{@link #bindPlayerName(String, String)} — 验证玩家名只含 [a-zA-Z0-9_]，不合法直接拒绝</li>
 *   <li>{@link #bindQuoted(String, String)} — 自由文本用双引号包裹并转义内部引号</li>
 *   <li>{@link #bindRaw(String, String)} — 原样绑定（仅用于不含用户输入的固定参数）</li>
 * </ul>
 */
public final class SafeCommandExecutor {

    private final String template;
    private final Map<String, String> bindings = new LinkedHashMap<>();

    private SafeCommandExecutor(String template) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("命令模板不能为空");
        }
        this.template = template;
    }

    /**
     * 创建命令执行器构建器。
     *
     * @param template 命令模板，用 {@code {placeholder}} 标记参数位置
     * @return 构建器实例
     */
    public static SafeCommandExecutor builder(String template) {
        return new SafeCommandExecutor(template);
    }

    /**
     * 绑定玩家名参数：验证只含 [a-zA-Z0-9_]，长度 1-16，不合法抛异常。
     *
     * @param placeholder 模板中的占位符名（不含花括号）
     * @param playerName  玩家名
     * @return this
     */
    public SafeCommandExecutor bindPlayerName(String placeholder, String playerName) {
        bindings.put(placeholder, CommandSanitizer.validatePlayerName(playerName));
        return this;
    }

    /**
     * 绑定自由文本参数：用双引号包裹并转义内部引号和反斜杠。
     *
     * @param placeholder 模板中的占位符名（不含花括号）
     * @param text        自由文本
     * @return this
     */
    public SafeCommandExecutor bindQuoted(String placeholder, String text) {
        bindings.put(placeholder, CommandSanitizer.quote(text));
        return this;
    }

    /**
     * 绑定原始参数：不做任何转义，仅用于不含用户输入的固定参数。
     *
     * @param placeholder 模板中的占位符名（不含花括号）
     * @param value       固定值
     * @return this
     */
    public SafeCommandExecutor bindRaw(String placeholder, String value) {
        bindings.put(placeholder, value == null ? "" : value);
        return this;
    }

    /**
     * 构建最终命令字符串（替换占位符为绑定值）。
     *
     * @return 替换后的命令字符串
     */
    public String build() {
        String result = template;
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * 构建并执行命令。
     *
     * @param sender 命令发送者（通常为 {@code Bukkit.getConsoleSender()}）
     * @return {@code true} 如果命令被成功分发
     */
    public boolean execute(CommandSender sender) {
        String command = build();
        return Bukkit.dispatchCommand(sender, command);
    }
}
