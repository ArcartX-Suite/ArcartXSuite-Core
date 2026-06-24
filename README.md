# ArcartXSuite 模块开发 SDK

> **Suite — 组曲，亦是套装。**
>
> 在音乐中，**Suite（组曲）** 是由多首独立小曲串联而成的成套器乐作品。
> 巴赫的《英国组曲》有四乐章——阿勒曼德的庄严，库朗特的轻快，萨拉班德的沉思，吉格的欢腾。
> 每一段风格、节奏各不相同，**分开能单独演奏，合起来是一部统一于同一调性的完整作品**。
>
> **ArcartX-Suite** 正是这样的理念：每个模块如同一个乐章，独立运作、各具音色、按需启用，共享 ArcartX 的统一调性。
> 本 SDK 为你提供谱写这一乐章所需的全部乐谱与乐器。

这是 **ArcartXSuite (AXS)** 的模块开发 SDK。本仓库提供完整的公共 API 源码与文档，供第三方开发者基于 AXS 宿主编写自定义模块。

## 快速开始

1. 获取 `axs-api` JAR
2. 在模块项目的 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    compileOnly(files("libs/axs-api-x.x.x.jar"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}
```

3. 实现 `AbstractAXSModule` 并打包为 `.jar`
4. 将模块 JAR 放入服务器的 `plugins/ArcartXSuite/modules/`
5. 在 `plugins/ArcartXSuite/config.yml` 中启用模块

## 项目结构

| 目录 | 说明 |
|------|------|
| `axs-api/` | 模块公共 API（`AXSModule`, `ModuleContext`, `Bridge API` 等）|
| `axs-placeholder/` | PlaceholderAPI 扩展接口 |
| `proxy/` | Bungee / Velocity 代理端公共库 |
| `src/main/java/xuanmo/arcartxsuite/` | 宿主核心源码参考（`ModuleRegistry`, `BridgeLifecycleManager` 等）|

## 模块开发指南

参见 [MODULAR-README.md](./MODULAR-README.md)。

## License

作者保留一切权利。
