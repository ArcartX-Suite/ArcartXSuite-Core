package xuanmo.arcartxsuite.api.capability.mail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件发送请求。
 * <p>
 * 传给 {@link MailDispatchable#sendMail(MailSendRequest)}。
 * 收件人用 UUID（直接落库，UUID 是唯一标识）。
 * 附件支持物品+货币混合（{@link MailAttachmentSpec} 列表）。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class MailSendRequest {

    private final @NotNull UUID playerUuid;
    private final @NotNull String subject;
    private final @NotNull String body;
    private final @NotNull List<MailAttachmentSpec> attachments;
    private final @Nullable String senderName;
    private final @NotNull String sourceModule;
    private final @Nullable String sourceDetail;
    private final @Nullable Instant expiresAt;

    private MailSendRequest(@NotNull UUID playerUuid, @NotNull String subject, @NotNull String body,
                            @NotNull List<MailAttachmentSpec> attachments, @Nullable String senderName,
                            @NotNull String sourceModule, @Nullable String sourceDetail,
                            @Nullable Instant expiresAt) {
        this.playerUuid = playerUuid;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
        this.senderName = senderName;
        this.sourceModule = sourceModule;
        this.sourceDetail = sourceDetail;
        this.expiresAt = expiresAt;
    }

    public static @NotNull Builder builder(@NotNull UUID playerUuid, @NotNull String subject, @NotNull String body) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(body, "body");
        if (subject.isBlank()) {
            throw new IllegalArgumentException("subject 不能为空");
        }
        if (body.isBlank()) {
            throw new IllegalArgumentException("body 不能为空");
        }
        return new Builder(playerUuid, subject, body);
    }

    public @NotNull UUID playerUuid() { return playerUuid; }

    public @NotNull String subject() { return subject; }

    public @NotNull String body() { return body; }

    public @NotNull List<MailAttachmentSpec> attachments() { return attachments; }

    public @Nullable String senderName() { return senderName; }

    public @NotNull String sourceModule() { return sourceModule; }

    public @Nullable String sourceDetail() { return sourceDetail; }

    public @Nullable Instant expiresAt() { return expiresAt; }

    public static final class Builder {
        private final @NotNull UUID playerUuid;
        private final @NotNull String subject;
        private final @NotNull String body;
        private final @NotNull List<MailAttachmentSpec> attachments = new ArrayList<>();
        private @Nullable String senderName;
        private @NotNull String sourceModule = "unknown";
        private @Nullable String sourceDetail;
        private @Nullable Instant expiresAt;

        private Builder(@NotNull UUID playerUuid, @NotNull String subject, @NotNull String body) {
            this.playerUuid = playerUuid;
            this.subject = subject;
            this.body = body;
        }

        public @NotNull Builder attachment(@NotNull MailAttachmentSpec attachment) {
            this.attachments.add(Objects.requireNonNull(attachment, "attachment"));
            return this;
        }

        public @NotNull Builder attachments(@NotNull List<MailAttachmentSpec> attachments) {
            this.attachments.clear();
            this.attachments.addAll(Objects.requireNonNull(attachments, "attachments"));
            return this;
        }

        public @NotNull Builder senderName(@Nullable String senderName) {
            this.senderName = senderName;
            return this;
        }

        public @NotNull Builder sourceModule(@NotNull String sourceModule) {
            this.sourceModule = Objects.requireNonNull(sourceModule, "sourceModule");
            return this;
        }

        public @NotNull Builder sourceDetail(@Nullable String sourceDetail) {
            this.sourceDetail = sourceDetail;
            return this;
        }

        public @NotNull Builder expiresAt(@Nullable Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public @NotNull MailSendRequest build() {
            return new MailSendRequest(
                playerUuid, subject, body,
                Collections.unmodifiableList(new ArrayList<>(attachments)),
                senderName, sourceModule, sourceDetail, expiresAt
            );
        }
    }
}
