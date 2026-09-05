package xuanmo.arcartxsuite.api.mail;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件视图，对外公开的只读邮件信息。
 * <p>
 * 由 {@link AxsMailService#getMail}、{@link AxsMailService#getInbox}、
 * {@link AxsMailService#markRead} 返回。
 * 不暴露内部持久化模型，仅提供外部插件所需的展示和判断字段。
 *
 * @param id          邮件 ID（模块内自增 long）
 * @param ownerUuid   收件人 UUID
 * @param senderName  发件人显示名
 * @param subject     邮件标题
 * @param body        邮件正文（含换行符）
 * @param state       邮件状态
 * @param attachments 附件列表（只读）
 * @param createdAt   创建时间
 * @param expiresAt   过期时间，{@code null} 表示永不过期
 * @param claimedAt   领取时间，{@code null} 表示未领取
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsMailView(
    long id,
    @NotNull UUID ownerUuid,
    @Nullable String senderName,
    @NotNull String subject,
    @NotNull String body,
    @NotNull AxsMailState state,
    @NotNull List<AxsMailAttachmentView> attachments,
    @NotNull Instant createdAt,
    @Nullable Instant expiresAt,
    @Nullable Instant claimedAt
) {
    /**
     * 判断邮件是否未读。
     *
     * @return {@code true} 表示未读
     */
    public boolean unread() {
        return state == AxsMailState.UNREAD;
    }

    /**
     * 判断邮件是否可领取（未读或已读且未领取/删除/过期）。
     *
     * @return {@code true} 表示可领取
     */
    public boolean claimable() {
        return state == AxsMailState.UNREAD || state == AxsMailState.READ;
    }

    /**
     * 判断邮件是否含附件。
     *
     * @return {@code true} 表示有附件
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
}
