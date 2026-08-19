package xuanmo.arcartxsuite.api.currency;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Rondo 交易日志条目。
 *
 * @param currencyId    货币 ID
 * @param action        交易动作（DEPOSIT/WITHDRAW/TRANSFER_IN/TRANSFER_OUT/SET/EXCHANGE_IN/EXCHANGE_OUT）
 * @param amount        交易金额
 * @param balanceAfter  交易后余额
 * @param source        资金来源标识
 * @param timestamp     时间戳（毫秒）
 */
@ApiStability.Stable
public record RondoTransactionLog(
    String currencyId,
    String action,
    java.math.BigDecimal amount,
    java.math.BigDecimal balanceAfter,
    String source,
    long timestamp
) {}
