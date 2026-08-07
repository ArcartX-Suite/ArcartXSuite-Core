package xuanmo.arcartxsuite.api.capability;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * 拾取拦截能力接口。
 * <p>
 * 由 Pickup 模块（扫描模式）实现：当扫描模式接管掉落物拾取流程时，
 * 其他模块（如 Warehouse 的自动入库）必须放弃对 {@code EntityPickupItemEvent}
 * 的处理，否则会在扫描面板展示掉落物之前把物品提前收走，导致扫描模式失效。
 */
public interface PickupInterceptor {

    /**
     * 查询该玩家的原版自动拾取是否已被本模块接管。
     *
     * @param playerId 玩家 UUID
     * @return {@code true} 表示拾取流程已被接管，其他模块不应自行处理拾取事件
     */
    boolean isAutoPickupIntercepted(@NotNull UUID playerId);
}
