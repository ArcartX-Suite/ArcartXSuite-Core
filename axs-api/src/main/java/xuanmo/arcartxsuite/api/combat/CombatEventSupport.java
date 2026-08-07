package xuanmo.arcartxsuite.api.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * 战斗事件辅助工具，用于从伤害事件中解析实际的玩家攻击者。
 * <p>
 * 处理直接攻击与抛射物（弓箭、雪球等）两种场景，统一返回背后的玩家对象。
 */
public final class CombatEventSupport {

    private CombatEventSupport() {}

    /**
     * 解析伤害事件中的玩家攻击者。
 * <p>
 * 当伤害来源为玩家时直接返回；当为抛射物时追溯其发射者，若发射者为玩家则返回。
 *
     * @param event 实体被实体伤害事件
     * @return 造成伤害的玩家；若伤害来源不是玩家（或抛射物发射者不是玩家）则返回 null
     */
    public static Player resolvePlayerAttacker(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}
