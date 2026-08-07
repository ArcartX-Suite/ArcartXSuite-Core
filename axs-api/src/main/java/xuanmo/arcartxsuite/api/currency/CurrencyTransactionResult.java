package xuanmo.arcartxsuite.api.currency;

/**
 * 货币交易结果记录。
 *
 * @param success 交易是否成功
 * @param message  失败时的提示消息；成功时为空串
 */
public record CurrencyTransactionResult(
    boolean success,
    String message
) {
    /** 返回一个成功结果（无消息）。 */
    public static CurrencyTransactionResult ok() {
        return new CurrencyTransactionResult(true, "");
    }

    /**
     * 返回一个失败结果。
     *
     * @param message 失败提示，null 视为空串
     * @return 失败结果
     */
    public static CurrencyTransactionResult failure(String message) {
        return new CurrencyTransactionResult(false, message == null ? "" : message);
    }
}
