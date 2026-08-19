package xuanmo.arcartxsuite.api.attribute;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 属性桥接。
 * <p>
 * 核心操作:
 * <ul>
 *   <li>{@link #setAttribute(Player, String, boolean, double, String)} — 设置属性</li>
 *   <li>{@link #removeAttribute(Player, String)} — 移除属性源</li>
 *   <li>{@link #recalculate(Player)} — 重新计算属性</li>
 * </ul>
 * 查询操作（Symphony 不可用时返回 null / 默认值）:
 * <ul>
 *   <li>{@link #combatPower(LivingEntity)} — 查询战力</li>
 *   <li>{@link #level(LivingEntity)} — 查询等级快照</li>
 *   <li>{@link #attributeValue(LivingEntity, String)} — 查询属性当前值</li>
 *   <li>{@link #shield(LivingEntity)} / {@link #setShield(LivingEntity, double)} — 护盾读写</li>
 *   <li>{@link #combatState(LivingEntity)} — 查询战斗状态</li>
 *   <li>{@link #statuses(LivingEntity)} — 查询状态效果</li>
 *   <li>{@link #auras(LivingEntity)} — 查询光环</li>
 *   <li>{@link #activeSets(LivingEntity)} — 查询激活的套装</li>
 * </ul>
 */
@ApiStability.Stable
public interface SymphonyBridge {

    boolean available();

    // ─── 属性写操作 ──────────────────────────────────────────

    /**
     * 设置属性修饰符。
     * @param percent true=百分比(MULTIPLY_TOTAL)，false=固定值(ADD)
     * @param sourceKey 来源标识，用于后续移除
     */
    void setAttribute(Player player, String attributeId, boolean percent, double value, String sourceKey);

    /** 移除指定 sourceKey 的属性 */
    void removeAttribute(Player player, String sourceKey);

    /** 重新计算玩家全部属性 */
    void recalculate(Player player);

    // ─── 属性查询 ────────────────────────────────────────────

    /** 查询实体指定属性的当前值，Symphony 不可用或属性不存在时返回 0 */
    double attributeValue(LivingEntity entity, String attributeId);

    // ─── 战力查询 ────────────────────────────────────────────

    /** 查询实体战力快照，Symphony 不可用时返回 null */
    SymphonyCombatPower combatPower(LivingEntity entity);

    // ─── 等级查询 ────────────────────────────────────────────

    /** 查询实体等级快照，Symphony 不可用或无等级提供者时返回 null */
    SymphonyLevelSnapshot level(LivingEntity entity);

    // ─── 护盾 ────────────────────────────────────────────────

    /** 查询实体护盾值，Symphony 不可用时返回 0 */
    double shield(LivingEntity entity);

    /** 设置实体护盾值，返回设置后的实际值 */
    double setShield(LivingEntity entity, double amount);

    // ─── 战斗状态 ────────────────────────────────────────────

    /** 查询实体战斗状态，Symphony 不可用时返回 inactive */
    SymphonyCombatState combatState(LivingEntity entity);

    /** 查询实体当前状态效果列表，Symphony 不可用时返回空列表 */
    List<SymphonyStatusEffect> statuses(LivingEntity entity);

    /** 查询实体当前光环列表，Symphony 不可用时返回空列表 */
    List<SymphonyAura> auras(LivingEntity entity);

    /** 查询实体激活的套装（key → 件数），Symphony 不可用时返回空 Map */
    Map<String, Integer> activeSets(LivingEntity entity);
}
