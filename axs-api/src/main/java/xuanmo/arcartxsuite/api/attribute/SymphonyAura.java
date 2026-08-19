package xuanmo.arcartxsuite.api.attribute;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 光环信息。
 *
 * @param channel        光环通道
 * @param amount         光环数值
 * @param remainingMillis 剩余时间（毫秒），-1 表示永久
 */
@ApiStability.Stable
public record SymphonyAura(
    String channel,
    double amount,
    long remainingMillis
) {}
