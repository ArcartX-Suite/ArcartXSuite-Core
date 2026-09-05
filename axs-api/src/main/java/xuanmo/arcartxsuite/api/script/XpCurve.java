package xuanmo.arcartxsuite.api.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 经验成长曲线，基于转折点断点列表 + ARIA 表达式实时求值。
 * <p>
 * 配置形如：
 * <pre>
 * xp-curve:
 *   - level: 1
 *     formula: "500 + 50 * global.level"
 *   - level: 50
 *     formula: "500 + 50 * 49 + (global.level - 49) * 100"
 * </pre>
 * 每个断点指定一个起始等级与一个 ARIA 表达式，表达式以变量 {@code global.level} 为入参
 * （绑定值由 {@link AriaBridge} 注入到 ARIA 的 {@code global.} 命名空间，必须用 {@code global.} 前缀访问；
 * 裸名 {@code level} 因 ARIA 命名空间隔离无法读到绑定值），
 * 计算结果即为"升到该等级所需的经验值"。系统按断点等级升序排列，
 * 给定目标等级时选取最大的且不超过目标等级的断点公式进行求值。
 * <p>
 * 求值在每次调用时实时进行（不预计算缓存），结果强制为不小于 1 的整数。
 * <p>
 * 此类原属 battlepass 模块，1.4.0 起提升为公开 API，供 fishing 等模块复用。
 *
 * @param breakpoints 断点列表（按 level 升序，不可变）
 * @param ariaBridge  ARIA 脚本桥接，用于求值表达式
 */
@ApiStability.Stable
public record XpCurve(
    @NotNull List<Breakpoint> breakpoints,
    @NotNull AriaBridge ariaBridge
) {

    public XpCurve {
        Objects.requireNonNull(breakpoints, "breakpoints");
        Objects.requireNonNull(ariaBridge, "ariaBridge");
        if (breakpoints.isEmpty()) {
            throw new IllegalArgumentException("xp-curve 至少需要一个断点");
        }
        // 防御性拷贝并排序
        List<Breakpoint> sorted = new ArrayList<>(breakpoints);
        sorted.sort(java.util.Comparator.comparingInt(Breakpoint::level));
        // 校验等级唯一且递增、首段从 1 开始、公式非空
        int firstLevel = sorted.get(0).level();
        if (firstLevel != 1) {
            throw new IllegalArgumentException(
                "xp-curve 第一个断点的 level 必须为 1，实际为 " + firstLevel);
        }
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).level() <= sorted.get(i - 1).level()) {
                throw new IllegalArgumentException("xp-curve 断点 level 必须严格递增");
            }
            if (sorted.get(i).formula() == null || sorted.get(i).formula().isBlank()) {
                throw new IllegalArgumentException("xp-curve 断点 level=" + sorted.get(i).level() + " 的 formula 为空");
            }
        }
        breakpoints = Collections.unmodifiableList(sorted);
    }

    /**
     * 返回升到指定等级所需的经验值。
     * <p>
     * 等级 ≤ 1 时返回 0；否则选取最大的且不超过 {@code level} 的断点公式，
     * 以 {@code {"level": level}} 为变量绑定调用 ARIA 求值（绑定值注入到 ARIA {@code global.} 命名空间，
     * 公式中需用 {@code global.level} 访问），结果强制为不小于 1 的整数。
     *
     * @param level 目标等级
     * @return 所需经验值，等级 ≤ 1 时返回 0
     * @throws XpCurveEvalException 表达式求值失败或结果非法时抛出
     */
    public int xpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        String formula = pickFormula(level);
        Map<String, Object> bindings = Map.of("level", level);
        Object result;
        try {
            result = ariaBridge.eval(formula, bindings);
        } catch (Exception e) {
            throw new XpCurveEvalException("xp-curve 求值异常 level=" + level + " formula=\"" + formula + "\": " + e.getMessage(), e);
        }
        int xp = toInt(result, formula, level);
        if (xp < 1) {
            throw new XpCurveEvalException("xp-curve 公式结果必须 ≥ 1，level=" + level + " formula=\"" + formula + "\" 结果=" + xp);
        }
        return xp;
    }

    /**
     * 返回升到指定等级所需的经验值，求值失败时回退为 {@code fallback}。
     * <p>
     * 适用于不希望因公式错误中断主流程的场景（如 UI 刷新），调用方应记录日志。
     *
     * @param level    目标等级
     * @param fallback 求值失败时的回退值
     * @return 所需经验值或回退值
     */
    public int xpRequiredForLevelOr(int level, int fallback) {
        try {
            return xpRequiredForLevel(level);
        } catch (XpCurveEvalException e) {
            return fallback;
        }
    }

    private String pickFormula(int level) {
        String formula = breakpoints.get(0).formula();
        for (Breakpoint bp : breakpoints) {
            if (bp.level() <= level) {
                formula = bp.formula();
            } else {
                break;
            }
        }
        return formula;
    }

    private static int toInt(Object result, String formula, int level) {
        if (result == null) {
            throw new XpCurveEvalException("xp-curve 公式返回 null，level=" + level + " formula=\"" + formula + "\"");
        }
        if (result instanceof Number number) {
            return (int) Math.round(number.doubleValue());
        }
        if (result instanceof String text) {
            try {
                return (int) Math.round(Double.parseDouble(text.trim()));
            } catch (NumberFormatException e) {
                throw new XpCurveEvalException("xp-curve 公式返回非数字字符串，level=" + level + " formula=\"" + formula + "\" 结果=\"" + text + "\"");
            }
        }
        throw new XpCurveEvalException("xp-curve 公式返回不可识别类型 " + result.getClass().getName() + "，level=" + level + " formula=\"" + formula + "\"");
    }

    /**
     * 经验曲线断点，指定从某个等级起生效的 ARIA 表达式。
     *
     * @param level   起始等级（含），首个断点必须为 1
     * @param formula ARIA 表达式，以变量 {@code global.level} 为入参（需用 {@code global.} 前缀访问绑定值）
     */
    @ApiStability.Stable
    public record Breakpoint(int level, @NotNull String formula) {
        public Breakpoint {
            if (level < 1) {
                throw new IllegalArgumentException("breakpoint level 必须 ≥ 1，实际为 " + level);
            }
            Objects.requireNonNull(formula, "formula");
            if (formula.isBlank()) {
                throw new IllegalArgumentException("breakpoint level=" + level + " 的 formula 为空");
            }
        }
    }

    /**
     * 经验曲线求值异常，封装表达式求值失败或结果非法的情况。
     */
    @ApiStability.Stable
    public static final class XpCurveEvalException extends RuntimeException {
        public XpCurveEvalException(String message) { super(message); }
        public XpCurveEvalException(String message, Throwable cause) { super(message, cause); }
    }
}
