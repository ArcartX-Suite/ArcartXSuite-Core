package xuanmo.arcartxsuite.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.account.AccountTypeService;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.bridge.ClientBridgeAPI;
import xuanmo.arcartxsuite.api.bridge.ItemBridgeAPI;
import xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI;
import xuanmo.arcartxsuite.api.bridge.VanillaItemNameBridge;
import xuanmo.arcartxsuite.api.attribute.AttributeBridgeRegistry;
import xuanmo.arcartxsuite.api.condition.ScriptConditionEvaluator;
import xuanmo.arcartxsuite.api.crossserver.CrossServerAPI;
import xuanmo.arcartxsuite.api.currency.CurrencyBridgeAPI;
import xuanmo.arcartxsuite.api.currency.RondoBridge;
import xuanmo.arcartxsuite.api.item.ItemMatcherAPI;
import xuanmo.arcartxsuite.api.item.ItemSourceRegistry;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderExpansionRegistry;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderResolverAPI;
import xuanmo.arcartxsuite.api.security.PacketGuardAPI;
import xuanmo.arcartxsuite.api.script.AriaBridge;
import xuanmo.arcartxsuite.api.storage.StorageManager;

/**
 * 宿主提供给模块的上下文接口。
 * <p>
 * 模块通过此接口获取基础设施能力（桥接、文件、UI 注册等），
 * 而无需直接引用宿主插件主类。
 */
public interface ModuleContext {

    // ─── 基础设施 ──────────────────────────────────────────────

    /** 宿主插件实例（用于注册事件、调度任务） */
    JavaPlugin plugin();

    /** 带模块前缀的 Logger */
    Logger logger();

    /** 模块私有数据目录（plugins/ArcartXSuite/data/<moduleId>/） */
    File dataFolder();

    /** UI 文件输出目录（plugins/ArcartXSuite/ui/） */
    File uiFolder();

    // ─── ArcartX 桥接（类型安全 API） ────────────────────────────

    /** 获取 ArcartX UI/Packet 桥接（可能为 null，如桥接初始化失败） */
    @ApiStability.Stable
    @Nullable PacketBridgeAPI packetBridge();

    /** 获取 ArcartX Client 桥接（可能为 null） */
    @ApiStability.Stable
    @Nullable ClientBridgeAPI clientBridge();

    /** 获取 ArcartX ItemStack 桥接（可能为 null） */
    @ApiStability.Stable
    @Nullable ItemBridgeAPI itemStackBridge();

    // ─── 全局桥接（物品/经济/匹配） ─────────────────────────────

    /** 获取全局物品来源注册表（统一 Mythic/Neige/Overture/MMOItems 桥接） */
    @ApiStability.Stable
    ItemSourceRegistry itemSourceRegistry();

    /**
     * 获取原版物品中文名称解析桥接（永不为 null）。
     * <p>
     * 用于显示原版物品名、入库搜索文本、聊天物品预览等场景，
     * 避免各模块直接显示英文材质名。
     *
     * @since 1.3.3
     */
    @ApiStability.Stable
    @NotNull VanillaItemNameBridge vanillaItemNameBridge();

    /** 获取全局物品匹配器 */
    @ApiStability.Stable
    ItemMatcherAPI itemMatcher();

    /** 获取全局货币管理器 */
    @ApiStability.Stable
    CurrencyBridgeAPI currencyManager();

    /** 获取 Rondo 经济系统桥接（排行榜/转账/快照/日志），Rondo 未安装时 available() 为 false */
    @ApiStability.Stable
    @NotNull RondoBridge rondoBridge();

    /** 获取全局属性桥接注册表（统一 AttributePlus/CraneAttribute/MythicLib/Symphony 桥接） */
    @ApiStability.Stable
    AttributeBridgeRegistry attributeBridge();

    /** 获取 ArcartX 内置 Aria 脚本桥接（不可用时 available() 为 false） */
    @ApiStability.Stable
    @NotNull AriaBridge ariaBridge();

    /** 获取统一条件评估器（PlaceholderAPI + Aria 脚本） */
    @ApiStability.Stable
    @NotNull ScriptConditionEvaluator scriptConditionEvaluator();

    /** TACZ（创世战术武器）兼容桥接是否已激活。模块可通过此查询判断 TACZ 伤害转发是否生效。 */
    @ApiStability.Stable
    boolean taczActive();

    /** 获取 ArcartX WorldTexture 文字贴图桥接（可能为 null） */
    @ApiStability.Internal
    @Nullable xuanmo.arcartxsuite.api.bridge.WorldTextureBridgeAPI worldTextureBridge();

    /** 创建新的 ArcartX 路标桥接实例（模块独立管理生命周期） */
    @ApiStability.Internal
    @NotNull xuanmo.arcartxsuite.api.bridge.WaypointBridgeAPI createWaypointBridge();

    /** 创建新的 Adyeshach NPC 桥接实例（模块独立管理生命周期） */
    @ApiStability.Internal
    @NotNull xuanmo.arcartxsuite.api.bridge.AdyeshachNpcBridgeAPI createAdyeshachNpcBridge();

    /** 创建新的 ArcartX 音效播放器桥接实例（模块独立管理生命周期） */
    @ApiStability.Internal
    @NotNull xuanmo.arcartxsuite.api.bridge.SoundPlayerBridgeAPI createSoundPlayerBridge();

    // ─── 模块间通信 ───────────────────────────────────────────

    // 模块间通信统一通过 Capability 机制（registerCapability / getCapability），
    // 不再支持按模块 ID 或类直接查找模块实例。

    // ─── 安全 ─────────────────────────────────────────────────

    /** 获取客户端包频率限制器（可能为 null） */
    @ApiStability.Stable
    @Nullable PacketGuardAPI packetGuard();

    /**
     * 获取宿主统一账号识别服务（微软正版 / LittleSkin / 离线判定）。
     * <p>
     * 由本体实现，供 loginview / qqbot / eventpacket 等模块共享，永不为 null。
     * 取代各模块自行实现且不一致的账号判定逻辑。
     */
    @ApiStability.Stable
    @NotNull AccountTypeService accountTypeService();

    /**
     * 宿主统一跨服传输（Redis + Proxy 双后端、统一信封与 HMAC）。
     */
    @ApiStability.Stable
    @NotNull CrossServerAPI crossServer();

    // ─── 配置与资源工具 ───────────────────────────────────────

    /**
     * 从模块 Jar 中读取资源。
     *
     * @param resourcePath 资源路径（相对于模块 Jar 根目录）
     * @param loader       模块的 ClassLoader
     * @return 输入流，资源不存在时返回 null
     */
    InputStream openResource(String resourcePath, ClassLoader loader);

    /**
     * 导出模块内置资源到目标文件。
     *
     * @param resourcePath 资源路径
     * @param target       目标文件
     * @param overwrite    是否覆盖已有文件
     */
    void exportResource(String resourcePath, File target, boolean overwrite);

    /**
     * 准备 ArcartX UI 绑定（注册 UI 文件到 ArcartX）。
     *
     * @param moduleName        模块名（日志用）
     * @param configuredUiId    配置中的 UI id
     * @param registerOnEnable  是否自动注册
     * @param uiFile            UI YAML 文件
     * @return UI 绑定结果，失败时返回 null
     */
    UiBinding prepareUiBinding(String moduleName, String configuredUiId, boolean registerOnEnable, File uiFile);

    /**
     * 检查指定的外部 Bukkit 插件是否已安装。
     */
    boolean hasPlugin(String pluginName);

    // ─── 宿主级资源 ────────────────────────────────────────────

    /** 宿主插件数据目录（plugins/ArcartXSuite/），模块配置文件仍放在此处以保持用户习惯 */
    File pluginDataFolder();

    /** 获取 ArcartX Prop 桥接（可能为 null） */
    @ApiStability.Internal
    @Nullable xuanmo.arcartxsuite.api.bridge.PropBridgeAPI propBridge();

    /**
     * 获取宿主统一数据源管理器（永不为 null）。
     * <p>
     * 由本体在启动时创建唯一一个 HikariCP 连接池并注入给所有模块。
     * 模块通过它复用本体连接池，无需自行管理 {@code initialize()}/{@code shutdown()}。
     * <p>
     * 若模块在自身配置中声明了独立的 storage 节（覆盖模式），
     * 可不使用此管理器而走自建模式（向后兼容）。
     *
     * @return 宿主数据源管理器
     * @since 1.4.0
     */
    @ApiStability.Stable
    @NotNull StorageManager storageManager();

    // ─── 事件与命令注册 ────────────────────────────────────────

    /**
     * 注册 Bukkit 事件监听器，由宿主管理生命周期。
     * 调用 {@link #unregisterListeners()} 或模块 onDisable 时自动注销。
     *
     * @param listener 事件监听器
     */
    void registerListener(Listener listener);

    /**
     * 注销当前模块注册的所有事件监听器。
     */
    void unregisterListeners();

    /**
     * 延迟绑定玩家命令。将 executor 绑定到 plugin.yml 中已声明的命令名。
     * 模块 onDisable 时自动解绑。
     *
     * @param commandName 命令名（必须在 plugin.yml 中已声明）
     * @param executor    命令执行器
     */
    void registerCommand(String commandName, TabExecutor executor);

    // ─── PlaceholderAPI ────────────────────────────────────────

    /**
     * 获取统一 PlaceholderAPI 解析器（永不为 null）。
     * <p>
     * 未安装 PlaceholderAPI 时返回 no-op 实现，原样返回输入字符串。
     */
    @ApiStability.Stable
    @NotNull
    PlaceholderResolverAPI placeholderResolver();

    /**
     * 获取模块级 PlaceholderAPI 扩展注册表（永不为 null）。
     * <p>
     * 模块通过它注册/注销自己的 {@code PlaceholderExpansion}，
     * 模块 onDisable 时自动注销本注册表下所有扩展。
     */
    @ApiStability.Stable
    @NotNull
    PlaceholderExpansionRegistry expansionRegistry();

    // ─── 客户端事件路由 ────────────────────────────────────────

    /**
     * 注册客户端自定义包处理器，使用默认优先级 (0)。
     *
     * @param handler 包处理器
     */
    void registerClientPacketHandler(ClientPacketHandler handler);

    /**
     * 注册客户端自定义包处理器，指定优先级。
     * 数值越小越优先处理，越大越靠后。
     *
     * @param handler  包处理器
     * @param priority 优先级（默认 0，EventPacket 建议使用 100）
     */
    void registerClientPacketHandler(ClientPacketHandler handler, int priority);

    /**
     * Registers a packet handler with optional route-layer ownership metadata.
     *
     * @param handler packet handler
     * @param priority handler priority
     * @param packetId owned packet ID, or {@code null} for legacy registration
     * @param guardModule canonical PacketGuard module key, or {@code null} for the module ID
     */
    default void registerClientPacketHandler(
        ClientPacketHandler handler,
        int priority,
        String packetId,
        String guardModule
    ) {
        registerClientPacketHandler(handler, priority);
    }

    /**
     * 注册客户端初始化完成处理器。
     *
     * @param handler 初始化处理器
     */
    void registerClientInitializedHandler(ClientInitializedHandler handler);

    // ─── 模块级资源导出 ────────────────────────────────────────

    /**
     * 从模块 Jar 导出 UI 资源到宿主 ui/ 目录。
     *
     * @param resourcePath 模块 Jar 内的资源路径
     * @param relativeUiPath 相对于宿主数据目录的 UI 文件路径（如 "ui/login_view.yml"）
     * @param overwrite    是否覆盖已有文件
     * @param loader       模块的 ClassLoader
     * @return 导出后的文件
     * @throws IOException 导出失败时抛出
     */
    File exportUiResource(String resourcePath, String relativeUiPath, boolean overwrite, ClassLoader loader) throws IOException;

    /**
     * 从模块 Jar 导出配置文件到宿主数据目录。
     *
     * @param resourcePath       模块 Jar 内的资源路径
     * @param targetRelativePath 相对于宿主数据目录的目标路径
     * @param overwrite          是否覆盖已有文件
     * @param loader             模块的 ClassLoader
     * @return 导出后的文件
     */
    File exportConfigResource(String resourcePath, String targetRelativePath, boolean overwrite, ClassLoader loader);

    /**
     * 注销指定的 ArcartX UI（根据注册 id）。
     *
     * @param registeredUiId 之前注册时获得的 UI id
     */
    void unregisterUi(@Nullable String registeredUiId);

    // ─── 全局按键订阅 ─────────────────────────────────────────

    /**
     * 注册按键事件处理器。
     * <p>
     * 宿主统一注册 ArcartX 客户端按键（在 config.yml keybinds 节定义），
     * 模块通过此方法订阅感兴趣的按键回调。
     * <p>
     * 优先级数值越小越先处理；处理器返回 {@code true} 表示已消费，后续不再分发。
     *
     * @param keyName  按键注册名（如 {@code "AXS_INTERACT"}），须与 config.yml keybinds 中的 name 匹配
     * @param priority 优先级（越小越先，建议: conversation=10, pickup=50）
     * @param handler  处理器
     */
    @ApiStability.Stable
    void registerKeybindHandler(String keyName, int priority, KeybindHandler handler);

    // ─── Capability 跨模块通信 ────────────────────────────────

    /**
     * 注册当前模块提供的能力接口实例。
     * 其他模块通过 {@link #getCapability(Class)} 按类型查找。
     * 模块 onDisable 时自动注销。
     *
     * @param capabilityType  能力接口类（如 {@code MailDispatchable.class}）
     * @param implementation  实现实例
     * @param <T>             能力接口类型
     */
    @ApiStability.Stable
    <T> void registerCapability(Class<T> capabilityType, T implementation);

    /**
     * 按类型查找其他模块注册的能力接口实例。
     *
     * @param capabilityType 能力接口类
     * @return 能力实例，未注册时返回 null
     * @param <T>            能力接口类型
     */
    @ApiStability.Stable
    @Nullable <T> T getCapability(Class<T> capabilityType);
}
