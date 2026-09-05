package xuanmo.arcartxsuite.api.selection;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 多边形选区管理器，管理玩家的选区会话和交互回调。
 * <p>
 * 由宿主或选择工具模块提供默认实现，各模块通过 {@link PolygonSelectionCapability} 获取实例。
 * <p>
 * 使用流程：
 * <ol>
 *   <li>调用 {@link #startSelection} 开始选区，传入闭合回调</li>
 *   <li>玩家用配置的 wand 物品左键加点、右键闭合、Shift+右键撤销</li>
 *   <li>选区闭合后回调被调用，传入完成的 {@link PolygonSelection}</li>
 *   <li>调用 {@link #stopSelection} 结束选区会话</li>
 * </ol>
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public interface PolygonSelectionManager {

    /**
     * 开始一个多边形选区会话。
     * <p>
     * 如果玩家已有进行中的选区会话，将被重置。
     *
     * @param player   目标玩家
     * @param onClosed 选区闭合时的回调（在主线程调用），可为 null
     */
    void startSelection(@NotNull Player player, @Nullable Consumer<PolygonSelection> onClosed);

    /**
     * 获取玩家当前的选区会话（不自动创建）。
     *
     * @param player 目标玩家
     * @return 当前选区会话；未在选区时返回 null
     */
    @Nullable PolygonSelection getSelection(@NotNull Player player);

    /**
     * 结束玩家的选区会话，清空状态。
     *
     * @param player 目标玩家
     */
    void stopSelection(@NotNull Player player);

    /**
     * 设置选区工具物品材质（如 {@code STICK}、{@code BLAZE_ROD}）。
     * <p>
     * 默认由配置决定，此方法允许模块在运行时覆盖。
     *
     * @param material 物品材质名称
     */
    void setWandMaterial(@NotNull String material);

    /**
     * 设置使用选区工具所需的权限节点。
     *
     * @param permission 权限节点，null 表示无权限限制
     */
    void setRequiredPermission(@Nullable String permission);
}
