package xuanmo.arcartxsuite.api.currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Rondo 经济系统桥接。
 * <p>
 * 提供 Rondo 原生的排行榜、转账、经济快照和交易日志能力。
 * 模块通过 {@code context.rondoBridge()} 获取。
 * Rondo 未安装时 {@link #available()} 返回 false，所有查询返回空/null。
 */
@ApiStability.Stable
public interface RondoBridge {

    boolean available();

    // ─── 排行榜 ──────────────────────────────────────────────

    /** 获取指定货币的排行榜（分页），Rondo 不可用时返回空列表 */
    List<RondoRankingEntry> ranking(String currencyId, int page, int pageSize);

    /** 获取玩家在指定货币的排名，未上榜或 Rondo 不可用时返回 null */
    @Nullable Integer playerRank(UUID playerUuid, String currencyId);

    // ─── 转账 ────────────────────────────────────────────────

    /** 玩家间转账（含税率、原子性），Rondo 不可用时返回失败结果 */
    RondoTransferResult transfer(UUID from, UUID to, String currencyId, BigDecimal amount);

    // ─── 经济快照 ────────────────────────────────────────────

    /** 非阻塞探测玩家经济快照（纯缓存读取，不访问数据库），未命中或 Rondo 不可用时返回 null */
    @Nullable BigDecimal peekBalance(UUID playerUuid, String currencyId);

    /** 异步获取玩家完整经济快照（所有货币），Rondo 不可用时返回已完成的 null future */
    CompletableFuture<RondoCurrencySnapshot.Full> economySnapshot(UUID playerUuid);

    // ─── 交易日志 ────────────────────────────────────────────

    /** 查询玩家交易流水（分页），currencyId 为 null 时查询所有货币，Rondo 不可用时返回空列表 */
    List<RondoTransactionLog> log(UUID playerUuid, @Nullable String currencyId, int page, int pageSize);
}
