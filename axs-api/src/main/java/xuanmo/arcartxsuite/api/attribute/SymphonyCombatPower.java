package xuanmo.arcartxsuite.api.attribute;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 战力计算快照。
 *
 * @param rawValue         原始战力值
 * @param value            最终战力值
 * @param formatted        格式化字符串
 * @param calculatedAtMillis 计算时间戳（毫秒）
 * @param error            计算错误信息，null 表示成功
 */
@ApiStability.Stable
public record SymphonyCombatPower(
    double rawValue,
    double value,
    String formatted,
    long calculatedAtMillis,
    String error
) {
    public boolean successful() { return error == null; }
}
