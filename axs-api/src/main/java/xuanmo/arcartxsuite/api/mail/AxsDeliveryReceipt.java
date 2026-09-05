package xuanmo.arcartxsuite.api.mail;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件发送回执。
 * <p>
 * 由 {@link AxsMailService#send(AxsMailRequest)} 和
 * {@link AxsMailService#sendDurable(AxsMailRequest)} 返回。
 * 调用方根据 {@link #mailId()} 判断是否成功创建（{@code mailId > 0} 表示成功）。
 *
 * @param mailId         邮件 ID，{@code <= 0} 表示空操作（模块未就绪或业务门关闭）
 * @param recipientCount 收件人数量
 * @param duplicate      是否为重复发送（相同 idempotencyKey）
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsDeliveryReceipt(
    long mailId,
    int recipientCount,
    boolean duplicate
) {
    /**
     * 空操作回执（模块未就绪或业务门关闭时返回）。
     *
     * @return 空回执
     */
    public static @NotNull AxsDeliveryReceipt noOp() {
        return new AxsDeliveryReceipt(0L, 0, true);
    }

    /**
     * 成功回执。
     *
     * @param mailId         邮件 ID
     * @param recipientCount 收件人数量
     * @return 成功回执
     */
    public static @NotNull AxsDeliveryReceipt ok(long mailId, int recipientCount) {
        return new AxsDeliveryReceipt(mailId, recipientCount, false);
    }

    /**
     * 重复发送回执。
     *
     * @param mailId 原邮件 ID
     * @return 重复回执
     */
    public static @NotNull AxsDeliveryReceipt duplicate(long mailId) {
        return new AxsDeliveryReceipt(mailId, 0, true);
    }

    /**
     * 判断是否为空操作（模块未就绪或业务门关闭）。
     *
     * @return {@code true} 表示空操作
     */
    public boolean isNoOp() {
        return mailId <= 0 && recipientCount == 0 && duplicate;
    }
}
