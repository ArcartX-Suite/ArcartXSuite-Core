package xuanmo.arcartxsuite.api.script;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ArcartX 内置 Aria 脚本桥接接口，提供脚本可用性查询与求值能力。
 * <p>
 * 宿主提供默认实现；当 Aria 运行时不可用时 {@link #available()} 返回 false，
 * 模块应据此降级处理而非直接调用 {@link #eval}。
 */
public interface AriaBridge {

    /** Aria 脚本运行时是否可用。 */
    boolean available();

    /** Aria 版本号，不可用时返回 null。 */
    @Nullable
    String version();

    /**
     * 执行一段 Aria 脚本。
     *
     * @param code     脚本文本，非 null
     * @param bindings 变量绑定，非 null
     * @return 脚本返回值；无返回值或失败时返回 null
     */
    @Nullable
    Object eval(@NotNull String code, @NotNull Map<String, Object> bindings);

    /**
     * 执行脚本并将结果转换为 boolean。
     *
     * @param code     脚本文本
     * @param bindings 变量绑定
     * @return 脚本结果的布尔值，转换规则见 {@link #toBoolean}
     */
    default boolean evalBoolean(@NotNull String code, @NotNull Map<String, Object> bindings) {
        return toBoolean(eval(code, bindings));
    }

    /**
     * 将脚本返回值转换为 boolean：null/空串/"false"/"0" 为 false，其余为 true。
     *
     * @param result 脚本返回值
     * @return 布尔值
     */
    static boolean toBoolean(@Nullable Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean bool) {
            return bool;
        }
        if (result instanceof Number number) {
            return number.doubleValue() != 0.0D;
        }
        if (result instanceof String text) {
            if (text.isBlank()) {
                return false;
            }
            return !"false".equalsIgnoreCase(text) && !"0".equals(text);
        }
        return true;
    }
}
