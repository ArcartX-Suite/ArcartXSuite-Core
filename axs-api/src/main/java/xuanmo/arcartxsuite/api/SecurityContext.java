package xuanmo.arcartxsuite.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.account.AccountTypeService;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.crossserver.CrossServerAPI;
import xuanmo.arcartxsuite.api.security.PacketGuardAPI;

/**
 * 安全上下文：提供包频率限制、账号识别、跨服传输等安全相关能力。
 *
 * @since 1.5.0
 */
public interface SecurityContext {

    @ApiStability.Stable
    @Nullable PacketGuardAPI packetGuard();

    @ApiStability.Stable
    @NotNull AccountTypeService accountTypeService();

    @ApiStability.Stable
    @NotNull CrossServerAPI crossServer();
}
