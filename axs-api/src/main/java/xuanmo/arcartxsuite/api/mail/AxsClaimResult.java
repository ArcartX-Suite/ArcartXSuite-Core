package xuanmo.arcartxsuite.api.mail;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件领取结果。
 * <p>
 * 由 {@link AxsMailService#claim(java.util.UUID, long)} 返回。
 *
 * @param success 是否领取成功
 * @param message 结果消息（失败原因或成功提示）
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsClaimResult(
    boolean success,
    @Nullable String message
) {
    /** 领取成功 */
    public static @NotNull AxsClaimResult ok() {
        return new AxsClaimResult(true, null);
    }

    /** 领取成功（带消息） */
    public static @NotNull AxsClaimResult ok(@Nullable String message) {
        return new AxsClaimResult(true, message);
    }

    /** 领取失败 */
    public static @NotNull AxsClaimResult failure(@NotNull String reason) {
        Objects.requireNonNull(reason, "reason");
        return new AxsClaimResult(false, reason);
    }

    /** 邮件不存在 */
    public static @NotNull AxsClaimResult notFound() {
        return new AxsClaimResult(false, "NOT_FOUND");
    }

    /** 无可领取附件 */
    public static @NotNull AxsClaimResult nothingClaimable() {
        return new AxsClaimResult(false, "NOTHING_CLAIMABLE");
    }

    /** 玩家离线（领取需要在线发放物品） */
    public static @NotNull AxsClaimResult playerOffline() {
        return new AxsClaimResult(false, "PLAYER_OFFLINE");
    }

    /** 背包已满 */
    public static @NotNull AxsClaimResult inventoryFull() {
        return new AxsClaimResult(false, "INVENTORY_FULL");
    }
}
