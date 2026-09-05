package xuanmo.arcartxsuite.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端包处理器规约，合并原 {@code AbstractAXSModule} 的 4 个包相关钩子为单一对象。
 *
 * @param handler           包处理器，null 表示不注册
 * @param priority          处理器优先级（越小越先，默认 0，EventPacket 建议 100）
 * @param ownershipPacketId 模块拥有的包 ID（用于路由层归属），null 表示旧式注册
 * @param guardModule       PacketGuard 模块 key，null 表示使用模块 ID
 * @since 1.5.0
 */
public record PacketHandlerSpec(
    @Nullable ClientPacketHandler handler,
    int priority,
    @Nullable String ownershipPacketId,
    @Nullable String guardModule
) {

    /** 不注册包处理器 */
    public static final PacketHandlerSpec NONE = new PacketHandlerSpec(null, 0, null, null);

    /** 简单注册：默认优先级 0，无归属元数据 */
    public static PacketHandlerSpec of(@NotNull ClientPacketHandler handler) {
        return new PacketHandlerSpec(handler, 0, null, null);
    }

    /** 指定优先级注册 */
    public static PacketHandlerSpec of(@NotNull ClientPacketHandler handler, int priority) {
        return new PacketHandlerSpec(handler, priority, null, null);
    }

    /** 完整注册：优先级 + 归属元数据 */
    public static PacketHandlerSpec of(@NotNull ClientPacketHandler handler, int priority,
                                       @Nullable String ownershipPacketId, @Nullable String guardModule) {
        return new PacketHandlerSpec(handler, priority, ownershipPacketId, guardModule);
    }
}
