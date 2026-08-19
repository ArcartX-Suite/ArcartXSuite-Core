package xuanmo.arcartxsuite.api.capability;

import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.capability.mail.MailPresetRequest;
import xuanmo.arcartxsuite.api.capability.mail.MailSendRequest;
import xuanmo.arcartxsuite.api.capability.mail.MailSendResult;

/**
 * 邮件发送能力接口。
 * <p>
 * 由 Mail 模块实现，供其他模块跨模块调用。
 * <p>
 * 线程安全：两个方法均可在任意线程调用（同步执行，内部落库阻塞至完成后返回）。
 * 调用方如需异步发送，请自行包异步任务。
 *
 * @since 1.5.0
 */
public interface MailDispatchable {

    /**
     * 发送一封带混合附件（物品+货币）的系统邮件。
     * <p>
     * 收件人无需在线，邮件存入收件箱，玩家上线后查看领取附件。
     * Mail 模块未启用时返回 {@code failure}，调用方应自行降级处理。
     *
     * @param request 邮件发送请求（收件人/主题/正文/附件列表/发件人名/来源/过期时间）
     * @return 发送结果（success/message）
     */
    @ApiStability.Stable
    MailSendResult sendMail(@NotNull MailSendRequest request);

    /**
     * 按预设模板发送邮件。
     * <p>
     * 收件人用玩家名（预设支持按名解析离线玩家）。
     * Mail 模块未启用或预设不存在时返回 {@code failure}。
     *
     * @param request 预设派发请求（presetId/收件人玩家名/来源模块/来源详情）
     * @return 发送结果
     */
    @ApiStability.Stable
    MailSendResult dispatchPreset(@NotNull MailPresetRequest request);
}
