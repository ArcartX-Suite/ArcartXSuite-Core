package xuanmo.arcartxsuite.api;

import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.bridge.PropBridgeAPI;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderExpansionRegistry;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderResolverAPI;

/**
 * 注册上下文：提供事件监听器、命令、客户端包处理器、按键、PlaceholderAPI、Capability 等注册能力。
 *
 * @since 1.5.0
 */
public interface RegistrationContext {

    void registerListener(Listener listener);

    void unregisterListeners();

    void registerCommand(String commandName, TabExecutor executor);

    @ApiStability.Stable
    @NotNull PlaceholderResolverAPI placeholderResolver();

    @ApiStability.Stable
    @NotNull PlaceholderExpansionRegistry expansionRegistry();

    void registerClientPacketHandler(ClientPacketHandler handler);

    void registerClientPacketHandler(ClientPacketHandler handler, int priority);

    default void registerClientPacketHandler(
        ClientPacketHandler handler,
        int priority,
        String packetId,
        String guardModule
    ) {
        registerClientPacketHandler(handler, priority);
    }

    void registerClientInitializedHandler(ClientInitializedHandler handler);

    @ApiStability.Stable
    void registerKeybindHandler(String keyName, int priority, KeybindHandler handler);

    @ApiStability.Stable
    <T> void registerCapability(Class<T> capabilityType, T implementation);

    @ApiStability.Stable
    @Nullable <T> T getCapability(Class<T> capabilityType);

    @ApiStability.Internal
    @Nullable PropBridgeAPI propBridge();
}
