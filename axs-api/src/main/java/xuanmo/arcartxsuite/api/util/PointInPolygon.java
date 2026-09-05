package xuanmo.arcartxsuite.api.util;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * 射线法多边形包含判断工具。
 * <p>
 * 判断一个 2D 点 (x, z) 是否落在由顶点列表构成的多边形内部（含边界）。
 * 算法使用经典的射线交叉法（ray casting），时间复杂度 O(n)。
 * <p>
 * 此类从 afkreward 模块的 {@code AfkArea.contains} 逻辑提取为公共 API，
 * 供 fishing、afkreward 等需要多边形区域判断的模块复用。
 *
 * @since 1.5.0
 */
public final class PointInPolygon {

    private PointInPolygon() {}

    /**
     * 2D 顶点。
     */
    public record Vertex(int x, int z) {}

    /**
     * 判断点 (x, z) 是否在多边形内部（含边界）。
     *
     * @param vertices 多边形顶点列表（按顺时针或逆时针顺序），至少 3 个点
     * @param x        待判断点的 X 坐标
     * @param z        待判断点的 Z 坐标
     * @return true 如果点在多边形内或边界上；顶点不足 3 个时返回 false
     */
    public static boolean contains(@NotNull List<Vertex> vertices, int x, int z) {
        if (vertices == null || vertices.size() < 3) return false;
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Vertex pi = vertices.get(i);
            Vertex pj = vertices.get(j);
            boolean intersect = ((pi.z > z) != (pj.z > z))
                && (x < (pj.x - pi.x) * (z - pi.z) / (double) (pj.z - pi.z) + pi.x);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    /**
     * 判断点 (x, z) 是否在多边形内部（含边界），使用 double 坐标。
     *
     * @param vertices 多边形顶点列表，至少 3 个点
     * @param x        待判断点的 X 坐标
     * @param z        待判断点的 Z 坐标
     * @return true 如果点在多边形内或边界上
     */
    public static boolean contains(@NotNull List<Vertex> vertices, double x, double z) {
        return contains(vertices, (int) Math.floor(x), (int) Math.floor(z));
    }
}
