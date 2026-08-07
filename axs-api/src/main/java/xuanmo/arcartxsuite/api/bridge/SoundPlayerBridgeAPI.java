package xuanmo.arcartxsuite.api.bridge;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ArcartX 音效播放器桥接。
 * <p>
 * 通过 {@code ArcartXAPI.getSoundPlayer()} 播放资源包中的自定义音效，
 * 支持仅对指定玩家播放、世界广播等模式。
 *
 * @since 1.3.0
 */
@ApiStability.Internal
public interface SoundPlayerBridgeAPI {

    /** 桥接是否可用（ArcartX 插件已加载且 SoundPlayer 初始化成功） */
    boolean isAvailable();

    /** 初始化桥接 */
    boolean initialize();

    /** 关闭桥接 */
    void shutdown();

    /**
     * 为指定玩家在指定位置播放自定义音效（仅该玩家可听到）。
     *
     * @param player       目标玩家
     * @param location     音效播放位置
     * @param resourcePath 音效资源路径（如 {@code "sounds/bell.ogg"}）
     * @param category     音效类别（如 {@code "master"}、{@code "music"}、{@code "ambient"}）
     * @param distOrRoll   传播距离/衰减
     * @param pitch        音调
     * @param keepTimeMs   持续时间（毫秒）
     * @return {@code true} 表示播放成功
     */
    boolean playSoundForPlayer(@NotNull Player player, @NotNull Location location,
                               @NotNull String resourcePath, @NotNull String category,
                               int distOrRoll, double pitch, int keepTimeMs);

    /**
     * 为玩家自身播放自定义音效（无位置，直接播放）。
     *
     * @param player       目标玩家
     * @param resourcePath 音效资源路径
     * @param category     音效类别
     * @param pitch        音调
     * @param keepTimeMs   持续时间（毫秒）
     * @return {@code true} 表示播放成功
     */
    boolean playSoundForSelf(@NotNull Player player, @NotNull String resourcePath,
                             @NotNull String category, float pitch, int keepTimeMs);
}
