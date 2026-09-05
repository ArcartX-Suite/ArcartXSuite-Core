package xuanmo.arcartxsuite.api.capability;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 模块间解耦事件总线能力接口（pub/sub 模式）。
 * <p>
 * 任何模块都可以发布事件（publish），其他模块订阅感兴趣的事件主题。
 * 事件主题以字符串标识（如 {@code "market.purchase"}、{@code "mail.received"}）。
 * <p>
 * 与 {@link SignalDispatchable} 的区别：
 * <ul>
 *   <li>SignalDispatchable 面向 EventPacket 规则引擎（1 对 1）</li>
 *   <li>EventBusCapability 面向所有模块（1 对多 pub/sub）</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 发布方（Market 模块）
 * eventBus.publish("market.purchase", player, Map.of("price", "1000", "item", "钻石剑"));
 *
 * // 订阅方（QQBot 模块）
 * eventBus.subscribe("market.purchase", (event) -> {
 *     qqBot.sendToAllGroups("[交易] " + event.player().getName() + " 购买了物品");
 * });
 * }</pre>
 */
public interface EventBusCapability {

    /**
     * 发布一个事件到总线（类型安全版本）。
     * <p>
     * payload 可以是任意类型（如自定义 record、BigDecimal、ItemStack 等），
     * 订阅方通过 {@link BusEvent#typedPayload()} 获取并按需强制转换。
     *
     * @param topic     事件主题
     * @param player    关联玩家（可能为 null，如系统事件）
     * @param payload   附加数据（任意类型）
     */
    void publish(@NotNull String topic, @Nullable Player player, @NotNull Object payload);

    /**
     * 发布一个事件到总线（Map<String,String> 版本，向后兼容）。
     * <p>
     * 此方法委托给 {@link #publish(String, Player, Object)}，仅用于兼容旧调用方。
     * 新代码建议直接使用 {@link #publish(String, Player, Object)} 传递类型安全的 payload。
     *
     * @param topic     事件主题
     * @param player    关联玩家（可能为 null，如系统事件）
     * @param payload   附加数据（字符串键值对）
     */
    default void publish(@NotNull String topic, @Nullable Player player, @NotNull Map<String, String> payload) {
        publish(topic, player, (Object) payload);
    }

    /**
     * 注册当前模块为指定主题的发布者。
     * <p>
     * 模块在 {@code onEnable} 时调用此方法声明自己会发布的事件主题，
     * 以便其他模块在启动时通过 {@link #hasPublisher(String)} 检测是否可订阅。
     * <p>
     * 重复注册同一主题是安全的（幂等）。
     *
     * @param topic 事件主题
     */
    @ApiStability.Stable
    void registerPublisher(@NotNull String topic);

    /**
     * 查询指定主题是否已有注册的发布者。
     * <p>
     * 用于在启动时判断某个 EventBus 主题是否有模块发布，
     * 若无则可降级到其他事件源（如原版 Bukkit 事件）。
     *
     * @param topic 事件主题
     * @return {@code true} 表示至少有一个模块已注册为该主题的发布者
     */
    @ApiStability.Stable
    boolean hasPublisher(@NotNull String topic);

    /**
     * 订阅一个事件主题。
     *
     * @param topic    事件主题（支持通配符 {@code "market.*"}）
     * @param handler  处理器
     * @return 订阅 ID（用于取消订阅）
     */
    String subscribe(@NotNull String topic, @NotNull EventHandler handler);

    /**
     * 取消订阅。
     *
     * @param subscriptionId 订阅 ID
     */
    void unsubscribe(@NotNull String subscriptionId);

    /**
     * 事件数据载体。
     * <p>
     * {@link #payload()} 返回 {@code Map<String, String>} 视图（向后兼容），
     * 当原始 payload 不是 Map 时返回空 Map。
     * {@link #typedPayload()} 返回原始 payload 对象（类型安全，订阅方按需强制转换）。
     */
    record BusEvent(
        @NotNull String topic,
        @Nullable Player player,
        @NotNull Object typedPayload,
        long timestamp
    ) {
        /**
         * 返回 {@code Map<String, String>} 视图的 payload。
         * 当原始 payload 不是 {@code Map<String, String>} 时返回空 Map。
         */
        @SuppressWarnings("unchecked")
        @NotNull
        public Map<String, String> payload() {
            if (typedPayload instanceof Map<?, ?> map) {
                try {
                    return (Map<String, String>) map;
                } catch (ClassCastException ignored) {
                    return Map.of();
                }
            }
            return Map.of();
        }

        /**
         * 返回类型安全的原始 payload 对象。订阅方按需强制转换。
         *
         * @return 原始 payload
         */
        @NotNull
        public Object typedPayload() {
            return typedPayload;
        }
    }

    /**
     * 事件处理器。
     */
    @FunctionalInterface
    interface EventHandler {
        void handle(@NotNull BusEvent event);
    }
}
