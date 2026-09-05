package xuanmo.arcartxsuite.api.config;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 配置同步策略。
 * <p>
 * 用于声明在 YAML 配置同步过程中，哪些路径属于"动态键"（子键由用户自由定义，
 * 不做剪枝）、"不透明区段"（整体跳过深层合并）、"已废弃路径"（强制删除）。
 *
 * <h3>路径表达式</h3>
 * 使用点号 {@code .} 分层，{@code *} 作为单层通配，{@code **} 作为递归通配（匹配 0 个或多个层）。例如：
 * <ul>
 *     <li>{@code "tabs"} 精确匹配根下的 tabs 节</li>
 *     <li>{@code "rules.*.actions"} 匹配 rules 下任意子节的 actions</li>
 *     <li>{@code "rules.**.actions"} 匹配 rules 下任意深度子节的 actions</li>
 * </ul>
 *
 * <h3>语义</h3>
 * <ul>
 *     <li><b>dynamicSection</b>：用户可任意增删该节下的条目；同步器不删除未在默认值中
 *         出现的键，但仍会向已存在的子节内补缺失的默认子键。</li>
 *     <li><b>opaqueSection</b>：完全不进入该节，跳过深层合并/剪枝。</li>
 *     <li><b>obsoletePath</b>：无条件移除该路径（升级时清理已废弃的配置项）。</li>
 * </ul>
 */
public final class SyncPolicy {

    private final Set<String> dynamicSectionPatterns;
    private final Set<String> opaqueSectionPatterns;
    private final Set<String> obsoletePathPatterns;

    private SyncPolicy(
        Set<String> dynamicSectionPatterns,
        Set<String> opaqueSectionPatterns,
        Set<String> obsoletePathPatterns
    ) {
        this.dynamicSectionPatterns = Set.copyOf(dynamicSectionPatterns);
        this.opaqueSectionPatterns = Set.copyOf(opaqueSectionPatterns);
        this.obsoletePathPatterns = Set.copyOf(obsoletePathPatterns);
    }

    /** 最严格的策略：所有不在默认值中的键都视为废弃并清理。 */
    public static SyncPolicy strict() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isDynamicSection(String path) {
        return matchesAny(dynamicSectionPatterns, normalize(path));
    }

    public boolean isOpaqueSection(String path) {
        return matchesAny(opaqueSectionPatterns, normalize(path));
    }

    public boolean isObsoletePath(String path) {
        return matchesAny(obsoletePathPatterns, normalize(path));
    }

    private static boolean matchesAny(Set<String> patterns, String path) {
        for (String pattern : patterns) {
            if (matches(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String pattern, String path) {
        String normalizedPattern = normalize(pattern);
        String normalizedPath = normalize(path);
        if (normalizedPattern.equals(normalizedPath)) {
            return true;
        }
        String[] patternParts = split(normalizedPattern);
        String[] pathParts = split(normalizedPath);
        return matchesSegments(patternParts, 0, pathParts, 0);
    }

    /**
     * 递归匹配路径段。支持 {@code *} 单层通配和 {@code **} 递归通配（匹配 0 个或多个段）。
     */
    private static boolean matchesSegments(String[] patternParts, int pi, String[] pathParts, int ti) {
        // 模式耗尽：路径也必须耗尽才匹配
        if (pi >= patternParts.length) {
            return ti >= pathParts.length;
        }
        // 路径耗尽：剩余模式必须全是 ** 才匹配
        if (ti >= pathParts.length) {
            for (int i = pi; i < patternParts.length; i++) {
                if (!"**".equals(patternParts[i])) {
                    return false;
                }
            }
            return true;
        }
        // ** 递归通配：匹配 0 个或多个段
        if ("**".equals(patternParts[pi])) {
            // 尝试跳过 0 个段（** 匹配空）和跳过 1+ 个段（** 匹配当前段后继续递归）
            return matchesSegments(patternParts, pi + 1, pathParts, ti)
                || matchesSegments(patternParts, pi, pathParts, ti + 1);
        }
        // * 单层通配或精确匹配
        if ("*".equals(patternParts[pi]) || patternParts[pi].equals(pathParts[ti])) {
            return matchesSegments(patternParts, pi + 1, pathParts, ti + 1);
        }
        return false;
    }

    private static String[] split(String value) {
        return value.isEmpty() ? new String[0] : value.split("\\.");
    }

    private static String normalize(String path) {
        if (path == null) {
            return "";
        }
        return path.trim().replaceAll("^[.]+|[.]+$", "");
    }

    public static final class Builder {
        private final Set<String> dynamicSectionPatterns = new LinkedHashSet<>();
        private final Set<String> opaqueSectionPatterns = new LinkedHashSet<>();
        private final Set<String> obsoletePathPatterns = new LinkedHashSet<>();

        private Builder() {
        }

        public Builder dynamicSection(String pathPattern) {
            dynamicSectionPatterns.add(normalize(Objects.requireNonNull(pathPattern, "pathPattern")));
            return this;
        }

        public Builder opaqueSection(String pathPattern) {
            opaqueSectionPatterns.add(normalize(Objects.requireNonNull(pathPattern, "pathPattern")));
            return this;
        }

        public Builder obsoletePath(String pathPattern) {
            obsoletePathPatterns.add(normalize(Objects.requireNonNull(pathPattern, "pathPattern")));
            return this;
        }

        public SyncPolicy build() {
            return new SyncPolicy(dynamicSectionPatterns, opaqueSectionPatterns, obsoletePathPatterns);
        }
    }
}
