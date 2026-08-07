package xuanmo.arcartxsuite.api.security;

import org.bukkit.entity.Player;

/**
 * 客户端包频率限制 API。
 * <p>
 * 模块在处理客户端包时应调用 {@link #allow} 进行频率校验，
 * 防止客户端发送恶意高频包。
 */
public interface PacketGuardAPI {

    /**
     * Allows an operation according to the configured module/action rate rule.
     * Incoming packet handlers are guarded centrally by the framework route
     * layer and should not call this method for packet throttling.
     *
     * @param player       player identity
     * @param module       module key, such as {@code warehouse}
     * @param action       action key, such as {@code buy}
     * @param debugLogging whether to emit debug logging
     * @return true when the operation is allowed
     */
    boolean allow(Player player, String module, String action, boolean debugLogging);
}
