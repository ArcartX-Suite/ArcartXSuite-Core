package xuanmo.arcartxsuite.api.bridge;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ArcartX ItemStack 桥接的公开 API 子集。
 * <p>
 * 提供 ItemStack → JSON 序列化能力（用于客户端 UI 物品展示），
 * 以及写入自定义 NBT 标签的能力（用于 ArcartX 自定义贴图等）。
 *
 * @since 1.1.0
 */
@ApiStability.Stable
public interface ItemBridgeAPI {

    /** 桥接是否可用 */
    boolean isAvailable();

    /** 初始化桥接（由宿主调用） */
    boolean initialize();

    /** 关闭桥接（由宿主调用） */
    void shutdown();

    /**
     * 将 ItemStack 序列化为 ArcartX 客户端可识别的 JSON 字符串。
     *
     * @param itemStack Bukkit 物品栈
     * @return JSON 字符串，不可用时返回 empty
     */
    @NotNull Optional<String> itemToJson(@NotNull ItemStack itemStack);

    /**
     * 在物品上写入一个字符串型 NBT 标签，返回带该标签的物品。
     * <p>
     * 主要用于 ArcartX 自定义贴图：
     * <ul>
     *   <li>{@code icon} —— 资源包 {@code resource/item_icon} 中的贴图（手持 / AX-UI 渲染）；</li>
     *   <li>{@code url} —— 原版 GUI（箱子菜单）中的物品图标，值为文件路径或网络链接，支持 GIF。</li>
     * </ul>
     * 桥接不可用、或底层实现不支持时，原样返回传入的物品（不抛异常）。
     *
     * @param itemStack 物品栈
     * @param key NBT 键（如 {@code icon} / {@code url}）
     * @param value NBT 字符串值
     * @return 写入标签后的物品（可能与入参为同一实例）
     * @since 1.1.0
     */
    @NotNull
    default ItemStack putStringTag(@NotNull ItemStack itemStack, @NotNull String key, @NotNull String value) {
        return itemStack;
    }
}
