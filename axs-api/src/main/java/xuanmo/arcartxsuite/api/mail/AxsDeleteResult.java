package xuanmo.arcartxsuite.api.mail;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件删除结果。
 * <p>
 * 由 {@link AxsMailService#delete(java.util.UUID, long)} 返回。
 *
 * @param success 是否删除成功
 * @param message 结果消息
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsDeleteResult(
    boolean success,
    @Nullable String message
) {
    /** 删除成功 */
    public static @NotNull AxsDeleteResult ok() {
        return new AxsDeleteResult(true, null);
    }

    /** 删除失败 */
    public static @NotNull AxsDeleteResult failure(@NotNull String reason) {
        Objects.requireNonNull(reason, "reason");
        return new AxsDeleteResult(false, reason);
    }

    /** 邮件不存在 */
    public static @NotNull AxsDeleteResult notFound() {
        return new AxsDeleteResult(false, "NOT_FOUND");
    }

    /** 存在受保护附件（未领取），无法删除 */
    public static @NotNull AxsDeleteResult protectedAttachments() {
        return new AxsDeleteResult(false, "PROTECTED_ATTACHMENTS");
    }
}
