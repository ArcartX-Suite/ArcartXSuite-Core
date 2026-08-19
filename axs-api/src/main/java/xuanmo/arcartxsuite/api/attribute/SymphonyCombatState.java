package xuanmo.arcartxsuite.api.attribute;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 战斗状态信息。
 *
 * @param active          是否处于战斗状态
 * @param remainingMillis 战斗状态剩余时间（毫秒），0 表示已脱离战斗
 */
@ApiStability.Stable
public record SymphonyCombatState(
    boolean active,
    long remainingMillis
) {}
