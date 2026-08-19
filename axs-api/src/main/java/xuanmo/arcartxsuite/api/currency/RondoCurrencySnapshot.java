package xuanmo.arcartxsuite.api.currency;

import java.math.BigDecimal;
import java.util.Map;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Rondo 单货币经济快照。
 *
 * @param balance      当前余额
 * @param totalEarned  累计收入
 * @param totalSpent   累计支出
 */
@ApiStability.Stable
public record RondoCurrencySnapshot(
    BigDecimal balance,
    BigDecimal totalEarned,
    BigDecimal totalSpent
) {

    /**
     * Rondo 完整经济快照（所有货币）。
     *
     * @param playerUuid  玩家 UUID
     * @param currencies  货币 ID → 货币快照
     * @param updatedAtEpochMillis 更新时间戳（毫秒）
     */
    @ApiStability.Stable
    public record Full(
        java.util.UUID playerUuid,
        Map<String, RondoCurrencySnapshot> currencies,
        long updatedAtEpochMillis
    ) {
        /** 获取指定货币的余额，不存在时返回 null */
        public BigDecimal balance(String currencyId) {
            RondoCurrencySnapshot snapshot = currencies.get(currencyId);
            return snapshot != null ? snapshot.balance() : null;
        }
    }
}
