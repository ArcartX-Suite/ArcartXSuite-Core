package xuanmo.arcartxsuite.api.capability.mail;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 预设邮件派发请求。
 * <p>
 * 传给 {@link MailDispatchable#dispatchPreset(MailPresetRequest)}。
 * 收件人用玩家名（预设支持按名解析离线玩家）。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class MailPresetRequest {

    private final @NotNull String presetId;
    private final @NotNull String playerName;
    private final @NotNull String sourceModule;
    private final @Nullable String sourceDetail;

    private MailPresetRequest(@NotNull String presetId, @NotNull String playerName,
                              @NotNull String sourceModule, @Nullable String sourceDetail) {
        this.presetId = presetId;
        this.playerName = playerName;
        this.sourceModule = sourceModule;
        this.sourceDetail = sourceDetail;
    }

    public static @NotNull Builder builder(@NotNull String presetId, @NotNull String playerName) {
        Objects.requireNonNull(presetId, "presetId");
        Objects.requireNonNull(playerName, "playerName");
        if (presetId.isBlank()) {
            throw new IllegalArgumentException("presetId 不能为空");
        }
        if (playerName.isBlank()) {
            throw new IllegalArgumentException("playerName 不能为空");
        }
        return new Builder(presetId, playerName);
    }

    public @NotNull String presetId() { return presetId; }

    public @NotNull String playerName() { return playerName; }

    public @NotNull String sourceModule() { return sourceModule; }

    public @Nullable String sourceDetail() { return sourceDetail; }

    public static final class Builder {
        private final @NotNull String presetId;
        private final @NotNull String playerName;
        private @NotNull String sourceModule = "unknown";
        private @Nullable String sourceDetail;

        private Builder(@NotNull String presetId, @NotNull String playerName) {
            this.presetId = presetId;
            this.playerName = playerName;
        }

        public @NotNull Builder sourceModule(@NotNull String sourceModule) {
            this.sourceModule = Objects.requireNonNull(sourceModule, "sourceModule");
            return this;
        }

        public @NotNull Builder sourceDetail(@Nullable String sourceDetail) {
            this.sourceDetail = sourceDetail;
            return this;
        }

        public @NotNull MailPresetRequest build() {
            return new MailPresetRequest(presetId, playerName, sourceModule, sourceDetail);
        }
    }
}
