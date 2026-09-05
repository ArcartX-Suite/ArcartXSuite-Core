package xuanmo.arcartxsuite.api.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.util.PointInPolygon;

/**
 * 玩家的多边形选区会话。
 * <p>
 * 支持采集任意数量的 2D 顶点 (x, z)，用于定义多边形区域。
 * 交互约定（由 {@link PolygonSelectionManager} 的监听器实现）：
 * <ul>
 *   <li>左键方块 → {@link #addPoint(Location)} 添加顶点</li>
 *   <li>右键方块 → {@link #close()} 闭合多边形（至少 3 点）</li>
 *   <li>Shift+右键 → {@link #undoLast()} 撤销上一个顶点</li>
 * </ul>
 * <p>
 * 此类是可变状态对象，每个玩家持有一个实例，由 {@link PolygonSelectionManager} 管理。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class PolygonSelection {

    private @Nullable String worldName;
    private final List<PointInPolygon.Vertex> points = new ArrayList<>();
    private boolean closed = false;

    /**
     * 添加一个顶点。第一个顶点同时确定所在世界。
     * <p>
     * 后续顶点必须与第一个顶点在同一世界，否则忽略。
     *
     * @param loc 方块位置
     * @return true 如果添加成功；false 如果世界不匹配
     */
    public boolean addPoint(@NotNull Location loc) {
        if (loc.getWorld() == null) return false;
        String world = loc.getWorld().getName();
        if (worldName == null) {
            worldName = world;
        } else if (!worldName.equals(world)) {
            return false;
        }
        points.add(new PointInPolygon.Vertex(loc.getBlockX(), loc.getBlockZ()));
        return true;
    }

    /**
     * 撤销上一个顶点。
     *
     * @return true 如果成功撤销；false 如果没有可撤销的顶点
     */
    public boolean undoLast() {
        if (points.isEmpty()) return false;
        points.remove(points.size() - 1);
        return true;
    }

    /**
     * 闭合多边形。要求至少 3 个顶点。
     *
     * @return true 如果成功闭合；false 如果顶点不足 3 个
     */
    public boolean close() {
        if (points.size() < 3) return false;
        closed = true;
        return true;
    }

    /**
     * 重置选区，清空所有顶点和状态。
     */
    public void reset() {
        points.clear();
        worldName = null;
        closed = false;
    }

    /** 是否已闭合（至少 3 个顶点且调用了 {@link #close()}）。 */
    public boolean isClosed() {
        return closed;
    }

    /** 是否有至少 3 个顶点（可闭合的最小数量）。 */
    public boolean canClose() {
        return points.size() >= 3;
    }

    /** 当前顶点数量。 */
    public int pointCount() {
        return points.size();
    }

    /** 所在世界名称，未开始选择时为 null。 */
    public @Nullable String worldName() {
        return worldName;
    }

    /**
     * 返回不可变的顶点列表副本。
     */
    public @NotNull List<PointInPolygon.Vertex> points() {
        return Collections.unmodifiableList(points);
    }

    /**
     * 判断给定位置是否在已闭合的多边形内部。
     *
     * @param loc 待判断的位置
     * @return true 如果在多边形内；未闭合或世界不匹配时返回 false
     */
    public boolean contains(@NotNull Location loc) {
        if (!closed || worldName == null) return false;
        if (loc.getWorld() == null || !worldName.equals(loc.getWorld().getName())) return false;
        return PointInPolygon.contains(points, loc.getBlockX(), loc.getBlockZ());
    }
}
