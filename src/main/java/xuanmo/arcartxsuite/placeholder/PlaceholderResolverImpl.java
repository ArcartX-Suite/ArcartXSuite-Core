package xuanmo.arcartxsuite.placeholder;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderResolverAPI;

/**
 * {@link PlaceholderResolverAPI} 的宿主实现。
 * <p>
 * 封装 {@code me.clip.placeholderapi.PlaceholderAPI.setPlaceholders()}。
 * <p>
 * 关键：本体业务核心由加密类加载器加载，<strong>看不到 PlaceholderAPI 的类</strong>，
 * 因此不能直接以符号引用方式调用 {@code PlaceholderAPI.setPlaceholders}
 * （会在链接期抛 {@link NoClassDefFoundError}）。这里改为通过 PlaceholderAPI
 * <em>自身的类加载器</em> 反射解析方法并缓存，保证在任意类加载器拓扑下都可用。
 */
public final class PlaceholderResolverImpl implements PlaceholderResolverAPI {

    private volatile boolean available;
    private volatile Method setPlaceholdersPlayer;
    private volatile Method setPlaceholdersOffline;

    public PlaceholderResolverImpl() {
        refreshAvailability();
    }

    /**
     * 刷新可用性状态（例如插件重新加载后）。
     */
    public void refreshAvailability() {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) {
            available = false;
            setPlaceholdersPlayer = null;
            setPlaceholdersOffline = null;
            return;
        }
        try {
            // 用 PAPI 自身的类加载器解析其 API 类，绕开本体加密核心类加载器的可见性限制。
            ClassLoader cl = papi.getClass().getClassLoader();
            Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI", false, cl);
            Method offline = papiClass.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
            Method playerMethod;
            try {
                playerMethod = papiClass.getMethod("setPlaceholders", Player.class, String.class);
            } catch (NoSuchMethodException ignored) {
                // 没有 Player 专用重载时，Player 也是 OfflinePlayer，复用 offline 重载即可。
                playerMethod = offline;
            }
            setPlaceholdersOffline = offline;
            setPlaceholdersPlayer = playerMethod;
            available = true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            available = false;
            setPlaceholdersPlayer = null;
            setPlaceholdersOffline = null;
        }
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public @NotNull String applyPlaceholders(@Nullable Player player, @NotNull String input) {
        Method method = setPlaceholdersPlayer;
        if (!available || method == null || input.isEmpty()) {
            return input;
        }
        try {
            Object result = method.invoke(null, player, input);
            return result instanceof String resolved ? resolved : input;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return input;
        }
    }

    @Override
    public @NotNull String applyPlaceholders(@Nullable OfflinePlayer player, @NotNull String input) {
        Method method = setPlaceholdersOffline;
        if (!available || method == null || input.isEmpty()) {
            return input;
        }
        try {
            Object result = method.invoke(null, player, input);
            return result instanceof String resolved ? resolved : input;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return input;
        }
    }
}
