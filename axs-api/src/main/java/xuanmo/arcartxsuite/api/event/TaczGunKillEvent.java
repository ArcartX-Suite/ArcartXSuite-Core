package xuanmo.arcartxsuite.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * TACZ（创世战术武器）枪械击杀事件。
 * <p>
 * 当 TACZ Mod 的 {@code EntityKillByGunEvent} 触发时，由 {@code TaczCombatBridge}
 * 转换为标准 Bukkit 事件并广播。AXS 各模块可通过标准 Bukkit 事件机制监听此事件，
 * 以获取 TACZ 枪械击杀信息，而无需关心 Forge/NeoForge 事件总线的反射细节。
 * <p>
 * 此事件与 {@link org.bukkit.event.entity.EntityDeathEvent} 互补：
 * TACZ 击杀不保证 {@code LivingEntity.getKiller()} 返回枪手，
 * 模块应监听本事件以准确获取枪械击杀的攻击者信息。
 *
 * <pre>{@code
 * @EventHandler
 * public void onTaczKill(TaczGunKillEvent event) {
 *     Player attacker = event.getAttacker();
 *     LivingEntity victim = event.getKilledEntity();
 *     boolean headshot = event.isHeadShot();
 *     // ...
 * }
 * }</pre>
 *
 * @since 1.3.2
 */
@ApiStability.Stable
public final class TaczGunKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player attacker;
    private final LivingEntity killedEntity;
    private final double damage;
    private final boolean headShot;
    private final String gunId;

    public TaczGunKillEvent(@NotNull Player attacker, @NotNull LivingEntity killedEntity,
                            double damage, boolean headShot, @NotNull String gunId) {
        this.attacker = attacker;
        this.killedEntity = killedEntity;
        this.damage = damage;
        this.headShot = headShot;
        this.gunId = gunId;
    }

    /** 攻击者（开枪的玩家） */
    @NotNull
    public Player getAttacker() {
        return attacker;
    }

    /** 被击杀的实体 */
    @NotNull
    public LivingEntity getKilledEntity() {
        return killedEntity;
    }

    /** 基础伤害值（尚未经过护甲/抗性等减免） */
    public double getDamage() {
        return damage;
    }

    /** 是否为爆头击杀 */
    public boolean isHeadShot() {
        return headShot;
    }

    /** 枪械 ID（如 {@code tacz:modern_kinetic_gun}） */
    @NotNull
    public String getGunId() {
        return gunId;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
