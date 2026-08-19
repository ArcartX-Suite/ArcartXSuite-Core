package xuanmo.arcartxsuite.api.capability;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.AXSModule;
import xuanmo.arcartxsuite.api.ModuleDescriptor;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 模块管理能力接口，由宿主核心注册，供调试/管理工具使用。
 * <p>
 * 提供按 ID 查找模块实例、列出所有模块、重载模块等管理操作。
 * 普通业务模块不应使用此接口，应通过 {@link EventBusCapability} 或其他
 * 具体 capability 进行模块间通信。
 *
 * @since 1.3.2
 */
@ApiStability.Internal
public interface ModuleAdminCapability {

    /**
     * 按 ID 查找已加载的模块实例。
     *
     * @param moduleId 模块 ID
     * @return 模块实例，未加载或不存在时返回 empty
     */
    @NotNull Optional<AXSModule> findModule(@NotNull String moduleId);

    /**
     * 列出所有已加载模块的描述符。
     *
     * @return 不可变的模块描述符列表
     */
    @NotNull List<ModuleDescriptor> listModules();

    /**
     * 重载指定模块（调用其 {@code onReload()}）。
     *
     * @param moduleId 模块 ID
     * @throws Exception 重载失败时抛出
     */
    void reloadModule(@NotNull String moduleId) throws Exception;
}
