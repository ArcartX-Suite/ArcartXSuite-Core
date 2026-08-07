package xuanmo.arcartxsuite.api.condition;

/**
 * 全局条件评估服务定位器。
 * <p>
 * 宿主在初始化时通过 {@link #install(ScriptConditionEvaluator)} 注入实现，
 * 模块通过 {@link #evaluator()} 获取条件评估器，或通过
 * {@link #actionExecutor()} 获取脚本动作执行器。
 */
public final class ScriptConditionServices {

    private static volatile ScriptConditionEvaluator evaluator =
        ScriptConditionEvaluator.noop();
    private static volatile ScriptActionExecutor actionExecutor =
        ScriptActionExecutor.noop();

    private ScriptConditionServices() {
    }

    public static ScriptConditionEvaluator evaluator() {
        return evaluator;
    }

    public static ScriptActionExecutor actionExecutor() {
        return actionExecutor;
    }

    public static void install(ScriptConditionEvaluator installed) {
        evaluator = installed == null ? ScriptConditionEvaluator.noop() : installed;
        actionExecutor = installed instanceof ScriptActionExecutor executor
            ? executor
            : ScriptActionExecutor.noop();
    }

    public static void install(
        ScriptConditionEvaluator installed,
        ScriptActionExecutor actions
    ) {
        evaluator = installed == null ? ScriptConditionEvaluator.noop() : installed;
        actionExecutor = actions == null ? ScriptActionExecutor.noop() : actions;
    }

    public static void reset() {
        evaluator = ScriptConditionEvaluator.noop();
        actionExecutor = ScriptActionExecutor.noop();
    }
}
