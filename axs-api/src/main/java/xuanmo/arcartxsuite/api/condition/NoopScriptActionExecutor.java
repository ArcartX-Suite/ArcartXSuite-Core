package xuanmo.arcartxsuite.api.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ScriptActionExecutor} 的空操作实现，任何调用都返回 null。
 * <p>
 * 作为脚本桥接不可用时的兜底单例，避免模块空指针。
 */
final class NoopScriptActionExecutor implements ScriptActionExecutor {

    static final NoopScriptActionExecutor INSTANCE =
        new NoopScriptActionExecutor();

    private NoopScriptActionExecutor() {
    }

    @Override
    public @Nullable Object execute(
        @Nullable Player player,
        @NotNull ScriptActionKind kind,
        @NotNull String script
    ) {
        return null;
    }
}
