package xuanmo.arcartxsuite.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * 统一调度器，兼容 Folia / Paper / Spigot。
 * <p>
 * 参考 Asteroid 的 FoliaScheduler 设计，在 AXS 内部实现，不引入外部依赖。
 * Folia 环境下通过反射调用 Folia 的各调度器；非 Folia 环境直接委托 Bukkit.getScheduler()。
 * 所有 runTask* 方法返回 BukkitTask，Folia 环境下用 FoliaTaskWrapper 包装。
 */
public final class AxsScheduler {

    private AxsScheduler() {}

    // ── Folia 反射方法缓存（避免每次调用都反射查找）──
    private static volatile boolean foliaReflectInit = false;
    private static Method FOLIA_GET_GLOBAL_REGION_SCHEDULER;
    private static Method FOLIA_GET_REGION_SCHEDULER;
    private static Method FOLIA_GET_ASYNC_SCHEDULER;
    private static Method FOLIA_GLOBAL_RUN;
    private static Method FOLIA_GLOBAL_RUN_DELAYED;
    private static Method FOLIA_GLOBAL_RUN_AT_FIXED_RATE;
    private static Method FOLIA_REGION_RUN;
    private static Method FOLIA_ASYNC_RUN_NOW;
    private static Method FOLIA_ASYNC_RUN_DELAYED;
    private static Method FOLIA_ASYNC_RUN_AT_FIXED_RATE;
    private static Method FOLIA_ENTITY_GET_SCHEDULER;
    private static Method FOLIA_ENTITY_RUN;

    // ── FoliaTaskWrapper 反射方法按类缓存（Folia task 类编译时不可用）──
    private static final Map<Class<?>, Method> WRAPPER_METHOD_CACHE = new ConcurrentHashMap<>();

    private static void initFoliaReflect() {
        if (foliaReflectInit) return;
        synchronized (AxsScheduler.class) {
            if (foliaReflectInit) return;
            try {
                FOLIA_GET_GLOBAL_REGION_SCHEDULER = Bukkit.class.getMethod("getGlobalRegionScheduler");
                FOLIA_GET_REGION_SCHEDULER = Bukkit.class.getMethod("getRegionScheduler");
                FOLIA_GET_ASYNC_SCHEDULER = Bukkit.class.getMethod("getAsyncScheduler");
                Class<?> globalSchedClass = FOLIA_GET_GLOBAL_REGION_SCHEDULER.getReturnType();
                FOLIA_GLOBAL_RUN = globalSchedClass.getMethod("run", Plugin.class, Consumer.class);
                FOLIA_GLOBAL_RUN_DELAYED = globalSchedClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                FOLIA_GLOBAL_RUN_AT_FIXED_RATE = globalSchedClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                Class<?> regionSchedClass = FOLIA_GET_REGION_SCHEDULER.getReturnType();
                FOLIA_REGION_RUN = regionSchedClass.getMethod("run", Plugin.class, Location.class, Consumer.class);
                Class<?> asyncSchedClass = FOLIA_GET_ASYNC_SCHEDULER.getReturnType();
                FOLIA_ASYNC_RUN_NOW = asyncSchedClass.getMethod("runNow", Plugin.class, Consumer.class);
                FOLIA_ASYNC_RUN_DELAYED = asyncSchedClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
                FOLIA_ASYNC_RUN_AT_FIXED_RATE = asyncSchedClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
                // 实体调度：Entity.getScheduler() → EntityScheduler.run(Plugin, Consumer, Runnable)
                FOLIA_ENTITY_GET_SCHEDULER = Entity.class.getMethod("getScheduler");
                Class<?> entitySchedClass = FOLIA_ENTITY_GET_SCHEDULER.getReturnType();
                FOLIA_ENTITY_RUN = entitySchedClass.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to cache Folia scheduler methods", e);
            }
            foliaReflectInit = true;
        }
    }

    // ── 同步任务 ──────────────────────────────────────────────

    public static BukkitTask runTask(Plugin plugin, Runnable task) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaGlobalRun(plugin, task), true);
        }
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * 延迟执行同步任务。
     *
     * @param delayTicks 延迟 tick 数。Folia GlobalRegionScheduler.runDelayed 的单位也是 tick，无需换算。
     */
    public static BukkitTask runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaGlobalRunDelayed(plugin, task, delayTicks), true);
        }
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaGlobalRunAtFixedRate(plugin, task, delayTicks, periodTicks), true);
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    // ── 实体/区域相关同步任务 ─────────────────────────────────

    public static BukkitTask runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaEntityRun(plugin, entity, task), true);
        }
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    public static BukkitTask runLocationTask(Plugin plugin, Location location, Runnable task) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaRegionRun(plugin, location, task), true);
        }
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    // ── 异步任务 ──────────────────────────────────────────────

    public static BukkitTask runAsync(Plugin plugin, Runnable task) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaAsyncRunNow(plugin, task), false);
        }
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * 延迟执行异步任务。
     *
     * @param delayTicks 延迟 tick 数。Folia AsyncScheduler.runDelayed 接受 TimeUnit，
     *                   此处将 tick × 50ms 换算为毫秒后传入。
     */
    public static BukkitTask runAsyncLater(Plugin plugin, Runnable task, long delayTicks) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaAsyncRunDelayed(plugin, task, delayTicks * 50, TimeUnit.MILLISECONDS), false);
        }
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    /**
     * 定时循环执行异步任务。
     *
     * @param delayTicks  首次延迟 tick 数（Folia 异步调度器按毫秒处理，内部 × 50 换算）
     * @param periodTicks 循环间隔 tick 数（同上）
     */
    public static BukkitTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (PlatformCompat.isFolia()) {
            return wrap(foliaAsyncRunAtFixedRate(plugin, task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS), false);
        }
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    // ── 取消任务 ──────────────────────────────────────────────

    public static void cancelTask(Object taskHandle) {
        if (taskHandle == null) return;
        if (taskHandle instanceof BukkitTask) {
            ((BukkitTask) taskHandle).cancel();
        } else {
            try {
                Method cancelMethod = taskHandle.getClass().getMethod("cancel");
                cancelMethod.invoke(taskHandle);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "[AXS] Failed to cancel task", e);
            }
        }
    }

    public static void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public static <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task) {
        if (PlatformCompat.isFolia()) {
            return foliaCallSyncMethod(plugin, task);
        }
        return Bukkit.getScheduler().callSyncMethod(plugin, task);
    }

    // ── Folia 包装 ────────────────────────────────────────────

    private static BukkitTask wrap(Object foliaHandle) {
        return wrap(foliaHandle, false);
    }

    private static BukkitTask wrap(Object foliaHandle, boolean sync) {
        if (foliaHandle == null) return new NoOpBukkitTask(sync);
        if (foliaHandle instanceof BukkitTask) return (BukkitTask) foliaHandle;
        return new FoliaTaskWrapper(foliaHandle, sync);
    }

    /**
     * Folia 调度失败时的 no-op 占位，避免调用方拿到 null 后 cancel() NPE。
     */
    private static final class NoOpBukkitTask implements BukkitTask {
        private final boolean sync;
        private volatile boolean cancelled = true;

        NoOpBukkitTask(boolean sync) {
            this.sync = sync;
        }

        @Override public int getTaskId() { return -1; }
        @Override public Plugin getOwner() { return null; }
        @Override public void cancel() { cancelled = true; }
        @Override public boolean isCancelled() { return cancelled; }
        @Override public boolean isSync() { return sync; }
    }

    private static final class FoliaTaskWrapper implements BukkitTask {
        private final Object handle;
        private final Method cancelMethod;
        private final Method getTaskIdMethod;
        private final Method getOwnerMethod;
        private final boolean sync;
        private volatile boolean cancelled;

        FoliaTaskWrapper(Object handle, boolean sync) {
            this.handle = handle;
            this.sync = sync;
            this.cancelMethod = cacheWrapperMethod(handle, "cancel");
            this.getTaskIdMethod = cacheWrapperMethod(handle, "getTaskId");
            this.getOwnerMethod = cacheWrapperMethod(handle, "getOwner");
        }

        private static Method cacheWrapperMethod(Object handle, String name) {
            Class<?> cls = handle.getClass();
            return WRAPPER_METHOD_CACHE.computeIfAbsent(cls, c -> {
                try {
                    return c.getMethod(name);
                } catch (Exception ignored) {
                    return null;
                }
            });
        }

        @Override public int getTaskId() {
            if (getTaskIdMethod != null) {
                try { return (int) getTaskIdMethod.invoke(handle); } catch (Exception ignored) {}
            }
            return -1;
        }

        @Override public Plugin getOwner() {
            if (getOwnerMethod != null) {
                try { return (Plugin) getOwnerMethod.invoke(handle); } catch (Exception ignored) {}
            }
            return null;
        }

        @Override public void cancel() {
            if (cancelled) return;
            cancelled = true;
            if (cancelMethod != null) {
                try { cancelMethod.invoke(handle); } catch (Exception ignored) {}
            }
        }

        @Override public boolean isCancelled() {
            return cancelled;
        }

        @Override public boolean isSync() {
            return sync;
        }
    }

    // ── Folia 反射调用 ────────────────────────────────────────

    private static Object foliaGlobalRun(Plugin plugin, Runnable task) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_GLOBAL_REGION_SCHEDULER == null || FOLIA_GLOBAL_RUN == null) return null;
            Object scheduler = FOLIA_GET_GLOBAL_REGION_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_GLOBAL_RUN.invoke(scheduler, plugin, consumer);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia GlobalRegionScheduler.run", e);
            return null;
        }
    }

    /**
     * Folia GlobalRegionScheduler.runDelayed，延迟单位为 tick（与 Bukkit 一致，无需换算）。
     */
    private static Object foliaGlobalRunDelayed(Plugin plugin, Runnable task, long delayTicks) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_GLOBAL_REGION_SCHEDULER == null || FOLIA_GLOBAL_RUN_DELAYED == null) return null;
            Object scheduler = FOLIA_GET_GLOBAL_REGION_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_GLOBAL_RUN_DELAYED.invoke(scheduler, plugin, consumer, Math.max(1, delayTicks));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia GlobalRegionScheduler.runDelayed", e);
            return null;
        }
    }

    private static Object foliaGlobalRunAtFixedRate(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_GLOBAL_REGION_SCHEDULER == null || FOLIA_GLOBAL_RUN_AT_FIXED_RATE == null) return null;
            Object scheduler = FOLIA_GET_GLOBAL_REGION_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_GLOBAL_RUN_AT_FIXED_RATE.invoke(scheduler, plugin, consumer, Math.max(1, delayTicks), Math.max(1, periodTicks));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia GlobalRegionScheduler.runAtFixedRate", e);
            return null;
        }
    }

    private static Object foliaEntityRun(Plugin plugin, Entity entity, Runnable task) {
        initFoliaReflect();
        try {
            if (FOLIA_ENTITY_GET_SCHEDULER == null || FOLIA_ENTITY_RUN == null) return null;
            Object scheduler = FOLIA_ENTITY_GET_SCHEDULER.invoke(entity);
            Consumer<Object> consumer = t -> task.run();
            Runnable retired = () -> {};
            return FOLIA_ENTITY_RUN.invoke(scheduler, plugin, consumer, retired);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia EntityScheduler.run", e);
            return null;
        }
    }

    private static Object foliaRegionRun(Plugin plugin, Location location, Runnable task) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_REGION_SCHEDULER == null || FOLIA_REGION_RUN == null) return null;
            Object scheduler = FOLIA_GET_REGION_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_REGION_RUN.invoke(scheduler, plugin, location, consumer);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia RegionScheduler.run", e);
            return null;
        }
    }

    private static Object foliaAsyncRunNow(Plugin plugin, Runnable task) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_ASYNC_SCHEDULER == null || FOLIA_ASYNC_RUN_NOW == null) return null;
            Object scheduler = FOLIA_GET_ASYNC_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_ASYNC_RUN_NOW.invoke(scheduler, plugin, consumer);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia AsyncScheduler.runNow", e);
            return null;
        }
    }

    private static Object foliaAsyncRunDelayed(Plugin plugin, Runnable task, long delay, TimeUnit unit) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_ASYNC_SCHEDULER == null || FOLIA_ASYNC_RUN_DELAYED == null) return null;
            Object scheduler = FOLIA_GET_ASYNC_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_ASYNC_RUN_DELAYED.invoke(scheduler, plugin, consumer, delay, unit);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia AsyncScheduler.runDelayed", e);
            return null;
        }
    }

    private static Object foliaAsyncRunAtFixedRate(Plugin plugin, Runnable task, long delay, long period, TimeUnit unit) {
        initFoliaReflect();
        try {
            if (FOLIA_GET_ASYNC_SCHEDULER == null || FOLIA_ASYNC_RUN_AT_FIXED_RATE == null) return null;
            Object scheduler = FOLIA_GET_ASYNC_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> task.run();
            return FOLIA_ASYNC_RUN_AT_FIXED_RATE.invoke(scheduler, plugin, consumer, Math.max(1, delay), Math.max(1, period), unit);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia AsyncScheduler.runAtFixedRate", e);
            return null;
        }
    }

    private static <T> Future<T> foliaCallSyncMethod(Plugin plugin, Callable<T> task) {
        initFoliaReflect();
        java.util.concurrent.CompletableFuture<T> future = new java.util.concurrent.CompletableFuture<>();
        try {
            if (FOLIA_GET_GLOBAL_REGION_SCHEDULER == null || FOLIA_GLOBAL_RUN == null) {
                future.completeExceptionally(new IllegalStateException("Folia scheduler methods not available"));
                return future;
            }
            Object scheduler = FOLIA_GET_GLOBAL_REGION_SCHEDULER.invoke(null);
            Consumer<Object> consumer = t -> {
                try {
                    future.complete(task.call());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            };
            FOLIA_GLOBAL_RUN.invoke(scheduler, plugin, consumer);
            return future;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[AXS] Failed to invoke Folia callSyncMethod", e);
            future.completeExceptionally(e);
            return future;
        }
    }
}
