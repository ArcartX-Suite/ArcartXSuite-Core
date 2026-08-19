package xuanmo.arcartxsuite.api.currency;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Rondo 排行榜条目。
 *
 * @param rank        排名（从 1 开始）
 * @param playerUuid  玩家 UUID
 * @param playerName  玩家名称
 * @param balance     余额
 */
@ApiStability.Stable
public record RondoRankingEntry(
    int rank,
    java.util.UUID playerUuid,
    String playerName,
    java.math.BigDecimal balance
) {}
