package xuanmo.arcartxsuite.api.capability.mail;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.capability.MailDispatchable;

/**
 * 预设邮件派发工具类。
 * <p>
 * 统一各模块的预设邮件发放逻辑：遍历 presetIds → {@link MailDispatchable#dispatchPreset} → 检查结果。
 * 各模块只需调用 {@link #dispatchPresets} 即可，无需各自实现遍历、空值检查、异常捕获、日志记录。
 * <p>
 * 本工具类仅处理<b>预设邮件</b>（{@code mail-presets}）。
 * 带物品附件的直接邮件（{@link MailDispatchable#sendMail}）由各模块按业务需求自行调用，
 * 因为附件物品的生成和降级策略因模块而异，不适合在此统一。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class MailPresetHelper {

    private MailPresetHelper() {}

    /**
     * 统一发放预设邮件。
     * <p>
     * 行为：
     * <ol>
     *   <li>若 {@code mailProvider} 为 null 或获取到的 {@link MailDispatchable} 为 null，记录警告日志并返回 {@code false}</li>
     *   <li>遍历 presetIds，跳过 null/空白项，逐个调用 {@link MailDispatchable#dispatchPreset}</li>
     *   <li>捕获每个预设的发送异常，记录警告日志但不中断后续预设</li>
     *   <li>返回是否全部成功（所有预设 {@link MailSendResult#success()} 均为 true）</li>
     * </ol>
     *
     * @param mailProvider  Mail 模块提供者（通常为 {@code () -> getCapability(MailDispatchable.class)}），为 null 时记录警告并返回 false
     * @param playerName    收件人玩家名
     * @param presetIds     预设 ID 列表，为 null 或空时直接返回 true
     * @param sourceModule  来源模块 ID（如 "afkreward"、"battlepass"）
     * @param sourceDetail  来源详情（可为 null，用于审计追踪）
     * @param logger        日志记录器
     * @return 是否全部预设发送成功；mailProvider/mail 为 null 时返回 false；presetIds 为空时返回 true
     */
    public static boolean dispatchPresets(
        @Nullable Supplier<MailDispatchable> mailProvider,
        @NotNull String playerName,
        @Nullable List<String> presetIds,
        @NotNull String sourceModule,
        @Nullable String sourceDetail,
        @NotNull Logger logger
    ) {
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(sourceModule, "sourceModule");
        Objects.requireNonNull(logger, "logger");
        if (presetIds == null || presetIds.isEmpty()) return true;

        MailDispatchable mail = mailProvider != null ? mailProvider.get() : null;
        if (mail == null) {
            logger.warning("[" + sourceModule + "] Mail 模块不可用，跳过预设邮件派发: player=" + playerName
                + (sourceDetail != null ? " detail=" + sourceDetail : ""));
            return false;
        }

        boolean allSuccess = true;
        for (String presetId : presetIds) {
            if (presetId == null || presetId.isBlank()) continue;
            try {
                MailPresetRequest.Builder builder = MailPresetRequest.builder(presetId, playerName)
                    .sourceModule(sourceModule);
                if (sourceDetail != null) {
                    builder.sourceDetail(sourceDetail);
                }
                MailSendResult result = mail.dispatchPreset(builder.build());
                if (!result.success()) {
                    allSuccess = false;
                    logger.warning("[" + sourceModule + "] 预设邮件派发失败: preset=" + presetId
                        + " player=" + playerName + " reason=" + result.message());
                }
            } catch (Exception e) {
                allSuccess = false;
                logger.warning("[" + sourceModule + "] 预设邮件派发异常: preset=" + presetId
                    + " player=" + playerName + " | " + e.getMessage());
            }
        }
        return allSuccess;
    }

    /**
     * 统一发放预设邮件（无 sourceDetail 的便捷重载）。
     *
     * @see #dispatchPresets(Supplier, String, List, String, String, Logger)
     */
    public static boolean dispatchPresets(
        @Nullable Supplier<MailDispatchable> mailProvider,
        @NotNull String playerName,
        @Nullable List<String> presetIds,
        @NotNull String sourceModule,
        @NotNull Logger logger
    ) {
        return dispatchPresets(mailProvider, playerName, presetIds, sourceModule, null, logger);
    }
}
