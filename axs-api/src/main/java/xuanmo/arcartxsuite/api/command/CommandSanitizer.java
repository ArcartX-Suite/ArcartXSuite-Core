package xuanmo.arcartxsuite.api.command;

/**
 * 命令注入防护工具：玩家名验证、自由文本引号包裹、命令分隔符检测。
 * <p>
 * Bukkit 的 {@code dispatchCommand} 按空格分割参数，虽然不会像 shell 一样执行多条命令，
 * 但离线服可注册含特殊字符的玩家名，改变命令参数结构；若服务器装了支持 {@code ;} 分隔多命令的插件，
 * 则可注入任意命令。本工具从源头拒绝非法输入，不依赖具体服务端行为。
 */
public final class CommandSanitizer {

    private CommandSanitizer() {}

    /** 玩家名合法字符集：字母、数字、下划线（Bukkit 原版玩家名限制） */
    private static final java.util.regex.Pattern PLAYER_NAME_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    /** 命令分隔符：分号、管道符、换行符、&&、|| */
    private static final java.util.regex.Pattern COMMAND_SEPARATORS =
        java.util.regex.Pattern.compile("[;|\\n\\r]|&&|\\|\\|");

    /**
     * 验证玩家名只含 {@code [a-zA-Z0-9_]}，长度 1-16。
     *
     * @param name 待验证的玩家名
     * @return 验证通过的玩家名
     * @throws IllegalArgumentException 玩家名不合法时抛出
     */
    public static String validatePlayerName(String name) {
        if (name == null || !PLAYER_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("非法玩家名: " + name);
        }
        return name;
    }

    /**
     * 对自由文本用双引号包裹，转义内部双引号和反斜杠。
     * <p>
     * 例如 {@code hello"world} → {@code "hello\"world"}
     *
     * @param text 待包裹的自由文本
     * @return 用双引号包裹并转义后的文本
     */
    public static String quote(String text) {
        if (text == null) {
            return "\"\"";
        }
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * 检测字符串是否包含命令分隔符（{@code ;}, {@code |}, {@code &&}, {@code ||}, 换行符）。
     *
     * @param text 待检测的字符串
     * @return {@code true} 如果包含命令分隔符
     */
    public static boolean containsCommandSeparators(String text) {
        return text != null && COMMAND_SEPARATORS.matcher(text).find();
    }

    /**
     * 清理命令分隔符：将命令分隔符替换为空格。
     * <p>
     * 用于无法用引号包裹的场景（如命令模板本身不允许引号）。
     *
     * @param text 待清理的字符串
     * @return 清理后的字符串
     */
    public static String stripCommandSeparators(String text) {
        if (text == null) {
            return "";
        }
        return COMMAND_SEPARATORS.matcher(text).replaceAll(" ");
    }
}
