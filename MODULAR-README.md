# ArcartXSuite 模块开发指南

本文档面向第三方开发者，介绍如何基于 ArcartXSuite（AXS）宿主编写自定义模块。

---

## 前置条件

- 已安装 AXS 宿主插件（`ArcartXSuite.jar`）并正确配置
- **玩家客户端已安装 ArcartX 模组**（AXS 的所有 UI/HUD/Packet 功能依赖此客户端模组）
- Java 17+
- Gradle（推荐）或 Maven

> **关于 ArcartX 客户端模组：** 模块开发者在服务器端编写 Java 代码，但所有 UI 交互（如打开菜单、发送 HUD、播放粒子、显示伤害飘字等）都通过 ArcartX 客户端模组在玩家本地渲染。请确保目标玩家群体已安装对应版本的 ArcartX 模组。

---

## 第一步：创建 Gradle 模块项目

完整目录结构：

```
MyAXSModule/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/
    ├── java/com/example/
    │   ├── MyModule.java
    │   ├── MyListener.java
    │   ├── MyPacketHandler.java
    │   ├── service/
    │   │   └── MyService.java
    │   ├── config/
    │   │   └── MyModuleConfiguration.java
    │   └── storage/
    │       └── JdbcMyRepository.java
    └── resources/
        ├── module.yml
        ├── ArcartXMyModule.yml      ← 模块默认配置
        ├── messages.yml              ← 外部化消息文件
        └── arcartx/
            └── ui/
                └── my_ui.yml         ← ArcartX UI 文件
```

### build.gradle.kts

```kotlin
plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    // AXS 公共 API（从 GitHub Releases 下载预编译 JAR）
    compileOnly(files("libs/axs-api-x.x.x.jar"))

    // Bukkit API
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // 可选：PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.7")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.jar {
    archiveBaseName.set("MyAXSModule")
}
```

---

## 第二步：编写 module.yml

每个模块必须包含 `module.yml`，放在 `src/main/resources/` 下（打包后位于 JAR 根目录）：

```yaml
id: mymodule
name: MyModule
version: 1.0.0
main: com.example.MyModule
api-version: 1.0

depends: []
softdepends: []
external-depends:
  - PlaceholderAPI
external-softdepends:
  - AuthMe
```

| 字段 | 说明 |
|------|------|
| `id` | 模块唯一标识（字母、数字、下划线、连字符）|
| `name` | 显示名称 |
| `version` | 版本号 |
| `main` | 模块入口类全限定名 |
| `api-version` | 兼容的 AXS API 版本 |
| `depends` | 硬依赖的其他模块 ID（未加载则本模块拒绝启动）|
| `softdepends` | 软依赖的其他模块 ID（未加载则跳过，不报错）|
| `external-depends` | 硬依赖的 Bukkit 插件（未安装则本模块拒绝启动）|
| `external-softdepends` | 软依赖的 Bukkit 插件（未安装则跳过相关功能）|

> **依赖选择**：如果模块需要 PlaceholderAPI 占位符，放 `external-depends`；如果只是可选增强（如 AuthMe 登录支持），放 `external-softdepends`。

---

## 第三步：实现模块入口类

所有 AXS 模块均继承 `AbstractAXSModule`，通过覆写声明式方法声明模块能力，基类在 `onEnable` 时自动处理配置导出、UI 绑定、命令注册、监听器注册等。

```java
package com.example;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.AbstractAXSModule;
import xuanmo.arcartxsuite.api.ModuleCommandHandler;
import xuanmo.arcartxsuite.api.ModuleDescriptor;
import xuanmo.arcartxsuite.api.ModuleUiSpec;
import xuanmo.arcartxsuite.api.PacketHandlerSpec;
import xuanmo.arcartxsuite.api.config.ModuleConfig;
import xuanmo.arcartxsuite.api.config.SyncPolicy;
import xuanmo.arcartxsuite.api.config.ValidationRule;
import xuanmo.arcartxsuite.api.config.ValueType;

public final class MyModule extends AbstractAXSModule implements ModuleCommandHandler {

    private MyModuleConfiguration configuration;
    private MyService service;

    // ── 模块描述 ──────────────────────────────────────

    @Override
    public ModuleDescriptor descriptor() {
        return ModuleDescriptor.builder("mymodule")
            .name("MyModule")
            .version("1.0.0")
            .mainClass(getClass().getName())
            .build();
    }

    // ── 配置规约（合并原 configFileName / messagesFileName / syncPolicy 等）──

    @Override
    protected @NotNull ModuleConfig configSpec() {
        return ModuleConfig.builder()
            .configFileName("ArcartXMyModule.yml")   // 从 Jar 导出到 data/mymodule/config.yml
            .messagesFileName("messages.yml")         // 外部化消息文件
            .syncPolicy(SyncPolicy.builder()
                .dynamicSection("rewards")            // 声明动态配置节（不在诊断中校验结构）
                .build())
            .currentVersion(2)                        // 配置版本号，破坏性改动时递增
            .validations(List.of(
                ValidationRule.required("storage.mode", ValueType.STRING),
                ValidationRule.of("max-items", ValueType.INT).withRange(1, 1000)
            ))
            .build();
    }

    // ── UI 资源规约（合并原 uiResourceMappings / overwriteUiFiles）──

    @Override
    protected @NotNull ModuleUiSpec uiSpec() {
        return ModuleUiSpec.of(Map.of(
            "arcartx/ui/my_ui.yml", "ui/my_ui.yml"
        ));
    }

    // ── 客户端包处理器规约（合并原 createPacketHandler / priority 等）──

    @Override
    protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
        return PacketHandlerSpec.of(new MyPacketHandler());
    }

    // ── EventBus 发布主题（供其他模块检测本模块是否已加载）──

    @Override
    protected List<String> publishedTopics() {
        return List.of("axs.mymodule.item_purchased");
    }

    // ── 抽象方法：加载配置 ────────────────────────────

    @Override
    protected void loadConfiguration(@Nullable File configFile) throws Exception {
        configuration = MyModuleConfiguration.load(
            YamlConfiguration.loadConfiguration(configFile),
            messages(),  // MessageProvider，由基类根据 messagesFileName 初始化
            logger);
    }

    // ── 抽象方法：启动服务 ────────────────────────────

    @Override
    protected void startService() throws Exception {
        service = new MyService(plugin, configuration, packetBridge, logger);
        service.start();

        // 注册 UI（在 startService 中调用，基类已先完成资源导出）
        registerModuleUi("ui/my_ui.yml", "AXS:my_ui", true);

        // 注册 Capability（供其他模块调用）
        registerCapability(MyCapability.class, new MyCapabilityImpl());
    }

    // ── 抽象方法：停止服务 ────────────────────────────

    @Override
    protected void stopService() {
        if (service != null) {
            service.shutdown();
            service = null;
        }
    }

    // ── 声明式：事件监听器（基类自动注册/注销）──

    @Override
    protected @NotNull List<Listener> createListeners() {
        return List.of(new MyListener(this));
    }

    // ── 声明式：独立玩家命令（需在 plugin.yml 声明命令）──

    @Override
    protected @NotNull Map<String, TabExecutor> commandBindings() {
        return Map.of("mycmd", new MyPlayerCommand(() -> service, messages()));
    }

    // ── 声明式：PlaceholderAPI 占位符 ──

    @Override
    protected @Nullable Object createPlaceholderExpansion() {
        return new MyPlaceholderExpansion(plugin, () -> service);
    }

    // ── ModuleCommandHandler：/axs mymodule 子命令 ──

    @Override public String commandId() { return "mymodule"; }

    @Override public List<String> actions() {
        return List.of("help", "status", "reload");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        // 处理 /axs mymodule <action>
        return true;
    }
}
```

---

## 第四步：AbstractAXSModule 声明式 API 详解

继承 `AbstractAXSModule` 时，通过覆写以下方法声明模块能力。基类在 `onEnable` 时按固定顺序自动处理。

### onEnable 执行顺序

```
1. 导出并加载配置（configSpec → loadConfiguration）
2. 导出外部化消息（configSpec.messagesFileName）
3. 导出 UI 资源并绑定（uiSpec）
4. 绑定命令（commandBindings）
5. 注册 EventBus 发布主题（publishedTopics）
6. 启动服务（startService）
7. 注册事件监听器（createListeners）
8. 注册 PlaceholderAPI（createPlaceholderExpansion）
9. 注册客户端包处理器（createPacketHandlerSpec）
10. 注册客户端初始化回调（createInitializedHandler）
```

> **关键**：`startService()` 在命令绑定之后、监听器注册之前调用。命令使用 `Supplier` 延迟引用服务，即使服务启动失败命令仍可注册。

### 配置规约：`configSpec()`

返回 `ModuleConfig` record，合并了原 8 个配置钩子：

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `configFileName` | 模块配置文件名（Jar 内资源路径），null 表示无配置文件 | null |
| `messagesFileName` | 外部化消息文件名，null 表示不使用 | null |
| `syncPolicy` | 配置同步策略 | `SyncPolicy.strict()` |
| `currentVersion` | 内置配置版本号 | 1 |
| `versionPath` | 版本号在 YAML 中的路径 | `"config-version"` |
| `migrationFolder` | Jar 内迁移规则目录 | `"migrations"` |
| `validations` | 配置字段校验规则列表 | 空列表 |
| `additionalSpecs` | 附属配置 spec（如 `chat/channels/*.yml`） | 空列表 |

```java
@Override
protected @NotNull ModuleConfig configSpec() {
    return ModuleConfig.builder()
        .configFileName("ArcartXMyModule.yml")
        .messagesFileName("messages.yml")
        .syncPolicy(SyncPolicy.builder()
            .dynamicSection("rewards")        // 动态节：用户自定义 key，诊断时不校验结构
            .dynamicSection("commands")
            .build())
        .currentVersion(3)
        .validations(List.of(
            ValidationRule.required("storage.mode", ValueType.STRING),
            ValidationRule.of("max-items", ValueType.INT).withRange(1, 1000),
            ValidationRule.of("tax-rate", ValueType.DOUBLE).withRange(0.0, 0.99)
        ))
        .build();
}
```

### UI 资源规约：`uiSpec()`

返回 `ModuleUiSpec` record：

```java
@Override
protected @NotNull ModuleUiSpec uiSpec() {
    return ModuleUiSpec.of(Map.of(
        "arcartx/ui/my_ui.yml", "ui/my_ui.yml",
        "arcartx/ui/my_hud.yml", "ui/my_hud.yml"
    ));
    // 第二参数 overwrite=true 时覆盖用户已修改的 UI 文件（一般不推荐）
}
```

- **键**：Jar 内资源路径（`src/main/resources/` 下的相对路径）
- **值**：导出到宿主数据目录的相对路径（`plugins/ArcartXSuite/ui/` 下）
- 基类在 `onEnable` 时自动导出，**不会覆盖**已有文件（除非 `overwrite=true`）

### 客户端包处理器规约：`createPacketHandlerSpec()`

返回 `PacketHandlerSpec` record，合并了原 4 个包相关钩子：

```java
// 简单注册（默认优先级 0）
@Override
protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
    return PacketHandlerSpec.of(new MyPacketHandler());
}

// 指定优先级（越小越先，EventPacket 建议 100）
@Override
protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
    return PacketHandlerSpec.of(new MyPacketHandler(), 0);
}

// 完整注册（含归属元数据，用于 PacketGuard 路由）
@Override
protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
    return PacketHandlerSpec.of(handler, 0, "AXS_MY_MODULE", "mymodule");
}
```

> **无客户端包的模块**：返回 `PacketHandlerSpec.NONE`（默认值），基类不注册。

### EventBus 发布主题：`publishedTopics()`

声明本模块通过 EventBus 发布的事件主题。基类在 `startService` **之前**自动注册到 EventBus，确保其他模块可在启动时通过 `EventBusCapability.hasPublisher(topic)` 检测本模块是否已加载。

```java
@Override
protected List<String> publishedTopics() {
    return List.of(
        "axs.mymodule.item_purchased",
        "axs.mymodule.quest_completed"
    );
}
```

> **命名规范**：使用 `axs.<moduleId>.<event>` 格式，避免与其他模块冲突。

### 客户端初始化回调：`createInitializedHandler()`

当玩家客户端 ArcartX 模组完成初始化时触发，适合在此打开 HUD 或同步初始数据：

```java
@Override
protected @Nullable ClientInitializedHandler createInitializedHandler() {
    return player -> {
        service.handleClientInitialized(player);
    };
}
```

### 其他声明式方法

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `createListeners()` | Bukkit 事件监听器列表（自动注册/注销） | 空列表 |
| `commandBindings()` | 独立玩家命令：命令名 → TabExecutor | 空 Map |
| `createPlaceholderExpansion()` | PAPI 占位符扩展实例，null 不注册 | null |

---

## 第五步：UI 绑定与资源路径

### 1. 声明 UI 资源映射

在 `src/main/resources/arcartx/ui/` 下放置 UI YAML 文件，通过 `uiSpec()` 声明映射。基类在 `onEnable` 时自动导出到 `plugins/ArcartXSuite/ui/`。

### 2. 运行时注册 UI

在 `startService()` 中调用 `registerModuleUi()` 将 UI 注册到 ArcartX 客户端：

```java
@Override
protected void startService() {
    service = new MyService(...);
    service.start();

    // registerModuleUi(relativeUiPath, uiId, closeOnReload)
    registerModuleUi("ui/my_ui.yml", "AXS:my_ui", true);
    registerModuleUi("ui/my_hud.yml", "AXS:my_hud", true);
}
```

- `relativeUiPath`：相对于 `plugins/ArcartXSuite/` 的 UI 文件路径（与 `uiSpec()` 的值一致）
- `uiId`：UI 唯一标识，建议用 `AXS:<name>` 前缀
- `closeOnReload`：reload 时是否关闭客户端 UI

### 3. reload 时 UI 保持

`AbstractAXSModule` 内部使用 `reloading` 标志，reload 时跳过 UI 注销，避免客户端丢失已打开的 HUD 或菜单。开发者无需手动处理。

---

## 第六步：使用 ModuleContext 与基类注入字段

`AbstractAXSModule` 在 `onEnable` 时自动从 `ModuleContext` 注入以下 `protected` 字段，子类直接访问即可：

### 核心字段

```java
// 基础设施（直接访问 protected 字段）
plugin           // JavaPlugin 实例
logger           // 带模块前缀的 Logger
dataFolder       // 模块私有数据目录（plugins/ArcartXSuite/data/mymodule/）
pluginDataFolder // 宿主数据根目录（plugins/ArcartXSuite/）
moduleClassLoader() // 模块 ClassLoader
messages()       // MessageProvider（由 configSpec.messagesFileName 初始化）
```

### ArcartX 桥接

```java
packetBridge       // PacketBridgeAPI — 发送自定义包、注册/打开/关闭 UI
clientBridge       // ClientBridgeAPI — 检测客户端是否在线
itemStackBridge    // ItemBridgeAPI — 序列化/反序列化带 NBT 的物品
propBridge         // PropBridgeAPI — 快捷道具栏
worldTextureBridge // WorldTextureBridgeAPI — 世界纹理
```

### 全局桥接

```java
itemSourceRegistry    // ItemSourceRegistry — MythicMobs/NeigeItems/MMOItems/Overture 物品来源
itemMatcher           // ItemMatcherAPI — 按 id/名称/NBT 匹配物品
itemRewardDispatcher  // ItemRewardDispatcher — 统一物品奖励发放
pendingRewardService  // PendingRewardService — 待发放奖励队列
vanillaItemNameBridge // VanillaItemNameBridge — 原版物品中文名解析
currencyManager       // CurrencyBridgeAPI — Vault/PlayerPoints/XConomy 等
rondoBridge           // RondoBridge — Rondo 经济系统
attributeBridge       // AttributeBridgeRegistry — AttributePlus/Crane/MythicLib/Symphony
ariaBridge            // AriaBridge — Aria 脚本桥接
scriptConditionEvaluator // ScriptConditionEvaluator — 条件评估器
```

### 基础设施服务

```java
storageManager       // StorageManager — 统一数据源管理（共享/自建双模式）
scheduler            // SchedulerAPI — 调度器
crossServer          // CrossServerAPI — 跨服传输（Redis + Proxy）
packetGuard          // PacketGuardAPI — 客户端包频率限制（可能为 null）
accountTypeService   // AccountTypeService — 账号类型识别
placeholderResolver  // PlaceholderResolverAPI — 占位符解析
expansionRegistry    // PlaceholderExpansionRegistry — PAPI 扩展注册表
```

### 高级桥接（模块独立管理生命周期）

```java
// 创建路标桥接（模块卸载时自动清理）
WaypointBridgeAPI waypoint = context.createWaypointBridge();
waypoint.create(player, "目标", x, y, z, Color.RED);

// 创建 Adyeshach NPC 桥接
AdyeshachNpcBridgeAPI npc = context.createAdyeshachNpcBridge();
```

### 资源与工具

```java
// 从模块 Jar 读取资源
InputStream in = openResource("arcartx/ui/my_ui.yml", moduleClassLoader());

// 导出资源到宿主目录
exportResource("shops/example.yml", targetFile, false);

// 检查外部插件是否安装
boolean hasPapi = hasPlugin("PlaceholderAPI");
```

---

## 第七步：注册子命令

实现 `ModuleCommandHandler` 接口即可自动注册 `/axs mymodule ...` 子命令：

```java
public final class MyModule extends AbstractAXSModule implements ModuleCommandHandler {

    private MyAdminCommand adminCommand;

    @Override public String commandId() { return "mymodule"; }

    @Override public List<String> actions() {
        return adminCommand != null ? adminCommand.actions() : List.of("help", "status");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return adminCommand != null && adminCommand.onCommand(sender, label, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return adminCommand != null ? adminCommand.onTabComplete(sender, args) : null;
    }
}
```

> **实际模式**：大多数模块将命令逻辑委托给独立的 `*AdminCommand` 类，`ModuleCommandHandler` 只做转发。

---

## 第八步：处理客户端包

客户端通过 ArcartX 模组向服务器发送自定义数据包，模块通过 `createPacketHandlerSpec()` 注册处理器：

```java
public class MyPacketHandler implements ClientPacketHandler {

    @Override
    public String action() {
        return "my_action"; // 客户端发送 action=my_action 时触发
    }

    @Override
    public boolean handle(@NotNull Player player, @NotNull String packetId,
                          @NotNull List<String> data) {
        String value = data.isEmpty() ? "" : data.get(0);
        player.sendMessage("收到客户端数据: " + value);
        return true; // 返回 true 表示已处理
    }
}
```

### 多包 ID 路由

一个模块可以处理多个 packet ID。在 `handle` 方法中通过 `packetId` 参数区分：

```java
@Override
protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
    return PacketHandlerSpec.of((player, packetId, data) -> {
        switch (packetId) {
            case "AXS_MY_MODULE_MAIN" -> service.handleMainPacket(player, data);
            case "AXS_MY_MODULE_ADMIN" -> service.handleAdminPacket(player, data);
            default -> { return false; } // 不属于本模块的包
        }
        return true;
    });
}
```

### 主线程安全

涉及背包或经济操作的包处理应切到主线程：

```java
@Override
protected @NotNull PacketHandlerSpec createPacketHandlerSpec() {
    return PacketHandlerSpec.of((player, packetId, data) -> {
        if (Bukkit.isPrimaryThread()) {
            service.handleClientPacket(player, packetId, data);
        } else {
            AxsScheduler.runTask(plugin, () ->
                service.handleClientPacket(player, packetId, data));
        }
        return true;
    });
}
```

---

## 第九步：Capability 与跨模块通信

### 核心原则

**模块间不直接持有彼此实例，也不使用 `context.getModule()`。** 所有跨模块通信通过 Capability 注册表完成。

### 注册 Capability

模块在 `startService()` 中注册自身能力：

```java
@Override
protected void startService() {
    service = new MyService(...);
    service.start();

    // 注册业务能力
    registerCapability(MyCapability.class, new MyCapabilityImpl());

    // 注册系统能力（几乎所有数据型模块都应注册）
    registerCapability(PlayerDataPurgeable.class, new PlayerDataPurgeable() {
        @Override public @NotNull String moduleId() { return "mymodule"; }
        @Override public int purgePlayerData(@NotNull UUID playerUuid) {
            try { return repository.deletePlayerData(playerUuid); }
            catch (Exception e) { logger.warning("purge 失败: " + e.getMessage()); return -1; }
        }
        @Override public int purgeAllPlayerData() {
            try { return repository.deleteAllPlayerData(); }
            catch (Exception e) { logger.warning("purgeAll 失败: " + e.getMessage()); return -1; }
        }
    });

    registerCapability(DatabaseMigratable.class, new DatabaseMigratable() {
        @Override public @NotNull String moduleId() { return "mymodule"; }
        @Override public @NotNull MigrationResult migrateDatabase(
                @NotNull StorageDescriptor target, boolean overwrite) {
            return repository.migrateData(target, overwrite);
        }
        @Override public @NotNull StorageDescriptor currentDescriptor() {
            return repository.getDescriptor();
        }
    });
}
```

### 使用其他模块的 Capability

通过 `getCapability()` 获取。**使用 `Supplier` 延迟查找**，避免模块启动顺序问题：

```java
@Override
protected void startService() {
    // 延迟查找：模块启动时 mail 模块可能还未加载
    Supplier<MailDispatchable> mailSupplier = () -> getCapability(MailDispatchable.class);
    Supplier<SignalDispatchable> signalSupplier = () -> getCapability(SignalDispatchable.class);

    service = new MyService(..., mailSupplier, signalSupplier, ...);
    service.start();
}
```

### 常用 Capability 一览

| Capability | 提供模块 | 用途 |
|---|---|---|
| `MailDispatchable` | mail | 发送带附件的系统邮件 |
| `SignalDispatchable` | eventpacket | 派发事件信号 |
| `EventBusCapability` | 宿主 | 事件总线（发布/订阅主题） |
| `ChatCardSendable` | chat | 发送聊天卡片 |
| `ChatMutable` | chat | 禁言/解禁管理 |
| `SubtitlePlayable` | announcer | 播放字幕 |
| `TitleGrantable` | title | 授予称号 |
| `TabRefreshable` | tab | 刷新 Tab 列表 |
| `MapNavigable` | map | 地图导航 |
| `QuestGpsNavigable` | questgps | 任务导航 |
| `EssentialsQueryable` | essentials | 查询 Essentials 状态 |
| `WarehouseAutoDepositable` | warehouse | 自动存入仓库 |
| `PickupInterceptor` | pickup | 拦截物品拾取 |
| `PlayerDataPurgeable` | 各数据模块 | `/axs purge` 玩家数据清除 |
| `DatabaseMigratable` | 各数据模块 | `/axs migrate` 数据库迁移 |

### EventBus 事件总线

通过 `EventBusCapability` 发布/订阅事件，实现完全解耦的事件通知：

```java
// 发布事件（在 service 中）
EventBusCapability eventBus = getCapability(EventBusCapability.class);
if (eventBus != null) {
    eventBus.publish("axs.mymodule.item_purchased", Map.of("player", uuid, "item", itemId));
}

// 订阅事件（在其他模块中）
EventBusCapability eventBus = getCapability(EventBusCapability.class);
if (eventBus != null && eventBus.hasPublisher("axs.mymodule.item_purchased")) {
    eventBus.subscribe("axs.mymodule.item_purchased", payload -> {
        // 处理事件
    });
}
```

> **`publishedTopics()` 的作用**：声明主题后，其他模块在启动时就能通过 `hasPublisher(topic)` 检测本模块是否已加载，无需等待本模块的 `startService` 完成。

---

## 第十步：存储层（数据库模块）

需要持久化存储的模块使用 `AbstractModuleRepository` + `StorageManager` 双模式。

### 1. 定义 StorageConfiguration

```java
public record StorageConfiguration(
    String mode,           // "sqlite" 或 "mysql"
    String sqliteFileName,
    String host, int port, String database,
    String username, String password,
    String tablePrefix, int poolSize,
    boolean shared         // 是否使用共享数据源
) {
    public boolean hasOverride() {
        return !shared && "mysql".equalsIgnoreCase(mode);
    }

    public StorageDescriptor toDescriptor() {
        if (!isSqlite()) {
            return StorageDescriptor.mysql(host, port, database, username, password, poolSize, tablePrefix);
        }
        return StorageDescriptor.sqlite(sqliteFileName);
    }

    public boolean isSqlite() {
        return !"mysql".equalsIgnoreCase(mode);
    }
}
```

### 2. 实现 Repository

```java
public final class JdbcMyRepository extends AbstractModuleRepository {

    private String tRecords;

    // 新构造：接收 DataSource + Descriptor（由 StorageManager 解析）
    public JdbcMyRepository(DataSource dataSource, StorageDescriptor descriptor,
                            File dataFolder, Logger logger) {
        super("AXS-MyModule", dataFolder, descriptor, logger, dataSource);
    }

    @Override
    protected void onInitialize(Connection conn) throws SQLException {
        tRecords = descriptor().tablePrefix() + "records";
        try (Statement stmt = conn.createStatement()) {
            if (isMysql()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tRecords + " ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "player_uuid CHAR(36) NOT NULL,"
                    + "data MEDIUMTEXT NOT NULL,"
                    + "created_at BIGINT NOT NULL,"
                    + "INDEX idx_player (player_uuid))");
            } else {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + tRecords + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_uuid TEXT NOT NULL,"
                    + "data TEXT NOT NULL,"
                    + "created_at INTEGER NOT NULL)");
            }
        }
    }

    @Override
    protected List<String> playerDataTables() {
        return List.of(tRecords);
    }
}
```

### 3. 在 startService 中初始化（共享/自建双模式）

```java
@Override
protected void startService() throws Exception {
    StorageConfiguration storage = configuration.storage();

    StorageDescriptor descriptor;
    DataSource dataSource;
    if (storage.hasOverride()) {
        // 自建模式：模块配置了独立 MySQL
        descriptor = storage.toDescriptor().withTablePrefix(storage.tablePrefix());
        dataSource = storageManager.resolveModuleDataSource("mymodule", null, dataFolder, descriptor);
    } else {
        // 共享模式：使用本体统一数据源
        descriptor = storageManager.getDescriptor().withTablePrefix(storage.tablePrefix());
        dataSource = storageManager.resolveModuleDataSource("mymodule",
            storage.sqliteFileName(), dataFolder, null);
    }

    repository = new JdbcMyRepository(dataSource, descriptor, dataFolder, logger);
    repository.initialize();  // 建表（幂等）

    service = new MyService(repository, ...);
    service.start();
}

@Override
protected void stopService() {
    if (service != null) { service.shutdown(); service = null; }
    if (repository != null) { repository.shutdown(); repository = null; }
    storageManager.closeModuleDataSource("mymodule");
}
```

> **共享模式**（默认）：所有模块复用本体 HikariCP 连接池，用 `tablePrefix` 隔离表。
> **自建模式**：模块配置了独立 MySQL 时自建连接池，向后兼容。
> **SQLite 模式**：各模块使用各自的 `<moduleId>.db` 文件。

---

## 第十一步：打包与部署

1. 执行 `./gradlew jar` 构建模块 JAR
2. 将 `build/libs/MyAXSModule.jar` 复制到服务器：

```
plugins/
  ArcartXSuite.jar
  ArcartXSuite/
    config.yml
    modules/
      MyAXSModule.jar   ← 你的模块
```

3. 在 `config.yml` 中启用：

```yaml
modules:
  mymodule:
    enabled: true
```

4. 重启服务器或使用 `/axs load mymodule` 热加载

---

## 第十二步：模块 Ed25519 签名（可选）

对 `module.yml` 的 `id:version:main` 做 Ed25519 签名，供服主在 `module-signature-public-keys` 中校验模块完整性：

```bash
pip install cryptography
python scripts/sign-module.py keygen --out-dir ./module-signing-keys
python scripts/sign-module.py sign --module-yml src/main/resources/module.yml --private-key module-signing-keys/ed25519-private.pem
python scripts/sign-module.py pubkey --public-key module-signing-keys/ed25519-public.pem
```

将公钥填入宿主 `config.yml` 的 `module-signature-public-keys` 列表，未通过签名校验的模块将拒绝加载。

---

## 常见问题与排坑

### Q: 模块加载失败，控制台报 `ClassNotFoundException`
- 检查 `module.yml` 中的 `main` 字段是否与 Java 类全限定名一致
- 检查模块 JAR 是否包含编译后的 `.class` 文件
- 检查 `external-depends` 声明的插件是否已安装

### Q: `module.yml` 放错位置
- 必须放在 `src/main/resources/module.yml`，打包后位于 JAR 根目录
- 放在 `META-INF/` 或其他子目录下会找不到

### Q: UI 文件导出后客户端看不到
- 确认玩家客户端已安装 ArcartX 模组
- 检查 `uiSpec()` 的映射键值是否正确（Jar 内路径 → 输出路径）
- 检查 `registerModuleUi()` 的 `relativeUiPath` 是否与 `uiSpec()` 的值一致

### Q: reload 后 UI 丢失
- 确保使用 `AbstractAXSModule` 的 reload 机制（基类已处理 UI 保持逻辑）
- 不要手动调用 `packetBridge.unregisterUi()` 后再重新注册

### Q: 配置没有生效
- 配置文件位置：`plugins/ArcartXSuite/data/<moduleId>/config.yml`
- 配置版本不匹配时会触发迁移，运行 `/axs config preview <moduleId>` 检查兼容性
- 使用 `dynamicSection` 声明用户自定义 key 的配置节，避免诊断误报

### Q: 跨模块调用返回 null
- 使用 `Supplier` 延迟查找：`Supplier<MailDispatchable> mail = () -> getCapability(MailDispatchable.class)`
- 检查目标模块是否在 `softdepends` 中声明（确保加载顺序）
- 检查目标模块是否已注册对应 Capability

### Q: 可以 `import xuanmo.arcartxsuite.bridge.*` 吗？
- **不可以**。模块只能通过 `AbstractAXSModule` 注入的 `protected` 字段或 `ModuleContext` 获取的 API 接口与宿主交互
- 直接引用宿主实现类会导致 ClassLoader 隔离问题，且可能在不同版本中不兼容

### Q: 可以用 `context.getModule()` 获取其他模块实例吗？
- **不推荐**。AXS 的 30 个内置模块均未使用此方式
- 跨模块通信应通过 `registerCapability` / `getCapability` 或 `EventBusCapability` 完成

### Q: 占位符扩展未注册
- 确保服务器已安装 PlaceholderAPI
- 在 `module.yml` 的 `external-depends` 中声明 `PlaceholderAPI`
- `createPlaceholderExpansion()` 返回的对象需符合 PAPI 扩展规范

---

## 更多参考

- `axs-api/src/main/java/xuanmo/arcartxsuite/api/` — 所有公共接口的源码与 Javadoc
