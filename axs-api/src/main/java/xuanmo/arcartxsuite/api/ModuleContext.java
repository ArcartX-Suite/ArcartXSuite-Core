package xuanmo.arcartxsuite.api;

/**
 * 宿主提供给模块的上下文接口。
 * <p>
 * 模块通过此接口获取基础设施能力（桥接、文件、UI 注册等），
 * 而无需直接引用宿主插件主类。
 * <p>
 * 本接口按 ISP 原则拆分为多个职责子接口，ModuleContext 继承所有子接口以保持向后兼容：
 * <ul>
 *   <li>{@link InfrastructureContext} — 基础设施（plugin/logger/dataFolder）</li>
 *   <li>{@link BridgeContext} — 桥接（UI/Packet/Item/经济/属性/脚本）</li>
 *   <li>{@link SecurityContext} — 安全（包频率限制/账号识别/跨服）</li>
 *   <li>{@link StorageContext} — 存储（统一数据源管理器）</li>
 *   <li>{@link ResourceContext} — 资源（Jar 资源读取/导出/UI 绑定）</li>
 *   <li>{@link RegistrationContext} — 注册（事件/命令/按键/PAPI/Capability）</li>
 * </ul>
 * 模块可按需依赖窄接口（如只依赖 {@link StorageContext}），也可继续依赖完整的 {@link ModuleContext}。
 *
 * @since 1.0.0
 */
public interface ModuleContext extends
    InfrastructureContext,
    BridgeContext,
    SecurityContext,
    StorageContext,
    ResourceContext,
    RegistrationContext {
}
