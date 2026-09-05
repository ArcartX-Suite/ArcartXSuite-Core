package xuanmo.arcartxsuite.api.selection;

import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 多边形选区能力接口。
 * <p>
 * 由宿主或选择工具模块注册，其他模块通过
 * {@code context.getCapability(PolygonSelectionCapability.class)} 获取。
 * <p>
 * 当宿主未提供选区工具实现时，{@link #manager()} 返回 null，
 * 模块应据此降级（如退回到手动填写坐标模式）。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public interface PolygonSelectionCapability {

    /**
     * 获取多边形选区管理器实例。
     *
     * @return 选区管理器；宿主未提供时返回 null
     */
    @Nullable PolygonSelectionManager manager();
}
