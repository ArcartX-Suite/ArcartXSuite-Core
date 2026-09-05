package xuanmo.arcartxsuite.api.mail;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件公开服务接口，面向外部 Bukkit 插件。
 * <p>
 * 所有方法均返回 {@link CompletableFuture}，异步执行，不阻塞调用线程。
 * 调用方不应在 Bukkit 主线程对返回值调用 {@code join()} 或 {@code get()}，
 * 应使用 {@code thenAccept}、{@code whenComplete} 等回调处理结果。
 * <p>
 * 获取方式：
 * <pre>{@code
 * if (!AxsMailApi.isReady()) return;
 * AxsMailService service = AxsMailApi.service();
 * }</pre>
 * 或通过 Bukkit ServicesManager：
 * <pre>{@code
 * RegisteredServiceProvider<AxsMailService> reg =
 *     Bukkit.getServicesManager().getRegistration(AxsMailService.class);
 * if (reg == null) return;
 * AxsMailService service = reg.getProvider();
 * }</pre>
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public interface AxsMailService {

    /**
     * 发送一封系统邮件。
     * <p>
     * 收件人无需在线，邮件存入收件箱，玩家上线后查看领取附件。
     * 使用 {@link AxsMailRequest#idempotencyKey()} 作为去重标识。
     *
     * @param request 邮件发送请求
     * @return 异步回执，{@link AxsDeliveryReceipt#isNoOp()} 为 true 表示模块未就绪
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsDeliveryReceipt> send(@NotNull AxsMailRequest request);

    /**
     * 恢复持久化的邮件发送意图。
     * <p>
     * 语义与 {@link #send} 相同，区别在于允许更早的 {@code deliverAt}
     * （用于调用方从自己的持久化记录恢复发送）。
     * AXS mail 模块当前实现中与 {@link #send} 行为一致。
     *
     * @param request 邮件发送请求
     * @return 异步回执
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsDeliveryReceipt> sendDurable(@NotNull AxsMailRequest request);

    /**
     * 查询玩家自己的单封邮件。
     *
     * @param playerId 玩家 UUID
     * @param mailId   邮件 ID
     * @return 异步邮件视图，不存在时返回 {@code null}
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsMailView> getMail(@NotNull UUID playerId, long mailId);

    /**
     * 查询玩家收件箱。
     *
     * @param playerId 玩家 UUID
     * @param query    查询条件
     * @return 异步邮件视图列表
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<List<AxsMailView>> getInbox(@NotNull UUID playerId, @NotNull AxsMailQuery query);

    /**
     * 查询玩家未读邮件数量。
     *
     * @param playerId 玩家 UUID
     * @return 异步未读数量
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<Integer> getUnreadCount(@NotNull UUID playerId);

    /**
     * 标记邮件为已读并返回详情。
     * <p>
     * 如果邮件已经是已读或更后状态，不改变状态但仍返回邮件视图。
     *
     * @param playerId 玩家 UUID
     * @param mailId   邮件 ID
     * @return 异步邮件视图，不存在时返回 {@code null}
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsMailView> markRead(@NotNull UUID playerId, long mailId);

    /**
     * 领取邮件的全部可领附件。
     * <p>
     * 领取物品附件需要玩家在线（发放到背包）。玩家离线时返回
     * {@link AxsClaimResult#playerOffline()}。
     *
     * @param playerId 玩家 UUID
     * @param mailId   邮件 ID
     * @return 异步领取结果
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsClaimResult> claim(@NotNull UUID playerId, long mailId);

    /**
     * 删除没有受保护附件的邮件。
     * <p>
     * 还有未领取附件时返回 {@link AxsDeleteResult#protectedAttachments()}。
     *
     * @param playerId 玩家 UUID
     * @param mailId   邮件 ID
     * @return 异步删除结果
     */
    @ApiStability.Stable
    @NotNull CompletableFuture<AxsDeleteResult> delete(@NotNull UUID playerId, long mailId);
}
