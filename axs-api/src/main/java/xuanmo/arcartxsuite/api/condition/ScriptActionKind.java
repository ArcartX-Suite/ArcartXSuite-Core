package xuanmo.arcartxsuite.api.condition;

/**
 * 脚本动作的语言类型枚举。
 * <ul>
 *   <li>{@link #ARIA} — ArcartX 内置 Aria 脚本</li>
 *   <li>{@link #JS} — JavaScript（Nashorn/GraalJS）</li>
 * </ul>
 */
public enum ScriptActionKind {
    ARIA,
    JS
}
