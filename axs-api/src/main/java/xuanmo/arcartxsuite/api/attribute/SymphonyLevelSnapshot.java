package xuanmo.arcartxsuite.api.attribute;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * Symphony 等级快照。
 *
 * @param providerName         等级提供者名称
 * @param level                当前等级
 * @param experience           当前经验值，null 表示无经验数据
 * @param experienceForNextLevel 升级所需经验，null 表示无数据
 * @param characterId          角色 ID，null 表示无角色系统
 * @param characterName        角色名称，null 表示无角色系统
 */
@ApiStability.Stable
public record SymphonyLevelSnapshot(
    String providerName,
    int level,
    Long experience,
    Long experienceForNextLevel,
    String characterId,
    String characterName
) {}
