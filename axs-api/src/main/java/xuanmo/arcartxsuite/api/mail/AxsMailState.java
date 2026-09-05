package xuanmo.arcartxsuite.api.mail;

import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件状态枚举，对外公开的邮件生命周期阶段。
 * <p>
 * 与内部 {@code MailStatus} 对应，但作为公开 API 独立定义，
 * 避免外部插件依赖模块内部 model 类。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public enum AxsMailState {
    /** 未读 */
    UNREAD,
    /** 已读 */
    READ,
    /** 已领取 */
    CLAIMED,
    /** 已删除 */
    DELETED,
    /** 已过期 */
    EXPIRED
}
