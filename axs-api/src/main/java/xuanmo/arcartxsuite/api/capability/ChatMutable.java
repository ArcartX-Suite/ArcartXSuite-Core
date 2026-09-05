package xuanmo.arcartxsuite.api.capability;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 聊天禁言能力接口。
 * <p>
 * 由 Chat 模块实现并注册，供 Essentials 等模块通过 capability 调用以执行禁言/解禁操作。
 * 当 Chat 模块未启用时，调用方应做降级处理。
 */
public interface ChatMutable {

    /**
     * 禁言指定玩家。
     *
     * @param playerName 目标玩家名称
     * @param expiresAt  过期时间，null 表示永久禁言
     * @param reason     禁言原因，可为 null
     * @param mutedBy    执行者名称，可为 null
     * @return 操作结果消息（成功或失败原因）
     */
    @NotNull
    String mutePlayer(@NotNull String playerName, @Nullable Instant expiresAt, @Nullable String reason, @Nullable String mutedBy);

    /**
     * 解除指定玩家的禁言。
     *
     * @param playerName 目标玩家名称
     * @return 操作结果消息（成功或失败原因）
     */
    @NotNull
    String unmutePlayer(@NotNull String playerName);

    /**
     * 查询玩家是否当前处于禁言状态。
     *
     * @param playerUuid 目标玩家 UUID
     * @return 是否被禁言
     */
    boolean isMuted(@NotNull UUID playerUuid);

    /**
     * 列出当前所有处于生效状态的禁言记录。
     * <p>
     * 仅返回缓存中未过期的禁言记录。离线玩家的禁言可能不在缓存中，
     * 调用方不应将此结果视为完整的持久化禁言列表。
     *
     * @return 禁言信息列表，无禁言时返回空列表
     */
    @NotNull
    List<MuteInfo> listMutes();

    /**
     * 禁言信息摘要，用于跨模块传递禁言列表数据。
     *
     * @param playerUuid 被禁言玩家 UUID
     * @param playerName 被禁言玩家名称（可能为 null 如果无法解析）
     * @param mutedBy    执行禁言的操作者名称
     * @param reason     禁言原因
     * @param createdAt  禁言创建时间
     * @param expiresAt  禁言过期时间，null 表示永久禁言
     */
    record MuteInfo(
        @NotNull UUID playerUuid,
        @Nullable String playerName,
        @Nullable String mutedBy,
        @Nullable String reason,
        @Nullable Instant createdAt,
        @Nullable Instant expiresAt
    ) {
        /**
         * 判断禁言是否为永久禁言。
         *
         * @return {@code true} 表示永久禁言
         */
        public boolean isPermanent() {
            return expiresAt == null;
        }
    }
}
