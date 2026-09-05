package xuanmo.arcartxsuite.api.scheduler;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 已调度的任务句柄，用于取消任务。
 *
 * @since 1.6.0
 */
@ApiStability.Stable
public interface ScheduledTask {

    /** 取消该任务，停止后续执行。 */
    void cancel();

    /** 是否已取消。 */
    boolean isCancelled();
}
