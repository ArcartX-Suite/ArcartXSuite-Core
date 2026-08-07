package xuanmo.arcartxsuite.api.util;

import java.util.List;
import org.bukkit.Location;

/**
 * A* 方块级寻路服务。
 * <p>
 * 特性：
 * - 8 方向水平移动 + 上下台阶
 * - 自动回避不可通行方块（固体、液体、危险方块）
 * - 可配置最大迭代次数
 * - 路径简化（移除共线点）与等间距采样
 * - 随机地面/空中位置生成
 * - 路径平滑（线性插值）
 *
 * @since 1.5.0
 */
public final class NavigationPathfinder {

    private static final int[][] DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final double STRAIGHT_COST = 1.0;
    private static final double DIAGONAL_COST = 1.414;
    private static final double SWIM_PENALTY = 1.5;

    private final int maxIterations;

    /**
     * 创建寻路器。
     *
     * @param maxIterations 最大迭代次数（防止卡顿），推荐 2000~5000
     */
    public NavigationPathfinder(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * 计算从 start 到 goal 的路径。
     *
     * @param start 起点
     * @param goal  终点
     * @return 路径点列表（从起点到终点），找不到路径时返回空列表
     */
    public List<Location> findPath(Location start, Location goal) {
        if (start == null || goal == null) {
            return List.of();
        }
        org.bukkit.World world = start.getWorld();
        if (world == null || !world.equals(goal.getWorld())) {
            return List.of();
        }

        Node startNode = new Node(start.getBlockX(), groundY(world, start.getBlockX(), start.getBlockY(), start.getBlockZ()), start.getBlockZ());
        Node goalNode = new Node(goal.getBlockX(), groundY(world, goal.getBlockX(), goal.getBlockY(), goal.getBlockZ()), goal.getBlockZ());

        if (startNode.equals(goalNode)) {
            return List.of(center(world, startNode));
        }

        java.util.Map<Long, Node> closed = new java.util.HashMap<>();
        java.util.Map<Long, Node> openMap = new java.util.HashMap<>();
        java.util.PriorityQueue<Node> open = new java.util.PriorityQueue<>(java.util.Comparator.comparingDouble(n -> n.f));

        startNode.g = 0;
        startNode.f = heuristic(startNode, goalNode);
        open.add(startNode);
        openMap.put(startNode.key(), startNode);

        int iterations = 0;
        while (!open.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = open.poll();
            openMap.remove(current.key());

            if (current.x == goalNode.x && current.z == goalNode.z) {
                return reconstructPath(world, current);
            }

            closed.put(current.key(), current);

            boolean currentInWater = isSwimming(world, current.x, current.y, current.z);

            for (int[] dir : DIRECTIONS) {
                int nx = current.x + dir[0];
                int nz = current.z + dir[1];
                boolean diagonal = dir[0] != 0 && dir[1] != 0;

                int bestY = -1;
                int bestDy = 0;
                for (int dy : new int[]{0, 1, -1, -2}) {
                    int candidateY = current.y + dy;
                    if (isWalkable(world, nx, candidateY, nz)) {
                        if (dy == 1 && !isPassable(world.getBlockAt(current.x, current.y + 2, current.z))) {
                            continue;
                        }
                        bestY = candidateY;
                        bestDy = dy;
                        break;
                    }
                }
                if (bestY == -1) continue;

                if (diagonal) {
                    boolean side1Passable = isWalkable(world, current.x + dir[0], current.y, current.z)
                        || isWalkable(world, current.x + dir[0], current.y - 1, current.z);
                    boolean side2Passable = isWalkable(world, current.x, current.y, current.z + dir[1])
                        || isWalkable(world, current.x, current.y - 1, current.z + dir[1]);
                    if (!side1Passable && !side2Passable) {
                        continue;
                    }
                }

                boolean neighborInWater = isSwimming(world, nx, bestY, nz);

                if (currentInWater && !neighborInWater) {
                    if (bestY > current.y + 1) continue;
                    if (bestY > current.y && !isPassable(world.getBlockAt(current.x, current.y + 1, current.z))) {
                        continue;
                    }
                }

                Node neighbor = new Node(nx, bestY, nz);
                if (closed.containsKey(neighbor.key())) continue;

                double moveCost = diagonal ? DIAGONAL_COST : STRAIGHT_COST;
                if (bestDy == 1) moveCost += 1.0;
                else if (bestDy == -1) moveCost += 0.3;
                else if (bestDy == -2) moveCost += 0.8;
                if (neighborInWater) moveCost += SWIM_PENALTY;
                double tentativeG = current.g + moveCost;

                tryAddNeighbor(open, openMap, neighbor, tentativeG, goalNode, current);
            }

            if (currentInWater) {
                for (int vertDy : new int[]{1, -1}) {
                    int ny = current.y + vertDy;
                    if (isWalkable(world, current.x, ny, current.z)) {
                        Node vNeighbor = new Node(current.x, ny, current.z);
                        if (!closed.containsKey(vNeighbor.key())) {
                            double vCost = STRAIGHT_COST + SWIM_PENALTY;
                            double vTentativeG = current.g + vCost;
                            tryAddNeighbor(open, openMap, vNeighbor, vTentativeG, goalNode, current);
                        }
                    }
                }
            }
        }

        if (!closed.isEmpty()) {
            Node closestLand = null;
            Node closestAny = null;
            for (Node n : closed.values()) {
                double dist = heuristic(n, goalNode);
                if (dist >= heuristic(startNode, goalNode)) continue;
                if (closestAny == null || dist < heuristic(closestAny, goalNode)) {
                    closestAny = n;
                }
                if (!isSwimming(world, n.x, n.y, n.z)) {
                    if (closestLand == null || dist < heuristic(closestLand, goalNode)) {
                        closestLand = n;
                    }
                }
            }
            Node best = closestLand;
            if (best == null) {
                best = closestAny;
            } else if (closestAny != null && !isSwimming(world, closestAny.x, closestAny.y, closestAny.z)) {
                // closestAny is also land, already in closestLand
            } else if (closestAny != null && heuristic(closestAny, goalNode) < heuristic(best, goalNode) * 0.5) {
                best = closestAny;
            }
            if (best != null) {
                return reconstructPath(world, best);
            }
        }
        return List.of();
    }

    /**
     * 沿路径按指定间距采样点。
     *
     * @param path      路径点列表
     * @param interval  采样间距（方块）
     * @param maxPoints 最大采样点数
     * @return 采样后的路径点列表
     */
    public static List<Location> samplePath(List<Location> path, double interval, int maxPoints) {
        if (path.isEmpty()) return List.of();
        List<Location> result = new java.util.ArrayList<>();
        result.add(path.get(0));

        double accumulated = 0;
        for (int i = 1; i < path.size() && result.size() < maxPoints; i++) {
            Location prev = path.get(i - 1);
            Location curr = path.get(i);
            double segLen = prev.distance(curr);
            accumulated += segLen;

            while (accumulated >= interval && result.size() < maxPoints) {
                accumulated -= interval;
                double ratio = segLen > 0 ? Math.max(0, 1.0 - accumulated / segLen) : 1.0;
                Location point = new Location(
                    curr.getWorld(),
                    prev.getX() + (curr.getX() - prev.getX()) * ratio,
                    prev.getY() + (curr.getY() - prev.getY()) * ratio,
                    prev.getZ() + (curr.getZ() - prev.getZ()) * ratio
                );
                result.add(point);
            }
        }
        return result;
    }

    /**
     * 简化路径：移除共线中间点。
     *
     * @param path 原始路径
     * @return 简化后的路径
     */
    public static List<Location> simplifyPath(List<Location> path) {
        if (path.size() <= 2) return path;
        List<Location> simplified = new java.util.ArrayList<>();
        simplified.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            Location prev = path.get(i - 1);
            Location curr = path.get(i);
            Location next = path.get(i + 1);
            double dx1 = curr.getX() - prev.getX();
            double dz1 = curr.getZ() - prev.getZ();
            double dy1 = curr.getY() - prev.getY();
            double dx2 = next.getX() - curr.getX();
            double dz2 = next.getZ() - curr.getZ();
            double dy2 = next.getY() - curr.getY();

            if (Math.abs(dx1 * dz2 - dz1 * dx2) > 0.01 || Math.abs(dy1 - dy2) > 0.01) {
                simplified.add(curr);
            }
        }
        simplified.add(path.get(path.size() - 1));
        return simplified;
    }

    /**
     * 路径平滑：在路径点之间进行线性插值，生成更密集的路径。
     *
     * @param path        原始路径
     * @param granularity 插值间距（方块），越小越平滑
     * @return 平滑后的路径
     */
    public static List<Location> smoothPath(List<Location> path, double granularity) {
        if (path.size() <= 1 || granularity <= 0) return path;
        List<Location> smoothed = new java.util.ArrayList<>();
        smoothed.add(path.get(0));
        for (int i = 1; i < path.size(); i++) {
            Location prev = path.get(i - 1);
            Location curr = path.get(i);
            double dist = prev.distance(curr);
            int steps = Math.max(1, (int) Math.ceil(dist / granularity));
            for (int s = 1; s <= steps; s++) {
                double t = (double) s / steps;
                smoothed.add(new Location(
                    curr.getWorld(),
                    prev.getX() + (curr.getX() - prev.getX()) * t,
                    prev.getY() + (curr.getY() - prev.getY()) * t,
                    prev.getZ() + (curr.getZ() - prev.getZ()) * t
                ));
            }
        }
        return smoothed;
    }

    /**
     * 在指定中心位置附近生成一个随机可站立地面位置。
     *
     * @param center 中心位置
     * @param radius 搜索半径（方块）
     * @param maxAttempts 最大尝试次数
     * @return 随机地面位置，找不到时返回 null
     */
    public static Location randomGroundLocation(Location center, int radius, int maxAttempts) {
        if (center == null || center.getWorld() == null || radius <= 0 || maxAttempts <= 0) {
            return null;
        }
        org.bukkit.World world = center.getWorld();
        java.util.Random random = new java.util.Random();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            int bx = center.getBlockX() + dx;
            int bz = center.getBlockZ() + dz;
            int by = groundY(world, bx, center.getBlockY(), bz);
            if (isWalkable(world, bx, by, bz)) {
                return new Location(world, bx + 0.5, by, bz + 0.5);
            }
        }
        return null;
    }

    /**
     * 生成朝向目标的随机位置（在目标前方扇形区域）。
     *
     * @param origin  起始位置
     * @param target  目标位置
     * @param minDist 最小距离
     * @param maxDist 最大距离
     * @param spread  扇形角度（度），180=半圆
     * @return 随机位置，找不到时返回 null
     */
    public static Location randomLocationFacingTarget(Location origin, Location target, double minDist, double maxDist, double spread) {
        if (origin == null || target == null || origin.getWorld() == null) return null;
        if (!origin.getWorld().equals(target.getWorld())) return null;
        org.bukkit.World world = origin.getWorld();
        java.util.Random random = new java.util.Random();

        double baseAngle = Math.atan2(target.getZ() - origin.getZ(), target.getX() - origin.getX());
        double spreadRad = Math.toRadians(spread);
        double angle = baseAngle + (random.nextDouble() * 2 - 1) * spreadRad / 2;
        double dist = minDist + random.nextDouble() * (maxDist - minDist);

        int bx = (int) Math.floor(origin.getX() + Math.cos(angle) * dist);
        int bz = (int) Math.floor(origin.getZ() + Math.sin(angle) * dist);
        int by = groundY(world, bx, origin.getBlockY(), bz);
        if (isWalkable(world, bx, by, bz)) {
            return new Location(world, bx + 0.5, by, bz + 0.5);
        }
        return null;
    }

    /**
     * 生成远离目标的随机位置（在目标后方扇形区域）。
     *
     * @param origin  起始位置
     * @param target  目标位置
     * @param minDist 最小距离
     * @param maxDist 最大距离
     * @param spread  扇形角度（度）
     * @return 随机位置，找不到时返回 null
     */
    public static Location randomLocationAwayFromTarget(Location origin, Location target, double minDist, double maxDist, double spread) {
        if (origin == null || target == null || origin.getWorld() == null) return null;
        if (!origin.getWorld().equals(target.getWorld())) return null;
        org.bukkit.World world = origin.getWorld();
        java.util.Random random = new java.util.Random();

        double baseAngle = Math.atan2(origin.getZ() - target.getZ(), origin.getX() - target.getX());
        double spreadRad = Math.toRadians(spread);
        double angle = baseAngle + (random.nextDouble() * 2 - 1) * spreadRad / 2;
        double dist = minDist + random.nextDouble() * (maxDist - minDist);

        int bx = (int) Math.floor(origin.getX() + Math.cos(angle) * dist);
        int bz = (int) Math.floor(origin.getZ() + Math.sin(angle) * dist);
        int by = groundY(world, bx, origin.getBlockY(), bz);
        if (isWalkable(world, bx, by, bz)) {
            return new Location(world, bx + 0.5, by, bz + 0.5);
        }
        return null;
    }

    /**
     * 生成空中随机位置。
     *
     * @param center    中心位置
     * @param radius    水平半径
     * @param minY      最低 Y
     * @param maxY      最高 Y
     * @return 随机空中位置
     */
    public static Location randomAirLocation(Location center, int radius, int minY, int maxY) {
        if (center == null || center.getWorld() == null || radius <= 0) return null;
        org.bukkit.World world = center.getWorld();
        java.util.Random random = new java.util.Random();
        int dx = random.nextInt(radius * 2 + 1) - radius;
        int dz = random.nextInt(radius * 2 + 1) - radius;
        int dy = minY + random.nextInt(Math.max(1, maxY - minY + 1));
        return new Location(world, center.getX() + dx, dy, center.getZ() + dz);
    }

    /**
     * 生成一条短距离方向指引路径（不穿墙，最多 5 格）。
     * 当 A* 完全无法寻路时作为降级方案。
     *
     * @param start 起点
     * @param goal  终点
     * @return 短路径
     */
    public static List<Location> shortDirectionalPath(Location start, Location goal) {
        List<Location> path = new java.util.ArrayList<>();
        path.add(start.clone());
        double dx = goal.getX() - start.getX();
        double dz = goal.getZ() - start.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.5) {
            return path;
        }
        double nx = dx / len;
        double nz = dz / len;
        org.bukkit.World world = start.getWorld();
        for (int i = 1; i <= 5; i++) {
            double px = start.getX() + nx * i;
            double pz = start.getZ() + nz * i;
            int bx = (int) Math.floor(px);
            int bz = (int) Math.floor(pz);
            int by = start.getBlockY();
            if (world != null) {
                org.bukkit.block.Block block = world.getBlockAt(bx, by, bz);
                org.bukkit.block.Block headBlock = world.getBlockAt(bx, by + 1, bz);
                if (block.getType().isSolid() || headBlock.getType().isSolid()) {
                    break;
                }
            }
            path.add(new Location(world, px, start.getY(), pz));
        }
        return path;
    }

    // ==================== 内部方法 ====================

    private static void tryAddNeighbor(
        java.util.PriorityQueue<Node> open, java.util.Map<Long, Node> openMap,
        Node neighbor, double tentativeG, Node goalNode, Node parent
    ) {
        Node existing = openMap.get(neighbor.key());
        if (existing != null && tentativeG >= existing.g) return;

        neighbor.g = tentativeG;
        neighbor.f = tentativeG + heuristic(neighbor, goalNode);
        neighbor.parent = parent;

        if (existing != null) {
            open.remove(existing);
        }
        open.add(neighbor);
        openMap.put(neighbor.key(), neighbor);
    }

    private static boolean isWalkable(org.bukkit.World world, int x, int y, int z) {
        org.bukkit.block.Block feet = world.getBlockAt(x, y, z);
        org.bukkit.block.Block head = world.getBlockAt(x, y + 1, z);
        org.bukkit.block.Block ground = world.getBlockAt(x, y - 1, z);

        if (!isPassable(feet) || !isPassable(head)) return false;
        if (feet.getType() == org.bukkit.Material.WATER) return true;
        if (!isStandable(ground, world, x, y - 1, z)) return false;
        if (isDangerous(ground)) return false;
        return true;
    }

    private static boolean isSwimming(org.bukkit.World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z).getType() == org.bukkit.Material.WATER;
    }

    private static boolean isPassable(org.bukkit.block.Block block) {
        org.bukkit.Material type = block.getType();
        if (type.isAir()) return true;
        if (type == org.bukkit.Material.LAVA) return false;
        if (type == org.bukkit.Material.WATER) return true;
        if (!type.isSolid()) return true;
        if (isPassableBlock(block)) return true;
        return false;
    }

    private static boolean isStandable(org.bukkit.block.Block block, org.bukkit.World world, int bx, int by, int bz) {
        org.bukkit.Material type = block.getType();
        if (type.isAir()) return false;
        if (type == org.bukkit.Material.LAVA) return false;
        if (type.isSolid()) return true;
        if (type == org.bukkit.Material.WATER) {
            org.bukkit.block.Block below = world.getBlockAt(bx, by - 1, bz);
            return below.getType().isSolid() && !isDangerous(below);
        }
        return false;
    }

    private static boolean isPassableBlock(org.bukkit.block.Block block) {
        String name = block.getType().name();
        return name.contains("CARPET")
            || name.contains("PRESSURE_PLATE")
            || name.contains("SIGN")
            || name.contains("BANNER");
    }

    private static boolean isDangerous(org.bukkit.block.Block block) {
        org.bukkit.Material type = block.getType();
        return type == org.bukkit.Material.LAVA
            || type == org.bukkit.Material.CACTUS
            || type == org.bukkit.Material.CAMPFIRE
            || type == org.bukkit.Material.SOUL_CAMPFIRE
            || type == org.bukkit.Material.MAGMA_BLOCK
            || type == org.bukkit.Material.SWEET_BERRY_BUSH
            || type == org.bukkit.Material.WITHER_ROSE
            || type == org.bukkit.Material.POINTED_DRIPSTONE;
    }

    private static int groundY(org.bukkit.World world, int x, int startY, int z) {
        for (int y = startY; y >= Math.max(world.getMinHeight(), startY - 20); y--) {
            if (isWalkable(world, x, y, z)) {
                return y;
            }
        }
        for (int y = startY + 1; y <= startY + 20; y++) {
            if (isWalkable(world, x, y, z)) {
                return y;
            }
        }
        return startY;
    }

    private static double heuristic(Node a, Node b) {
        double dx = Math.abs(a.x - b.x);
        double dz = Math.abs(a.z - b.z);
        double dy = Math.abs(a.y - b.y);
        return (dx + dz) + (DIAGONAL_COST - 2) * Math.min(dx, dz) + dy * 2.0;
    }

    private static List<Location> reconstructPath(org.bukkit.World world, Node node) {
        List<Location> path = new java.util.ArrayList<>();
        Node current = node;
        while (current != null) {
            path.add(center(world, current));
            current = current.parent;
        }
        java.util.Collections.reverse(path);
        return path;
    }

    private static Location center(org.bukkit.World world, Node node) {
        return new Location(world, node.x + 0.5, node.y, node.z + 0.5);
    }

    private static final class Node {
        final int x, y, z;
        double g = Double.MAX_VALUE;
        double f = Double.MAX_VALUE;
        Node parent;

        Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        long key() {
            return ((long) x & 0x3FFFFFF) | (((long) z & 0x3FFFFFF) << 26) | (((long) y & 0xFFF) << 52);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node n)) return false;
            return x == n.x && y == n.y && z == n.z;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(key());
        }
    }
}
