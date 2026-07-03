package xuanmo.arcartxsuite.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.config.ConfigSyncSpec;
import xuanmo.arcartxsuite.api.config.ModuleConfigSpec;
import xuanmo.arcartxsuite.api.config.SyncPolicy;
import xuanmo.arcartxsuite.api.config.ValidationRule;
import xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI;
import xuanmo.arcartxsuite.api.message.MessageProvider;

/**
 * 可插拔模块的抽象基类，封装通用生命周期管理。
 * <p>
 * 子类通过覆写声明式方法（{@link #configFileName()}, {@link #uiResourceMappings()} 等）
 * 和实现抽象方法（{@link #loadConfiguration}, {@link #startService}, {@link #stopService}）
 * 来定义模块行为。基类自动处理配置导出、UI 绑定、监听器注册、命令绑定、
 * PAPI 注册、客户端包路由注册和 shutdown 清理。
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * public class MyModule extends AbstractAXSModule {
 *     private MyConfig config;
 *     private MyService service;
 *
 *     @Override public ModuleDescriptor descriptor() { ... }
 *     @Override protected String configFileName() { return "ArcartXMy.yml"; }
 *     @Override protected void loadConfiguration(File f) { config = MyConfig.load(f); }
 *     @Override protected void startService() { service = new MyService(context, config); service.start(); }
 *     @Override protected void stopService() { if (service != null) { service.shutdown(); service = null; } }
 * }
 * }</pre>
 */
public abstract class AbstractAXSModule implements AXSModule {

    /** 模块上下文，在 {@link #onEnable} 时注入（私有，子类通过 protected 字段访问具体 API） */
    private ModuleContext context;

    // ── 注入的 API 字段（子类直接使用，无需通过 context 获取） ──────
    protected JavaPlugin plugin;
    protected Logger logger;
    protected File dataFolder;
    protected @Nullable xuanmo.arcartxsuite.api.bridge.PacketBridgeAPI packetBridge;
    protected @Nullable xuanmo.arcartxsuite.api.bridge.ClientBridgeAPI clientBridge;
    protected @Nullable xuanmo.arcartxsuite.api.bridge.ItemBridgeAPI itemStackBridge;
    protected xuanmo.arcartxsuite.api.item.ItemSourceRegistry itemSourceRegistry;
    protected xuanmo.arcartxsuite.api.item.ItemMatcherAPI itemMatcher;
    protected xuanmo.arcartxsuite.api.currency.CurrencyBridgeAPI currencyManager;
    protected xuanmo.arcartxsuite.api.attribute.AttributeBridgeRegistry attributeBridge;
    protected @NotNull xuanmo.arcartxsuite.api.script.AriaBridge ariaBridge;
    protected @NotNull xuanmo.arcartxsuite.api.condition.ScriptConditionEvaluator scriptConditionEvaluator;
    protected @Nullable xuanmo.arcartxsuite.api.security.PacketGuardAPI packetGuard;
    protected @NotNull xuanmo.arcartxsuite.api.account.AccountTypeService accountTypeService;
    protected @NotNull xuanmo.arcartxsuite.api.crossserver.CrossServerAPI crossServer;
    protected @Nullable xuanmo.arcartxsuite.api.bridge.WorldTextureBridgeAPI worldTextureBridge;
    protected @NotNull xuanmo.arcartxsuite.api.placeholder.PlaceholderResolverAPI placeholderResolver;
    protected @NotNull xuanmo.arcartxsuite.api.placeholder.PlaceholderExpansionRegistry expansionRegistry;
    protected @Nullable xuanmo.arcartxsuite.api.bridge.PropBridgeAPI propBridge;
    protected File pluginDataFolder;


    private boolean ready;
    private boolean reloading; // reload 期间跳过 UI 注销，避免客户端丢失 HUD
    private final Map<String, UiBinding> uiBindings = new LinkedHashMap<>();
    private MessageProvider messages; // 模块消息提供者，由 messagesFileName() 声明后自动加载

    // ── 声明式元数据（子类按需覆写） ──────────────────────────

    /**
     * 默认配置文件名（如 "ArcartXLoginView.yml"）。
     * 返回 null 表示模块无独立配置文件。
     * <p>
     * 配置文件从模块 Jar 导出到宿主数据目录（plugins/ArcartXSuite/）。
     */
    @Nullable
    protected String configFileName() {
        return null;
    }

    /**
     * 模块消息文件名（如 {@code "messages.yml"}）。
     * 返回 null 表示模块不使用外部化消息（仍可硬编码）。
     * <p>
     * 声明后，基类在 {@link #onEnable} 时自动从模块 Jar 导出该文件到
     * {@code data/<moduleId>/<fileName>}，并加载为 {@link #messages()}。
     * 用户可编辑该文件自定义所有文本，支持 {@code &} 颜色码和 {@code {0}} 占位符。
     */
    @Nullable
    protected String messagesFileName() {
        return null;
    }

    /**
     * 主配置 yml 的同步策略。默认 {@link SyncPolicy#strict() strict}：
     * 玩家 yml 中不在内置默认值的键都会被视为废弃。
     * <p>
     * 子类若存在用户可自由增删的"动态节点"（如 announcer.entries、warehouse.warehouses），
     * 应覆写此方法返回 {@code SyncPolicy.builder().dynamicSection("xxx").build()}。
     */
    @NotNull
    protected SyncPolicy defaultSyncPolicy() {
        return SyncPolicy.strict();
    }

    /**
     * 主配置 yml 的内置版本号。默认 1。
     * <p>
     * 若有破坏性字段重命名 / 删除，请把版本号 +1，并在模块 jar 内
     * {@code <migrationFolder>/<from>-<to>.yml} 写迁移规则。
     */
    protected int currentConfigVersion() {
        return 1;
    }

    /**
     * 主配置版本号字段路径。默认 {@code "config-version"}。
     */
    @NotNull
    protected String configVersionPath() {
        return "config-version";
    }

    /**
     * 模块 jar 内迁移文件夹路径。默认 {@code "migrations"}。
     * 返回空字符串表示该模块不参与版本迁移。
     */
    @NotNull
    protected String migrationFolder() {
        return "migrations";
    }

    /**
     * 主配置的校验规则。默认空列表。
     */
    @NotNull
    protected List<ValidationRule> mainConfigValidations() {
        return List.of();
    }

    /**
     * 附属配置规约（如 chat 模块的 chat/channels/*.yml、prop 模块的 prop/key.yml 等）。
     * 默认空列表。子类按需覆写。
     */
    @NotNull
    protected List<ModuleConfigSpec> additionalConfigSpecs() {
        return List.of();
    }

    /**
     * 默认实现：基于 {@link #configFileName()} 与上面的钩子组合出主 yml 的诊断规约，
     * 并追加 {@link #additionalConfigSpecs()}。子类一般无需覆写。
     */
    @Override
    public List<ModuleConfigSpec> configSpecs() {
        List<ModuleConfigSpec> specs = new ArrayList<>();
        String fileName = configFileName();
        if (fileName != null && !fileName.isBlank()) {
            String moduleId = descriptor().id();
            String targetRelative = "data/" + moduleId + "/config.yml";
            specs.add(new ModuleConfigSpec(
                moduleId,
                new ConfigSyncSpec(fileName, targetRelative, defaultSyncPolicy()),
                currentConfigVersion(),
                configVersionPath(),
                migrationFolder(),
                mainConfigValidations()
            ));
        }
        specs.addAll(additionalConfigSpecs());
        return List.copyOf(specs);
    }

    /**
     * UI 资源映射：模块 Jar 内的资源路径 → 宿主数据目录下的相对输出路径。
     * <p>
     * 示例: {@code Map.of("arcartx/ui/login_view.yml", "ui/login_view.yml")}
     *
     * @return 资源映射，空 Map 表示无 UI 资源
     */
    @NotNull
    protected Map<String, String> uiResourceMappings() {
        return Map.of();
    }

    /**
     * 是否覆写已有的 UI 文件。默认 false。
     * 子类可在 {@link #loadConfiguration} 之后根据配置动态返回。
     */
    protected boolean overwriteUiFiles() {
        return false;
    }

    /**
     * 创建模块需要的 Bukkit 事件监听器列表。
     * 返回的监听器将由基类在 {@link #onEnable} 时自动注册，
     * {@link #onDisable} 时自动注销。
     */
    @NotNull
    protected List<Listener> createListeners() {
        return List.of();
    }

    /**
     * 模块需要绑定的独立玩家命令：命令名 → Executor。
     * 命令名必须在 plugin.yml 中已声明。
     * <p>
     * 示例: {@code Map.of("title", new TitlePlayerCommand(this))}
     */
    @NotNull
    protected Map<String, TabExecutor> commandBindings() {
        return Map.of();
    }

    /**
     * 创建 PlaceholderAPI 占位符扩展实例。返回 null 表示不注册占位符。
     */
    @Nullable
    protected Object createPlaceholderExpansion() {
        return null;
    }

    /**
     * 创建客户端自定义包处理器。返回 null 表示不处理客户端回包。
     */
    @Nullable
    protected ClientPacketHandler createPacketHandler() {
        return null;
    }

    /**
     * 客户端包处理器优先级。数值越小越优先，越大越靠后。
     * 默认 0。EventPacket 模块建议使用 100。
     */
    protected int packetHandlerPriority() {
        return 0;
    }

    /**
     * 创建客户端初始化完成处理器。返回 null 表示不需要客户端初始化通知。
     */
    @Nullable
    protected ClientInitializedHandler createInitializedHandler() {
        return null;
    }

    // ── 模块必须实现的抽象方法 ──────────────────────────────────

    /**
     * 加载配置。在配置文件已确保存在之后调用。
     * 子类在此方法中解析配置文件并缓存配置对象。
     *
     * @param configFile 配置文件（如果 {@link #configFileName()} 返回 null 则此参数为 null）
     * @throws Exception 配置加载失败时抛出
     */
    protected abstract void loadConfiguration(@Nullable File configFile) throws Exception;

    /**
     * 创建并启动模块服务。
     * 在配置加载、UI 绑定完成之后调用。
     *
     * @throws Exception 启动失败时抛出
     */
    protected abstract void startService() throws Exception;

    /**
     * 关闭模块服务并释放资源。
     * 在监听器、命令、占位符等自动注销之前调用。
     */
    protected abstract void stopService();

    // ── 基类自动处理的生命周期 ─────────────────────────────────

    @Override
    public final boolean onEnable(ModuleContext context) throws Exception {
        this.context = context;

        // 注入 API 字段（子类通过 protected 字段直接访问，无需 context）
        this.plugin = context.plugin();
        this.logger = context.logger();
        this.dataFolder = context.dataFolder();
        this.pluginDataFolder = context.pluginDataFolder();
        this.packetBridge = context.packetBridge();
        this.clientBridge = context.clientBridge();
        this.itemStackBridge = context.itemStackBridge();
        this.itemSourceRegistry = context.itemSourceRegistry();
        this.itemMatcher = context.itemMatcher();
        this.currencyManager = context.currencyManager();
        this.attributeBridge = context.attributeBridge();
        this.ariaBridge = context.ariaBridge();
        this.scriptConditionEvaluator = context.scriptConditionEvaluator();
        this.packetGuard = context.packetGuard();
        this.accountTypeService = context.accountTypeService();
        this.crossServer = context.crossServer();
        this.worldTextureBridge = context.worldTextureBridge();
        this.placeholderResolver = context.placeholderResolver();
        this.expansionRegistry = context.expansionRegistry();
        this.propBridge = context.propBridge();

        Logger logger = context.logger();

        try {
            // 1. 导出并加载配置
            File configFile = ensureConfigExists();
            loadConfiguration(configFile);

            // 1b. 导出并加载外部化消息（若声明）
            initMessages();

            // 2. 导出 UI 资源并绑定
            exportAndBindUi();

            // 3. 绑定命令（提前到 startService 之前，使用 Supplier 延迟引用服务；
            //    即使服务启动失败，命令仍可注册并对玩家显示友好提示）
            commandBindings().forEach((name, executor) ->
                context.registerCommand(name, executor));

            // 4. 启动服务
            startService();

            // 5. 注册事件监听器
            for (Listener listener : createListeners()) {
                context.registerListener(listener);
            }

            // 6. 注册 PlaceholderAPI 占位符
            if (context.hasPlugin("PlaceholderAPI")) {
                try {
                    Object expansion = createPlaceholderExpansion();
                    if (expansion != null) {
                        context.expansionRegistry().register(expansion);
                    }
                } catch (LinkageError error) {
                    logger.warning(descriptor().name() + " PlaceholderAPI 占位符不可用，已跳过注册: " + error.getMessage());
                }
            } else {
                logger.fine(descriptor().name() + " 未检测到 PlaceholderAPI，跳过占位符注册。");
            }

            // 7. 注册客户端包处理器
            ClientPacketHandler packetHandler = createPacketHandler();
            if (packetHandler != null) {
                context.registerClientPacketHandler(packetHandler, packetHandlerPriority());
            }

            // 8. 注册客户端初始化处理器
            ClientInitializedHandler initHandler = createInitializedHandler();
            if (initHandler != null) {
                context.registerClientInitializedHandler(initHandler);
            }

            ready = true;
            return true;
        } catch (Exception exception) {
            logger.severe(descriptor().name() + " 模块启动失败: " + exception.getMessage());
            cleanupOnFailure();
            throw exception;
        } catch (LinkageError error) {
            logger.severe(descriptor().name() + " 模块启动失败，缺少运行依赖或依赖版本不兼容: " + error.getMessage());
            cleanupOnFailure();
            throw error;
        }
    }

    @Override
    public final void onDisable() {
        ready = false;
        // 先关闭业务服务
        try {
            stopService();
        } catch (Exception exception) {
            if (context != null) {
                context.logger().warning(descriptor().name() + " 模块关闭异常: " + exception.getMessage());
            }
        }
        // 注销 UI（registerOrReloadUi 在 re-enable 时会自动重新注册）
        for (UiBinding binding : uiBindings.values()) {
            if (binding.registeredUiId() != null && context != null) {
                context.unregisterUi(binding.registeredUiId());
            }
        }
        uiBindings.clear();
        // 自动注销所有已注册的监听器、命令、占位符等
        if (context != null) {
            context.unregisterListeners();
            context.expansionRegistry().unregisterAll();
        }
    }

    @Override
    public void onReload() throws Exception {
        reloading = true;
        try {
            onDisable();
            onEnable(context);
        } finally {
            reloading = false;
        }
    }

    @Override
    public final boolean isReady() {
        return ready;
    }

    // ── 子类可用的工具方法 ─────────────────────────────────────

    /**
     * 获取指定 UI 资源路径对应的绑定结果。
     *
     * @param relativeUiPath 相对于宿主数据目录的 UI 文件路径
     * @return UI 绑定，未找到时返回 null
     */
    @Nullable
    protected UiBinding getUiBinding(String relativeUiPath) {
        return uiBindings.get(relativeUiPath);
    }

    /**
     * 获取模块的 ClassLoader（用于资源加载）。
     */
    protected ClassLoader moduleClassLoader() {
        return getClass().getClassLoader();
    }

    /**
     * 获取模块消息提供者。仅当 {@link #messagesFileName()} 返回非空时可用。
     * <p>
     * 用于替代硬编码文本：{@code messages().get("key", arg0, arg1)}。
     *
     * @return 消息提供者；若模块未声明消息文件则返回 null
     */
    @Nullable
    protected MessageProvider messages() {
        return messages;
    }


    // ── 注入的 API 委托方法（子类直接调用，无需通过 context） ──────

    /** 按类型查找已加载的模块实例 */
    protected <T extends AXSModule> java.util.Optional<T> getModule(Class<T> moduleClass) {
        return context.getModule(moduleClass);
    }

    /** 按 id 查找已加载的模块实例 */
    protected java.util.Optional<AXSModule> getModule(String moduleId) {
        return context.getModule(moduleId);
    }

    /** 注册当前模块提供的能力接口 */
    protected <T> void registerCapability(Class<T> capabilityType, T implementation) {
        context.registerCapability(capabilityType, implementation);
    }

    /** 按类型查找其他模块注册的能力接口 */
    @Nullable
    protected <T> T getCapability(Class<T> capabilityType) {
        return context.getCapability(capabilityType);
    }

    /** 创建新的路标桥接实例 */
    @NotNull
    protected xuanmo.arcartxsuite.api.bridge.WaypointBridgeAPI createWaypointBridge() {
        return context.createWaypointBridge();
    }

    /** 创建新的 Adyeshach NPC 桥接实例 */
    @NotNull
    protected xuanmo.arcartxsuite.api.bridge.AdyeshachNpcBridgeAPI createAdyeshachNpcBridge() {
        return context.createAdyeshachNpcBridge();
    }

    /** 导出模块内置资源到目标文件 */
    protected void exportResource(String resourcePath, File target, boolean overwrite) {
        context.exportResource(resourcePath, target, overwrite);
    }

    /** 从模块 Jar 中读取受保护的资源 */
    protected InputStream openProtectedResource(String resourcePath, ClassLoader loader) {
        return context.openProtectedResource(resourcePath, loader);
    }

    /** 从模块 Jar 导出配置文件到宿主数据目录 */
    protected File exportConfigResource(String resourcePath, String targetRelativePath, boolean overwrite, ClassLoader loader) {
        return context.exportConfigResource(resourcePath, targetRelativePath, overwrite, loader);
    }

    /** 从模块 Jar 导出 UI 资源到宿主 ui/ 目录 */
    protected File exportUiResource(String resourcePath, String relativeUiPath, boolean overwrite, ClassLoader loader) throws IOException {
        return context.exportUiResource(resourcePath, relativeUiPath, overwrite, loader);
    }

    /** 检查指定的外部 Bukkit 插件是否已安装 */
    protected boolean hasPlugin(String pluginName) {
        return context.hasPlugin(pluginName);
    }

    /** 注册 Bukkit 事件监听器 */
    protected void registerListener(Listener listener) {
        context.registerListener(listener);
    }

    /** 延迟绑定玩家命令 */
    protected void registerCommand(String commandName, TabExecutor executor) {
        context.registerCommand(commandName, executor);
    }

    /** 注册按键事件处理器 */
    protected void registerKeybindHandler(String keyName, int priority, KeybindHandler handler) {
        context.registerKeybindHandler(keyName, priority, handler);
    }

    /** 准备 ArcartX UI 绑定 */
    @Nullable
    protected UiBinding prepareUiBinding(String moduleName, String configuredUiId, boolean registerOnEnable, File uiFile) {
        return context.prepareUiBinding(moduleName, configuredUiId, registerOnEnable, uiFile);
    }

    /** 注销指定的 ArcartX UI */
    protected void unregisterUi(@Nullable String registeredUiId) {
        context.unregisterUi(registeredUiId);
    }

    /** TACZ 兼容桥接是否已激活 */
    protected boolean taczActive() {
        return context.taczActive();
    }

    // ── 内部方法 ──────────────────────────────────────────────

    /**
     * 确保配置文件存在。
     * <p>
     * 新版本中模块配置统一落到 {@code plugins/ArcartXSuite/data/<moduleId>/config.yml}，
     * 新装服务器则直接从模块 Jar 导出默认配置。
     */
    @Nullable
    private File ensureConfigExists() {
        String fileName = configFileName();
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        File moduleDataFolder = context.dataFolder();
        File newConfigFile = new File(moduleDataFolder, "config.yml");

        // 首次启动或仍缺失：从模块 Jar 导出默认配置到 data/<moduleId>/config.yml
        if (!newConfigFile.exists()) {
            String relative = moduleDataFolder.getName() + "/config.yml";
            // exportConfigResource 第二个参数是相对 pluginDataFolder 的路径
            return context.exportConfigResource(
                fileName, "data/" + relative, false, moduleClassLoader());
        }
        return newConfigFile;
    }

    /**
     * 导出并加载外部化消息文件。
     * <p>
     * 与 {@link #ensureConfigExists()} 同样走 {@link ModuleContext#exportConfigResource}，
     * 以便正确解密付费模块的加密资源（.axb / .axl）。文件落到
     * {@code data/<moduleId>/<fileName>}，用户编辑后 reload 即可生效。
     */
    private void initMessages() {
        String fileName = messagesFileName();
        if (fileName == null || fileName.isBlank()) {
            messages = null;
            return;
        }
        File moduleDataFolder = context.dataFolder();
        File messagesFile = new File(moduleDataFolder, fileName);
        if (!messagesFile.exists()) {
            // 走宿主导出（处理加密资源解密），目标相对 pluginDataFolder
            context.exportConfigResource(
                fileName,
                "data/" + moduleDataFolder.getName() + "/" + fileName,
                false,
                moduleClassLoader()
            );
        }
        messages = new MessageProvider(moduleDataFolder, fileName, moduleClassLoader(), context.logger());
        messages.load();
        context.logger().fine(descriptor().name() + " 已加载 " + messages.size() + " 条消息。");
    }

    /**
     * 导出所有声明的 UI 资源并执行 ArcartX UI 绑定。
     */
    private void exportAndBindUi() throws IOException {
        Map<String, String> mappings = uiResourceMappings();
        if (mappings.isEmpty()) {
            return;
        }
        boolean overwrite = overwriteUiFiles();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String resourcePath = entry.getKey();
            String relativeUiPath = entry.getValue();
            File uiFile = context.exportUiResource(resourcePath, relativeUiPath, overwrite, moduleClassLoader());
            // UI 绑定由子类在 startService() 中按需调用 context.prepareUiBinding()
            // 这里仅记录导出的文件路径供子类查询
            uiBindings.put(relativeUiPath, new UiBinding(relativeUiPath, null));
        }
    }

    /**
     * 启动失败时的清理。
     */
    private void cleanupOnFailure() {
        try {
            stopService();
        } catch (Exception ignored) {
        }
        for (UiBinding binding : uiBindings.values()) {
            if (binding.registeredUiId() != null && context != null) {
                context.unregisterUi(binding.registeredUiId());
            }
        }
        uiBindings.clear();
        if (context != null) {
            context.unregisterListeners();
            context.expansionRegistry().unregisterAll();
        }
    }

    /**
     * 记录 UI 绑定结果（子类在 startService 中调用 prepareUiBinding 后更新）。
     *
     * @param relativeUiPath UI 文件相对路径
     * @param binding        UI 绑定结果
     */
    protected void recordUiBinding(String relativeUiPath, UiBinding binding) {
        uiBindings.put(relativeUiPath, binding);
    }

    // ── 统一 UI 注册 API（供子类与第三方模块使用）────────────────────

    /**
     * 注册模块 UI（基于 {@link #uiResourceMappings()} 中的映射）。
     * <p>
     * 自动导出资源文件并使用 {@link PacketBridgeAPI#registerOrReloadUi} 注册/热重载，
     * 解决 reload 时手动修改的 UI 文件不生效的问题。
     *
     * @param relativeUiPath   UI 文件相对路径（如 {@code "ui/menu_panel.yml"}）
     * @param configuredUiId   配置中的 UI ID（可为 null，自动从文件名推导）
     * @param registerOnEnable 是否注册到 ArcartX 引擎
     * @return UI 绑定结果
     * @throws IllegalStateException 若 relativeUiPath 未在 uiResourceMappings() 中声明
     */
    @NotNull
    protected final UiBinding registerModuleUi(@NotNull String relativeUiPath,
                                                @Nullable String configuredUiId,
                                                boolean registerOnEnable) {
        String resourcePath = resolveUiResourcePath(relativeUiPath);
        if (resourcePath == null) {
            throw new IllegalStateException(
                "relativeUiPath '" + relativeUiPath + "' 未在 uiResourceMappings() 中声明"
            );
        }
        return doRegisterModuleUi(resourcePath, relativeUiPath, configuredUiId, registerOnEnable);
    }

    /**
     * 注册模块 UI（显式指定 jar 内资源路径）。
     * <p>
     * 适用于动态生成的 UI 文件（如运行时通过模板写入的文件），
     * 这些文件不需要在 {@link #uiResourceMappings()} 中声明。
     *
     * @param resourcePath     jar 内资源路径（如 {@code "arcartx/ui/custom.yml"}）
     * @param relativeUiPath   导出到数据目录的相对路径
     * @param configuredUiId   配置中的 UI ID
     * @param registerOnEnable 是否注册
     * @return UI 绑定结果
     */
    @NotNull
    protected final UiBinding registerModuleUi(@NotNull String resourcePath,
                                                @NotNull String relativeUiPath,
                                                @Nullable String configuredUiId,
                                                boolean registerOnEnable) {
        return doRegisterModuleUi(resourcePath, relativeUiPath, configuredUiId, registerOnEnable);
    }

    /**
     * 获取已注册 UI 的 runtime ID。
     *
     * @param relativeUiPath {@link #uiResourceMappings()} 中声明的相对路径
     * @return runtime UI ID，未注册时返回 null
     */
    @Nullable
    protected final String getModuleUiId(@NotNull String relativeUiPath) {
        UiBinding binding = uiBindings.get(relativeUiPath);
        return binding != null ? binding.runtimeUiId() : null;
    }

    private UiBinding doRegisterModuleUi(String resourcePath, String relativeUiPath,
                                          String configuredUiId, boolean registerOnEnable) {
        boolean overwrite = overwriteUiFiles();
        File uiFile;
        try {
            uiFile = context.exportUiResource(resourcePath, relativeUiPath, overwrite, moduleClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("导出 UI 资源失败: " + resourcePath, e);
        }
        if (!registerOnEnable) {
            String runtime = PacketBridgeAPI.normalizeUiId(configuredUiId, uiFile);
            UiBinding binding = new UiBinding(runtime, null);
            recordUiBinding(relativeUiPath, binding);
            return binding;
        }
        PacketBridgeAPI bridge = context.packetBridge();
        if (bridge == null || !bridge.isAvailable()) {
            String runtime = PacketBridgeAPI.normalizeUiId(configuredUiId, uiFile);
            return new UiBinding(runtime, null);
        }
        PacketBridgeAPI.UiRegistrationResult reg =
            bridge.registerOrReloadUi(configuredUiId, uiFile);
        UiBinding binding = new UiBinding(reg.runtimeUiId(), reg.registeredUiId());
        recordUiBinding(relativeUiPath, binding);
        return binding;
    }

    @Nullable
    private String resolveUiResourcePath(String relativeUiPath) {
        for (Map.Entry<String, String> entry : uiResourceMappings().entrySet()) {
            if (entry.getValue().equals(relativeUiPath)) {
                return entry.getKey();
            }
        }
        return null;
    }
}


