package xuanmo.arcartxsuite.api.currency;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Rondo 转账结果。
 *
 * @param success    是否成功
 * @param message    结果消息
 * @param taxAmount  扣除的税费
 */
@ApiStability.Stable
public record RondoTransferResult(
    boolean success,
    String message,
    java.math.BigDecimal taxAmount
) {}
