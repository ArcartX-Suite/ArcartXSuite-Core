package xuanmo.arcartxsuite.api.capability.mail;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件发送结果。
 * <p>
 * 由 {@link MailDispatchable#sendMail(MailSendRequest)} 和
 * {@link MailDispatchable#dispatchPreset(MailPresetRequest)} 返回。
 * 调用方根据 {@link #success()} 判断是否成功，失败时通过 {@link #message()} 获取原因。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class MailSendResult {

    private final boolean successful;
    private final @Nullable String message;

    private MailSendResult(boolean successful, @Nullable String message) {
        this.successful = successful;
        this.message = message;
    }

    public static @NotNull MailSendResult ok() {
        return new MailSendResult(true, null);
    }

    public static @NotNull MailSendResult ok(@Nullable String message) {
        return new MailSendResult(true, message);
    }

    public static @NotNull MailSendResult failure(@NotNull String reason) {
        Objects.requireNonNull(reason, "reason");
        return new MailSendResult(false, reason);
    }

    public boolean success() { return successful; }

    public @Nullable String message() { return message; }
}
