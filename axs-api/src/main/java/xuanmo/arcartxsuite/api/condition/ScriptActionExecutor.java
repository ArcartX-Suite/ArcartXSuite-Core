package xuanmo.arcartxsuite.api.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 脚本动作执行器接口，负责按指定语言类型执行脚本语句并返回结果。
 * <p>
 * 宿主提供默认实现（Aria / JS），模块可通过 {@link ModuleContext} 获取。
 */
public interface ScriptActionExecutor {

    /**
     * 执行一段脚本动作。
     *
     * @param player 触发动作的玩家，可为 null（控制台/离线场景）
     * @param kind   脚本语言类型（ARIA 或 JS）
     * @param script 脚本文本，非 null
     * @return 脚本返回值；无返回值或执行失败时返回 null
     */
    @Nullable
    Object execute(
        @Nullable Player player,
        @NotNull ScriptActionKind kind,
        @NotNull String script
    );

    /**
     * 获取空操作实现，任何调用都返回 null，用于桥接不可用时的兜底。
     *
     * @return 单例 noop 执行器
     */
    static ScriptActionExecutor noop() {
        return NoopScriptActionExecutor.INSTANCE;
    }
}
