package xuanmo.arcartxsuite.api.condition;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScriptConditionEvaluator {

    boolean passes(@Nullable Player player, @NotNull List<ScriptCondition> conditions);

    @Nullable
    ScriptCondition firstFailed(@Nullable Player player, @NotNull List<ScriptCondition> conditions);

    /**
     * 带上下文变量表的条件评估，用于支持 {@link ScriptConditionKind#CONTEXT} 类型条件。
     * <p>
     * 默认实现忽略 variables 并委托 {@link #firstFailed(Player, List)}，
     * 支持 CONTEXT 条件的实现需覆写此方法。
     *
     * @param player 玩家，可为 null
     * @param conditions 条件列表
     * @param variables 上下文变量表（键为变量名，值为字符串），可为 null 或空
     * @return 第一个未通过的条件，全部通过返回 null
     */
    default @Nullable ScriptCondition firstFailed(
        @Nullable Player player,
        @NotNull List<ScriptCondition> conditions,
        @Nullable Map<String, String> variables
    ) {
        return firstFailed(player, conditions);
    }

    @NotNull
    String applyPlaceholders(@Nullable Player player, @NotNull String input);

    default boolean hasPermission(@Nullable Player player, @Nullable String permission) {
        return permission == null || permission.isBlank() || (player != null && player.hasPermission(permission));
    }

    static ScriptConditionEvaluator noop() {
        return NoopScriptConditionEvaluator.INSTANCE;
    }
}
