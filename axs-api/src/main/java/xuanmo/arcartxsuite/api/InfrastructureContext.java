package xuanmo.arcartxsuite.api;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 基础设施上下文：提供宿主插件实例、日志、数据目录等基础能力。
 *
 * @since 1.5.0
 */
public interface InfrastructureContext {

    /** 宿主插件实例（用于注册事件、调度任务） */
    JavaPlugin plugin();

    /** 带模块前缀的 Logger */
    Logger logger();

    /** 模块私有数据目录（plugins/ArcartXSuite/data/<moduleId>/） */
    File dataFolder();

    /** UI 文件输出目录（plugins/ArcartXSuite/ui/） */
    File uiFolder();

    /** 宿主插件数据目录（plugins/ArcartXSuite/），模块配置文件仍放在此处以保持用户习惯 */
    File pluginDataFolder();
}
