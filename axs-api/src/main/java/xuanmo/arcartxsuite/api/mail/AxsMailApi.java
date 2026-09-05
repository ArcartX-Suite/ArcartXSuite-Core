package xuanmo.arcartxsuite.api.mail;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件 API 静态入口类。
 * <p>
 * 提供与 ArcartX SystemMail API 一致的获取方式：
 * <pre>{@code
 * if (!AxsMailApi.isReady()) return;
 * AxsMailService service = AxsMailApi.service();
 * }</pre>
 * <p>
 * 也可通过 Bukkit ServicesManager 获取：
 * <pre>{@code
 * RegisteredServiceProvider<AxsMailService> reg =
 *     Bukkit.getServicesManager().getRegistration(AxsMailService.class);
 * if (reg == null) return;
 * AxsMailService service = reg.getProvider();
 * }</pre>
 *
 * @since 1.5.0
 */
public final class AxsMailApi {

    private AxsMailApi() {}

    private static volatile @Nullable AxsMailService cachedService;

    /**
     * 判断邮件服务是否就绪。
     *
     * @return {@code true} 表示服务已注册且可用
     */
    public static boolean isReady() {
        return service() != null;
    }

    /**
     * 获取邮件服务实例。
     * <p>
     * 优先返回缓存的实例，缓存失效时从 Bukkit ServicesManager 查找。
     *
     * @return 服务实例，未注册时返回 {@code null}
     */
    public static @Nullable AxsMailService service() {
        AxsMailService snapshot = cachedService;
        if (snapshot != null) {
            return snapshot;
        }
        // 从 ServicesManager 查找（首次调用或模块热重载后缓存失效）
        RegisteredServiceProvider<AxsMailService> registration =
            Bukkit.getServicesManager().getRegistration(AxsMailService.class);
        if (registration != null && registration.getProvider() != null) {
            cachedService = registration.getProvider();
            return cachedService;
        }
        return null;
    }

    /**
     * 内部方法：设置缓存的服务实例（由 Mail 模块在 startService 时调用）。
     *
     * @param service 服务实例，{@code null} 表示清除缓存
     */
    @xuanmo.arcartxsuite.api.bridge.ApiStability.Internal
    public static void setService(@Nullable AxsMailService service) {
        cachedService = service;
    }
}
