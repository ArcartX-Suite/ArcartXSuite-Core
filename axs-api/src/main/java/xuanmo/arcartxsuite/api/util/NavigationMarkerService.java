package xuanmo.arcartxsuite.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xuanmo.arcartxsuite.api.bridge.AdyeshachNpcBridgeAPI;

/**
 * 导航路径标记服务 — 使用 Adyeshach 私有临时实体 + ArcartX 模型实现沿路径的导航可视化。
 * <p>
 * 特性:
 * - A* 寻路，智能绕开障碍物/液体/危险方块
 * - 沿路径等间距放置多个模型标记实体
 * - 定时更新：随玩家移动重新计算路径并传送/增减标记
 * - 实体复用池：尽量传送现有实体，减少创建/销毁开销
 * - 支持多模块共享：每个模块用不同的 markerIdPrefix 区分
 *
 * @since 1.5.0
 */
public final class NavigationMarkerService {

    private final JavaPlugin plugin;
    private final AdyeshachNpcBridgeAPI npcBridge;
    private final NavigationMarkerConfig config;
    private final Logger logger;
    private final boolean debug;
    private final String markerIdPrefix;
    private final ConcurrentMap<UUID, PlayerPathState> playerPaths = new ConcurrentHashMap<>();

    private NavigationPathfinder pathfinder;
    private BukkitTask updateTask;
    private boolean available;

    /**
     * 创建导航标记服务。
     *
     * @param plugin          插件实例
     * @param npcBridge       Adyeshach NPC 桥接
     * @param config          标记配置
     * @param logger          日志器
     * @param debug           是否调试模式
     * @param markerIdPrefix  标记 ID 前缀（用于区分不同模块的标记）
     */
    public NavigationMarkerService(
        JavaPlugin plugin,
        AdyeshachNpcBridgeAPI npcBridge,
        NavigationMarkerConfig config,
        Logger logger,
        boolean debug,
        String markerIdPrefix
    ) {
        this.plugin = plugin;
        this.npcBridge = npcBridge;
        this.config = config;
        this.logger = logger;
        this.debug = debug;
        this.markerIdPrefix = markerIdPrefix;
    }

    public void start() {
        if (!config.enabled()) {
            logger.info("导航标记: 已在配置中禁用 (marker.enabled=false)。");
            available = false;
            return;
        }
        if (npcBridge == null) {
            logger.warning("导航标记: npcBridge 为 null，标记功能将跳过。");
            available = false;
            return;
        }
        if (!npcBridge.isAvailable()) {
            logger.warning("导航标记: Adyeshach 不可用 (npcBridge.isAvailable()=false)，标记功能将跳过。");
            available = false;
            return;
        }
        available = true;
        pathfinder = new NavigationPathfinder(config.pathMaxIterations());

        int ticks = Math.max(1, config.pathUpdateTicks());
        updateTask = AxsScheduler.runTaskTimer(plugin, this::tickUpdate, ticks, ticks);

        logger.info("导航标记已启动: model=" + config.modelId() + " scale=" + config.scale()
            + " interval=" + config.pathInterval() + " maxMarkers=" + config.pathMaxMarkers()
            + " updateTicks=" + ticks);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            hideMarker(player);
        }
        playerPaths.clear();
        available = false;
    }

    /**
     * 设置导航目标，启动路径标记跟踪。
     *
     * @param player 玩家
     * @param world  目标世界
     * @param x      目标 X
     * @param y      目标 Y
     * @param z      目标 Z
     */
    public void showMarker(Player player, String world, double x, double y, double z) {
        if (!available || player == null) {
            return;
        }
        hideMarker(player);

        World w = Bukkit.getWorld(world);
        if (w == null) {
            w = player.getWorld();
        }

        Location goal = new Location(w, x, y, z);
        PlayerPathState state = new PlayerPathState(goal, new ArrayList<>(), new ArrayList<>());
        playerPaths.put(player.getUniqueId(), state);

        if (debug) {
            logger.info("路径标记: 开始追踪 player=" + player.getName()
                + " goal=(" + x + "," + y + "," + z + ")");
        }

        AxsScheduler.runTask(plugin, () -> {
            if (!player.isOnline()) return;
            computeAndApplyPath(player, state);
        });
    }

    /**
     * 移除玩家的所有路径标记。
     */
    public void hideMarker(Player player) {
        if (player == null) return;
        PlayerPathState state = playerPaths.remove(player.getUniqueId());
        if (state == null) return;

        state.cancelled = true;

        for (String markerId : state.markerIds) {
            npcBridge.removePrivateMarker(player, markerId);
        }
        state.markerIds.clear();
        state.markerEntities.clear();
    }

    public boolean isAvailable() {
        return available;
    }

    // ==================== 定时更新 ====================

    private void tickUpdate() {
        for (var entry : playerPaths.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                PlayerPathState offlineState = playerPaths.remove(entry.getKey());
                if (offlineState != null) {
                    offlineState.cancelled = true;
                }
                continue;
            }
            PlayerPathState state = entry.getValue();
            if (state.cancelled) continue;
            // tickUpdate 本身已在定时任务（主线程）中运行，无需嵌套调度
            if (player.isOnline() && !state.cancelled) {
                computeAndApplyPath(player, state);
            }
        }
    }

    private void computeAndApplyPath(Player player, PlayerPathState state) {
        try {
            computeAndApplyPathInternal(player, state);
        } catch (Exception ex) {
            if (debug) {
                logger.warning("路径计算异常: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
        }
    }

    private void computeAndApplyPathInternal(Player player, PlayerPathState state) {
        Location playerLoc = player.getLocation();
        Location goal = state.goal;

        if (playerLoc.getWorld() == null || goal.getWorld() == null
            || !playerLoc.getWorld().equals(goal.getWorld())) {
            removeExcessMarkers(player, state, 0);
            return;
        }

        double distance = playerLoc.distance(goal);
        if (distance > config.pathMaxDistance()) {
            removeExcessMarkers(player, state, 0);
            return;
        }

        List<Location> rawPath = pathfinder.findPath(playerLoc, goal);
        if (rawPath.isEmpty()) {
            rawPath = NavigationPathfinder.shortDirectionalPath(playerLoc, goal);
        }

        List<Location> simplified = NavigationPathfinder.simplifyPath(rawPath);
        List<Location> sampled = NavigationPathfinder.samplePath(simplified, config.pathInterval(), config.pathMaxMarkers());

        double yOffset = config.yOffset();
        List<Location> finalPoints = new ArrayList<>(sampled.size());
        for (int i = 0; i < sampled.size(); i++) {
            Location loc = sampled.get(i).clone().add(0, yOffset, 0);
            Location next = (i + 1 < sampled.size()) ? sampled.get(i + 1) : goal;
            double dx = next.getX() - sampled.get(i).getX();
            double dz = next.getZ() - sampled.get(i).getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            loc.setYaw(yaw);
            finalPoints.add(loc);
        }

        // 已在主线程中，无需嵌套调度
        if (!player.isOnline() || state.cancelled) return;
        applyPathMarkers(player, state, finalPoints);
    }

    private void applyPathMarkers(Player player, PlayerPathState state, List<Location> points) {
        int needed = points.size();
        int existing = state.markerIds.size();

        int toTeleport = Math.min(needed, existing);
        for (int i = 0; i < toTeleport; i++) {
            npcBridge.teleportMarker(player, state.markerIds.get(i), points.get(i));
        }

        if (needed > existing) {
            for (int i = existing; i < needed; i++) {
                String markerId = markerIdPrefix + player.getUniqueId().toString().substring(0, 8) + "_" + i;
                Object entity = npcBridge.spawnPrivateMarker(player, markerId, points.get(i));
                if (entity == null) continue;
                state.markerIds.add(markerId);
                state.markerEntities.add(entity);

                final Object ent = entity;
                AxsScheduler.runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    npcBridge.applyModelForPlayer(player, ent, config.modelId(), config.scale());
                    if (config.animation() != null && !config.animation().isBlank()) {
                        npcBridge.applyAnimationForPlayer(player, ent, config.animation(), 1.0, 200, -1);
                    }
                }, 2L);
            }
        }

        removeExcessMarkers(player, state, needed);
    }

    private void removeExcessMarkers(Player player, PlayerPathState state, int keepCount) {
        while (state.markerIds.size() > keepCount) {
            int last = state.markerIds.size() - 1;
            npcBridge.removePrivateMarker(player, state.markerIds.get(last));
            state.markerIds.remove(last);
            if (last < state.markerEntities.size()) {
                state.markerEntities.remove(last);
            }
        }
    }

    // ==================== 玩家状态 ====================

    private static final class PlayerPathState {
        final Location goal;
        final List<String> markerIds;
        final List<Object> markerEntities;
        volatile boolean cancelled;

        PlayerPathState(Location goal, List<String> markerIds, List<Object> markerEntities) {
            this.goal = goal;
            this.markerIds = markerIds;
            this.markerEntities = markerEntities;
        }
    }
}
