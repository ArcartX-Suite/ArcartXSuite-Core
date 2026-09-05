package xuanmo.arcartxsuite.api.item;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 统一物品来源注册表。
 * <p>
 * 宿主维护单例实例，涵盖所有已对接的外部物品插件（MythicMobs、NeigeItems、Overture、MMOItems 等）。
 * 模块通过 {@code context.itemSourceRegistry()} 获取，不再各自创建桥接。
 */
@ApiStability.Stable
public interface ItemSourceRegistry {

    // ─── 物品识别 ─────────────────────────────────────────────────

    /** 获取 MythicMobs 物品 ID，非 Mythic 物品返回空串 */
    String mythicItemId(ItemStack itemStack);

    /** 获取 NeigeItems 物品 ID，非 Neige 物品返回空串 */
    String neigeItemId(ItemStack itemStack);

    /** 获取 Overture 物品 ID，非 Overture 物品返回空串 */
    String overtureItemId(ItemStack itemStack);

    /** 获取 MMOItems 物品 ID（格式 "类型ID;物品ID"），非 MMOItems 物品返回空串 */
    String mmoItemId(ItemStack itemStack);

    /** 判断是否为 MythicMobs 物品（透传 MythicBukkit 原生 isMythicItem，比取 ID 更轻量） */
    boolean isMythicItem(ItemStack itemStack);

    /**
     * 判断是否为 NeigeItems 物品。
     * <p>
     * 与 Mythic/Overture/MMOItems 不同：NeigeItems 原生 ItemManager 未提供独立的轻量判断方法，
     * 因此本方法实现为 {@code !neigeItemId(itemStack).isBlank()} 的包装，无额外性能收益，
     * 仅为保持四个物品源在"判断"能力上的接口对称，便于模块作者统一写法。
     */
    boolean isNeigeItem(ItemStack itemStack);

    /** 判断是否为 Overture 物品（透传 OvertureAPI 原生 isOvertureItem，比取 ID 更轻量） */
    boolean isOvertureItem(ItemStack itemStack);

    /** 判断是否为 MMOItems 物品（仅检查 NBTItem.hasType()，比取完整 ID 少一次 NBT 字段读取） */
    boolean isMmoItem(ItemStack itemStack);

    // ─── 物品生成 ─────────────────────────────────────────────────

    /**
     * 统一物品生成入口。根据 source 分发到对应桥接生成物品。
     * <p>
     * 支持的 source 值（不区分大小写）：
     * <ul>
     *   <li>{@code minecraft} — 原版材质，id 为 Material 名（如 DIAMOND），nbt 为可选 SNBT</li>
     *   <li>{@code mythic} — MythicMobs，id 为物品内部 ID</li>
     *   <li>{@code neige} — NeigeItems，id 为物品内部 ID</li>
     *   <li>{@code overture} — Overture，id 为物品模板 ID（需要 player 上下文）</li>
     *   <li>{@code mmoitems} — MMOItems，id 格式为 {@code "类型ID;物品ID"}（如 "SWORD;EXCALIBUR"）</li>
     * </ul>
     * 后续接入新物品源只需在本体实现中扩展此方法，各模块无需修改。
     *
     * @param source 物品来源（minecraft / mythic / neige / overture / mmoitems）
     * @param id     物品 ID（mmoitems 格式为 "类型ID;物品ID"）
     * @param amount 数量（&ge; 1）
     * @param nbt    自定义 NBT（SNBT 字符串，仅 minecraft 源有效），可为 null
     * @param player 玩家上下文（Overture 等需要），可为 null
     * @return 生成的物品，失败返回 null
     */
    @Nullable ItemStack generateItem(@NotNull String source, @NotNull String id, int amount,
                                     @Nullable String nbt, @Nullable Player player);

    /** 通过 MythicMobs ID 生成物品 */
    @Nullable ItemStack generateMythicItem(String itemId, int amount);

    /** 通过 NeigeItems ID 生成物品 */
    @Nullable ItemStack generateNeigeItem(String itemId, int amount);

    /** 通过 Overture ID 生成物品（需要玩家上下文） */
    @Nullable ItemStack generateOvertureItem(String itemId, @Nullable Player player, int amount);

    /** 通过 MMOItems 类型+ID 生成物品 */
    @Nullable ItemStack generateMmoItem(String typeId, String itemId, int amount);

    // ─── Overture 扩展查询 ────────────────────────────────────────

    /** 获取 Overture 物品模板显示名，Overture 不可用或 ID 不存在时返回 null */
    @Nullable String overtureItemDisplayName(String itemId);

    /** 获取 Overture 物品模板描述行，Overture 不可用或 ID 不存在时返回 null */
    @Nullable List<String> overtureItemDisplayLore(String itemId);

    /** 获取 Overture 物品模板副本（仅展示用，不含实例数据），Overture 不可用或 ID 不存在时返回 null */
    @Nullable ItemStack overtureTemplateItem(String itemId);

    /** 获取所有已注册的 Overture 物品 ID，Overture 不可用时返回空列表 */
    List<String> overtureItemIds();

    // ─── Overture 序列化 ──────────────────────────────────────────

    /** 使用 Overture 原生序列化将 ItemStack 序列化为 JSON 字符串，Overture 不可用时返回 null */
    @Nullable String overtureSerialize(ItemStack item);

    /** 使用 Overture 原生反序列化从 JSON 字符串恢复 ItemStack，Overture 不可用或数据无效时返回 null */
    @Nullable ItemStack overtureDeserialize(String json);

    // ─── 可用性查询 ───────────────────────────────────────────────

    boolean mythicBridgeAvailable();

    boolean neigeBridgeAvailable();

    boolean overtureBridgeAvailable();

    boolean mmoBridgeAvailable();
}
