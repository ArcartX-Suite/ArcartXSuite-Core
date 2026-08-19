package xuanmo.arcartxsuite.api.item;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    /** 判断是否为 MythicMobs 物品 */
    boolean isMythicItem(ItemStack itemStack);

    /** 判断是否为 Overture 物品 */
    boolean isOvertureItem(ItemStack itemStack);

    // ─── 物品生成 ─────────────────────────────────────────────────

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
