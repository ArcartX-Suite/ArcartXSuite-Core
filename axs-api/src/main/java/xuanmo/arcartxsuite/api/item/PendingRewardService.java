package xuanmo.arcartxsuite.api.item;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 统一待发放奖励服务。
 * <p>
 * 由宿主（axs-core）提供单例实现，模块通过 {@code context.pendingRewardService()} 获取。
 * <p>
 * 统一行为：<b>模块调用 {@link #savePending} 暂存离线/失败的奖励 →
 * 玩家上线时宿主自动加载并补发 → 物品通过 {@link ItemRewardDispatcher} 发放、
 * 货币通过 CurrencyBridge 入账、命令通过控制台执行、邮件预设通过 MailDispatchable 发送</b>。
 * <p>
 * 各模块不再自行实现 pending 存储和补发逻辑，统一使用本服务。
 * 后续修改补发流程（如接入第三方邮箱插件）只需改本体实现。
 *
 * <h3>线程安全</h3>
 * {@link #savePending} 可在任意线程调用（内部异步写入数据库）。
 * 补发逻辑由宿主在主线程执行（PlayerJoinEvent 触发）。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public interface PendingRewardService {

    /**
     * 暂存一条待发放奖励。
     *
     * @param reward 奖励描述（不为 null）
     */
    void savePending(@NotNull PendingReward reward);

    /**
     * 加载指定玩家的所有待发放奖励（按创建时间升序）。
     * <p>
     * 主要供宿主补发逻辑调用；模块一般无需直接调用。
     *
     * @param playerUuid 玩家 UUID
     * @return 待发放奖励列表，无数据时返回空列表
     */
    @NotNull List<PendingReward> loadPending(@NotNull UUID playerUuid);

    /**
     * 加载指定模块下指定玩家的待发放奖励。
     *
     * @param sourceModule 来源模块 ID
     * @param playerUuid   玩家 UUID
     * @return 待发放奖励列表，无数据时返回空列表
     */
    @NotNull List<PendingReward> loadPending(@NotNull String sourceModule, @NotNull UUID playerUuid);

    /**
     * 标记一条待发放奖励已成功补发（删除记录）。
     *
     * @param id 记录 ID
     */
    void markDelivered(long id);

    /**
     * 递增补发尝试次数（补发失败时调用，达到阈值后自动丢弃避免无限重试）。
     *
     * @param id 记录 ID
     */
    void incrementAttempts(long id);

    /**
     * 待发放奖励数据模型。
     * <p>
     * 使用 JSON payload 存储奖励详情，按 {@link #type()} 序列化不同字段。
     * 各奖励类型的 payload 字段约定：
     * <ul>
     *   <li>{@link Type#ITEM} — {@code {"source":"minecraft","id":"DIAMOND","amount":1,"nbt":""}}</li>
     *   <li>{@link Type#ITEM_SERIALIZED} — {@code {"data":"<Base64 序列化的 ItemStack>"}}</li>
     *   <li>{@link Type#CURRENCY} — {@code {"currencyId":"coin","amount":100}}</li>
     *   <li>{@link Type#COMMAND} — {@code {"commands":["give {player} diamond 1"]}}</li>
     *   <li>{@link Type#MAIL_PRESET} — {@code {"presetIds":["preset1","preset2"]}}</li>
     * </ul>
     *
     * @param id           记录唯一 ID（自增，新建时传 0）
     * @param playerUuid   玩家 UUID
     * @param sourceModule 来源模块 ID（如 "market"、"fishing"）
     * @param type         奖励类型
     * @param payload      JSON 格式的奖励详情
     * @param description  奖励描述（用于日志和通知）
     * @param createdAt    创建时间戳（毫秒）
     * @param attempts     补发尝试次数
     */
    record PendingReward(
        long id,
        @NotNull UUID playerUuid,
        @NotNull String sourceModule,
        @NotNull Type type,
        @NotNull String payload,
        @Nullable String description,
        long createdAt,
        int attempts
    ) {

        /**
         * 奖励类型枚举。
         */
        public enum Type {
            /** 物品奖励（通过 ItemSourceRegistry 生成 + ItemRewardDispatcher 发放） */
            ITEM,
            /** 已序列化的物品奖励（Base64 ItemStack，反序列化后通过 ItemRewardDispatcher 发放） */
            ITEM_SERIALIZED,
            /** 货币奖励（通过 CurrencyBridge 入账） */
            CURRENCY,
            /** 命令奖励（通过控制台执行） */
            COMMAND,
            /** 邮件预设奖励（通过 MailDispatchable 发送） */
            MAIL_PRESET
        }

        /**
         * 创建待发放物品奖励的便捷构造。
         */
        public static @NotNull PendingReward item(@NotNull UUID playerUuid, @NotNull String sourceModule,
                                                  @NotNull String source, @NotNull String itemId,
                                                  int amount, @Nullable String nbt,
                                                  @Nullable String description) {
            String payload = "{\"source\":\"" + escapeJson(source) + "\","
                + "\"id\":\"" + escapeJson(itemId) + "\","
                + "\"amount\":" + amount + ","
                + "\"nbt\":\"" + (nbt != null ? escapeJson(nbt) : "") + "\"}";
            return new PendingReward(0, playerUuid, sourceModule, Type.ITEM, payload, description,
                System.currentTimeMillis(), 0);
        }

        /**
         * 创建待发放货币奖励的便捷构造。
         */
        public static @NotNull PendingReward currency(@NotNull UUID playerUuid, @NotNull String sourceModule,
                                                      @NotNull String currencyId, double amount,
                                                      @Nullable String description) {
            String payload = "{\"currencyId\":\"" + escapeJson(currencyId) + "\","
                + "\"amount\":" + amount + "}";
            return new PendingReward(0, playerUuid, sourceModule, Type.CURRENCY, payload, description,
                System.currentTimeMillis(), 0);
        }

        /**
         * 创建待发放命令奖励的便捷构造。
         */
        public static @NotNull PendingReward command(@NotNull UUID playerUuid, @NotNull String sourceModule,
                                                     @NotNull List<String> commands,
                                                     @Nullable String description) {
            StringBuilder sb = new StringBuilder("{\"commands\":[");
            for (int i = 0; i < commands.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append('"').append(escapeJson(commands.get(i))).append('"');
            }
            sb.append("]}");
            return new PendingReward(0, playerUuid, sourceModule, Type.COMMAND, sb.toString(), description,
                System.currentTimeMillis(), 0);
        }

        /**
         * 创建待发放邮件预设奖励的便捷构造。
         */
        public static @NotNull PendingReward mailPreset(@NotNull UUID playerUuid, @NotNull String sourceModule,
                                                        @NotNull List<String> presetIds,
                                                        @Nullable String description) {
            StringBuilder sb = new StringBuilder("{\"presetIds\":[");
            for (int i = 0; i < presetIds.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append('"').append(escapeJson(presetIds.get(i))).append('"');
            }
            sb.append("]}");
            return new PendingReward(0, playerUuid, sourceModule, Type.MAIL_PRESET, sb.toString(), description,
                System.currentTimeMillis(), 0);
        }

        /**
         * 创建待发放已序列化物品奖励的便捷构造。
         * <p>
         * 用于 market/lottery 等模块存储已序列化的 ItemStack（Base64），
         * 补发时反序列化后通过 ItemRewardDispatcher 发放。
         */
        public static @NotNull PendingReward itemSerialized(@NotNull UUID playerUuid, @NotNull String sourceModule,
                                                            @NotNull String base64Data,
                                                            @Nullable String description) {
            String payload = "{\"data\":\"" + escapeJson(base64Data) + "\"}";
            return new PendingReward(0, playerUuid, sourceModule, Type.ITEM_SERIALIZED, payload, description,
                System.currentTimeMillis(), 0);
        }

        private static @NotNull String escapeJson(@NotNull String value) {
            StringBuilder sb = new StringBuilder(value.length() + 8);
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                        else sb.append(c);
                    }
                }
            }
            return sb.toString();
        }
    }
}
