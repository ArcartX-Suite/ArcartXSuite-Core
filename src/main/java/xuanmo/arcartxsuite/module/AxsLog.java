package xuanmo.arcartxsuite.module;

import java.util.logging.Logger;

/**
 * 全局共享的样式化 Logger 入口。
 * <p>
 * 本体的桥接/服务类通常持有的是宿主薄壳 {@code ArcartXSuitePlugin}（Bukkit 默认
 * Logger，前缀为 {@code [ArcartX-Suite]}）。为让全部控制台输出统一成
 * {@code ◆ ArcartXSuite | <LEVEL>: ...} 前缀，这些地方应改用本入口返回的
 * {@link PluginConsoleLogger}，而非 {@code plugin.getLogger()}。
 * <p>
 * 注意：引导期（控制台尚未就绪时）仍保留 JUL。
 */
public final class AxsLog {

    private static final Logger SHARED = new PluginConsoleLogger("ArcartXSuite", null);

    private AxsLog() {
    }

    /** 返回统一前缀的共享 Logger。 */
    public static Logger logger() {
        return SHARED;
    }
}
