package xuanmo.arcartxsuite.api.item;

import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 统一物品奖励发放器。
 * <p>
 * 由宿主（axs-core）提供单例实现，模块通过 {@code context.itemRewardDispatcher()} 获取。
 * <p>
 * 统一行为：<b>放入背包 → 背包满时溢出物品通过 Mail 模块发邮件补发 → Mail 模块不可用时溢出物品掉落地面</b>。
 * <p>
 * 预设邮件（{@code mail-presets}）与指令（{@code commands}）属于独立的奖励类型，
 * 不由本接口处理；模块应分别通过 {@link xuanmo.arcartxsuite.api.capability.MailDispatchable}
 * 与 {@link org.bukkit.Bukkit#dispatchCommand} 统一调用。
 *
 * <h3>线程安全</h3>
 * {@link #dispatchItem} 与 {@link #dispatchItems} 内部会自动调度到主线程执行物品栏操作，
 * 可在任意线程调用。邮件发送在异步线程执行，掉落地面在主线程执行。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public interface ItemRewardDispatcher {

    /**
     * 向在线玩家发放单个物品。
     * <p>
     * 行为：放入玩家背包；背包满时，溢出物品通过 Mail 模块以物品附件邮件补发；
     * Mail 模块不可用或邮件发送失败时，溢出物品掉落在玩家脚下。
     *
     * @param player  在线玩家
     * @param item    要发放的物品（不为 null，amount &gt; 0）
     * @param options 发放选项（来源模块、溢出邮件文案等）
     * @return 发放结果
     */
    @NotNull DispatchResult dispatchItem(@NotNull Player player, @NotNull ItemStack item, @NotNull DispatchOptions options);

    /**
     * 向在线玩家批量发放物品。
     * <p>
     * 等价于对每个物品调用 {@link #dispatchItem}，结果聚合返回。
     *
     * @param player  在线玩家
     * @param items   要发放的物品列表（不为 null，不含 null 元素）
     * @param options 发放选项
     * @return 聚合发放结果
     */
    @NotNull DispatchResult dispatchItems(@NotNull Player player, @NotNull List<ItemStack> items, @NotNull DispatchOptions options);

    /**
     * 发放选项。
     * <p>
     * 用于标识奖励来源（便于审计与邮件 sourceDetail），以及配置背包满时溢出邮件的主题/正文/发件人名。
     * 当未提供溢出邮件文案时，使用实现默认文案。
     */
    final class DispatchOptions {

        private final @NotNull String sourceModule;
        private final @Nullable String sourceDetail;
        private final @Nullable String overflowMailSubject;
        private final @Nullable String overflowMailBody;
        private final @Nullable String overflowMailSenderName;
        private final boolean skipWarehouseOverflow;
        private final boolean skipPendingOverflow;

        private DispatchOptions(@NotNull String sourceModule, @Nullable String sourceDetail,
                                @Nullable String overflowMailSubject, @Nullable String overflowMailBody,
                                @Nullable String overflowMailSenderName, boolean skipWarehouseOverflow,
                                boolean skipPendingOverflow) {
            this.sourceModule = Objects.requireNonNull(sourceModule, "sourceModule");
            this.sourceDetail = sourceDetail;
            this.overflowMailSubject = overflowMailSubject;
            this.overflowMailBody = overflowMailBody;
            this.overflowMailSenderName = overflowMailSenderName;
            this.skipWarehouseOverflow = skipWarehouseOverflow;
            this.skipPendingOverflow = skipPendingOverflow;
        }

        /** 来源模块 ID（如 "battlepass"、"lottery"） */
        public @NotNull String sourceModule() { return sourceModule; }

        /** 来源详情（如 "overflow:level-10"），可为 null */
        public @Nullable String sourceDetail() { return sourceDetail; }

        /** 溢出邮件主题，为 null 时使用实现默认文案 */
        public @Nullable String overflowMailSubject() { return overflowMailSubject; }

        /** 溢出邮件正文，为 null 时使用实现默认文案 */
        public @Nullable String overflowMailBody() { return overflowMailBody; }

        /** 溢出邮件发件人名，为 null 时使用实现默认文案 */
        public @Nullable String overflowMailSenderName() { return overflowMailSenderName; }

        /**
         * 是否跳过仓库兜底。
         * <p>
         * 为 true 时，背包满后不尝试存入仓库，直接走邮件兜底。
         * mail 模块领取邮件附件等场景应设为 true，避免邮件附件又被存入仓库的循环。
         * 默认为 false（优先尝试仓库兜底）。
         *
         * @since 1.5.0
         */
        public boolean skipWarehouseOverflow() { return skipWarehouseOverflow; }

        /**
         * 是否跳过 PendingRewardService 兜底。
         * <p>
         * 为 true 时，邮件发送失败后不尝试存入 PendingRewardService，直接掉落地面。
         * PendingRewardService 补发物品时应设为 true，避免补发失败再次存入 pending 形成循环。
         * 默认为 false（优先尝试暂存 pending 待下次补发）。
         *
         * @since 1.5.0
         */
        public boolean skipPendingOverflow() { return skipPendingOverflow; }

        /**
         * 创建构建器。
         *
         * @param sourceModule 来源模块 ID
         * @return 构建器
         */
        public static @NotNull Builder builder(@NotNull String sourceModule) {
            return new Builder(sourceModule);
        }

        /** 选项构建器。 */
        public static final class Builder {
            private final @NotNull String sourceModule;
            private @Nullable String sourceDetail;
            private @Nullable String overflowMailSubject;
            private @Nullable String overflowMailBody;
            private @Nullable String overflowMailSenderName;
            private boolean skipWarehouseOverflow;
            private boolean skipPendingOverflow;

            private Builder(@NotNull String sourceModule) {
                this.sourceModule = Objects.requireNonNull(sourceModule, "sourceModule");
            }

            public @NotNull Builder sourceDetail(@Nullable String sourceDetail) {
                this.sourceDetail = sourceDetail;
                return this;
            }

            public @NotNull Builder overflowMailSubject(@Nullable String subject) {
                this.overflowMailSubject = subject;
                return this;
            }

            public @NotNull Builder overflowMailBody(@Nullable String body) {
                this.overflowMailBody = body;
                return this;
            }

            public @NotNull Builder overflowMailSenderName(@Nullable String senderName) {
                this.overflowMailSenderName = senderName;
                return this;
            }

            /**
             * 设置是否跳过仓库兜底。
             * <p>
             * mail 模块领取邮件附件等场景应设为 true。
             * 默认为 false。
             *
             * @param skip 是否跳过仓库兜底
             * @return 构建器
             * @since 1.5.0
             */
            public @NotNull Builder skipWarehouseOverflow(boolean skip) {
                this.skipWarehouseOverflow = skip;
                return this;
            }

            /**
             * 设置是否跳过 PendingRewardService 兜底。
             * <p>
             * PendingRewardService 补发物品时应设为 true，避免补发失败再次存入 pending 形成循环。
             * 默认为 false。
             *
             * @param skip 是否跳过 PendingRewardService 兜底
             * @return 构建器
             * @since 1.5.0
             */
            public @NotNull Builder skipPendingOverflow(boolean skip) {
                this.skipPendingOverflow = skip;
                return this;
            }

            public @NotNull DispatchOptions build() {
                return new DispatchOptions(sourceModule, sourceDetail,
                    overflowMailSubject, overflowMailBody, overflowMailSenderName,
                    skipWarehouseOverflow, skipPendingOverflow);
            }
        }
    }

    /**
     * 发放结果。
     *
     * @param success         是否全部成功（至少有一件物品被发放/邮寄/掉落/暂存即视为成功）
     * @param deliveredCount  直接放入背包的物品件数
     * @param warehousedCount 存入仓库的物品件数
     * @param mailedCount     通过邮件补发的物品件数
     * @param pendingCount    暂存到 PendingRewardService 的物品件数
     * @param droppedCount    掉落地面的物品件数
     * @param message         结果描述（失败原因或成功摘要）
     */
    record DispatchResult(boolean success, int deliveredCount, int warehousedCount, int mailedCount,
                           int pendingCount, int droppedCount, @NotNull String message) {

        /** 空结果（未发放任何物品） */
        public static @NotNull DispatchResult empty() {
            return new DispatchResult(true, 0, 0, 0, 0, 0, "");
        }

        /** 合并两个结果 */
        public @NotNull DispatchResult merge(@NotNull DispatchResult other) {
            return new DispatchResult(
                this.success && other.success,
                this.deliveredCount + other.deliveredCount,
                this.warehousedCount + other.warehousedCount,
                this.mailedCount + other.mailedCount,
                this.pendingCount + other.pendingCount,
                this.droppedCount + other.droppedCount,
                this.message.isEmpty() ? other.message : (other.message.isEmpty() ? this.message : this.message + "; " + other.message)
            );
        }
    }
}
