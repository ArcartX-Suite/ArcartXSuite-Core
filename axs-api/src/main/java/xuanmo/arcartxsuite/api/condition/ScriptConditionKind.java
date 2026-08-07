package xuanmo.arcartxsuite.api.condition;

public enum ScriptConditionKind {
    PAPI,
    ARIA,
    JS,
    /** EventPacket 上下文变量条件：左右操作数均支持 {variable} 语法或字面量。 */
    CONTEXT
}
