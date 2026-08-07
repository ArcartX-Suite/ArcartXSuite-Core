package xuanmo.arcartxsuite.api.currency;

/**
 * 货币定义记录，描述一种全局货币的元数据。
 *
 * @param id                货币唯一标识（如 "coin"、"gem"）
 * @param provider          货币提供方插件名（如 Vault、PlayerPoints、XConomy）
 * @param displayName       展示名称
 * @param scale             小数位数（精度）
 * @param balancePlaceholder 余额占位符（如 "%vault_eco_balance%"）
 * @param withdrawCommand   取款命令模板
 * @param depositCommand    存款命令模板
 * @param rounding          取整策略
 */
public record CurrencyDefinition(
    String id,
    String provider,
    String displayName,
    int scale,
    String balancePlaceholder,
    String withdrawCommand,
    String depositCommand,
    String rounding
) {
}
