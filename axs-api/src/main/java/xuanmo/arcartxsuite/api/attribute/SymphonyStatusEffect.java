package xuanmo.arcartxsuite.api.attribute;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 运行时状态效果。
 *
 * @param id             状态效果 ID
 * @param stacks         当前层数
 * @param remainingMillis 剩余时间（毫秒），-1 表示永久
 */
@ApiStability.Stable
public record SymphonyStatusEffect(
    String id,
    int stacks,
    long remainingMillis
) {}
