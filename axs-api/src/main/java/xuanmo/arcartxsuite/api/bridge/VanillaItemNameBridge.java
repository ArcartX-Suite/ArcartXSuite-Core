package xuanmo.arcartxsuite.api.bridge;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.item.VanillaItemNameTable;

/**
 * 原版物品中文名称解析桥接。
 * <p>
 * 由宿主实现并通过 {@link xuanmo.arcartxsuite.api.ModuleContext#vanillaItemNameBridge()} 暴露给模块。
 * 模块在显示原版物品名、入库搜索文本、聊天物品预览等场景统一调用此桥接，
 * 避免各模块各自维护映射表或直接显示英文材质名。
 * <p>
 * 解析优先级：
 * <ol>
 *   <li>物品自定义显示名（{@code ItemMeta.hasDisplayName()}）—— 原样返回，不翻译；</li>
 *   <li>附魔书 / 药水等带 NBT 子类型 —— 按子类型拼名（如“锋利附魔书”）；</li>
 *   <li>Material 粒度 —— 查 {@link VanillaItemNameTable}。</li>
 * </ol>
 *
 * @since 1.3.3
 */
@ApiStability.Stable
public interface VanillaItemNameBridge {

    /** 桥接是否可用（宿主未初始化时返回 false） */
    boolean isAvailable();

    /**
     * 按 Material 粒度翻译原版物品名。
     * 未命中映射表时返回材质名的小写空格形式（如 {@code diamond_sword} → {@code diamond sword}）。
     *
     * @param material 物品材质，null 或 AIR 返回“空气”
     * @return 中文名
     */
    @NotNull String translate(@NotNull Material material);

    /**
     * 解析物品的中文显示名。
     * <p>
     * 优先返回物品自定义显示名；无自定义名时按 Material + NBT 子类型翻译。
     * 返回值可能包含颜色代码（自定义名场景）。
     *
     * @param itemStack 物品栈，null 返回“空气”
     * @return 中文显示名
     */
    @NotNull String resolveDisplayName(@NotNull ItemStack itemStack);
}
