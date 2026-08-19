package xuanmo.arcartxsuite.api.capability;

import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ItemBridgeAPI;

/**
 * Tooltip 数据查询能力接口。
 * <p>
 * 由 Tooltip 模块实现，供 Chat 等模块查询客户端采集的动态 tooltip 数据
 * （TACZ 枪属性、Apotheosis affix 等）。
 * <p>
 * 客户端 Forge mod（ArcartX-Suite-Mod）在 {@code ItemTooltipEvent} 中
 * 采集 tooltip 文本行和结构化数据，通过 ArcartX 自定义网络包发送给服务端，
 * 由 Tooltip 模块缓存并提供给其他模块使用。
 *
 * @since 1.1.0
 */
public interface TooltipDataCapable {

    /**
     * 获取玩家当前手持物品的 tooltip 文本行。
     * <p>
     * 文本行由客户端 mod 采集，包含 TACZ 枪属性和 Apotheosis affix 等动态内容。
     *
     * @param player 玩家
     * @return tooltip 文本行列表，无数据时返回空列表
     */
    @NotNull List<String> getTooltipLines(@NotNull Player player);

    /**
     * 获取玩家指定物品的 tooltip 文本行。
     *
     * @param player    玩家（用于确定数据来源）
     * @param itemStack 物品栈
     * @return tooltip 文本行列表，无数据时返回空列表
     */
    @NotNull List<String> getTooltipLines(@NotNull Player player, @NotNull ItemStack itemStack);

    /**
     * 获取玩家当前手持物品的结构化数据（JSON 字符串）。
     * <p>
     * 包含 TACZ 枪属性的数值字段（damage、armorIgnore、headshotMultiplier 等），
     * 供业务逻辑使用。
     *
     * @param player 玩家
     * @return 结构化数据 JSON 字符串，无数据时返回 "{}"
     */
    @NotNull String getStructuredData(@NotNull Player player);

    /**
     * 将 tooltip 文本行注入物品的 NBT Lore 标签。
     * <p>
     * 用于聊天物品预览等场景：在 {@link ItemBridgeAPI#itemToJson} 序列化前
     * 调用此方法，使客户端能显示动态 tooltip 内容。
     *
     * @param itemStack 物品栈
     * @param lines     tooltip 文本行（将写入 {@code display.Lore} NBT 标签）
     * @return 注入 Lore 后的物品（可能与入参为同一实例）
     */
    @NotNull ItemStack injectLore(@NotNull ItemStack itemStack, @NotNull List<String> lines);

    /**
     * 判断指定物品是否有缓存的 tooltip 数据。
     *
     * @param player    玩家
     * @param itemStack 物品栈
     * @return {@code true} 表示有缓存的 tooltip 数据
     */
    boolean hasTooltipData(@NotNull Player player, @NotNull ItemStack itemStack);
}
