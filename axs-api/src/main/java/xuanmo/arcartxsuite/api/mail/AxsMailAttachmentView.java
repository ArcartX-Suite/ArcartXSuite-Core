package xuanmo.arcartxsuite.api.mail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件附件视图，对外公开的只读附件信息。
 * <p>
 * 不暴露内部序列化数据（如物品 NBT），仅提供展示所需的摘要信息。
 *
 * @param type        附件类型
 * @param currencyId  货币 ID（仅货币附件有效），物品附件为 {@code null}
 * @param amount      货币数量（仅货币附件有效），物品附件为 {@code 0}
 * @param displayName 附件展示名称（物品名或货币名）
 * @since 1.5.0
 */
@ApiStability.Stable
public record AxsMailAttachmentView(
    @NotNull Type type,
    @Nullable String currencyId,
    double amount,
    @Nullable String displayName
) {
    /** 附件类型 */
    public enum Type {
        /** 物品附件 */
        ITEM,
        /** 货币附件（CurrencyBridge） */
        CURRENCY,
        /** Vault 货币附件 */
        VAULT
    }

    /**
     * 构造物品附件视图。
     *
     * @param displayName 物品展示名
     * @return 物品附件视图
     */
    public static @NotNull AxsMailAttachmentView item(@Nullable String displayName) {
        return new AxsMailAttachmentView(Type.ITEM, null, 0, displayName);
    }

    /**
     * 构造货币附件视图。
     *
     * @param currencyId  货币 ID
     * @param amount      数量
     * @param displayName 货币展示名
     * @return 货币附件视图
     */
    public static @NotNull AxsMailAttachmentView currency(@NotNull String currencyId, double amount, @Nullable String displayName) {
        return new AxsMailAttachmentView(Type.CURRENCY, currencyId, amount, displayName);
    }

    /**
     * 构造 Vault 货币附件视图。
     *
     * @param amount      数量
     * @param displayName 货币展示名
     * @return Vault 附件视图
     */
    public static @NotNull AxsMailAttachmentView vault(double amount, @Nullable String displayName) {
        return new AxsMailAttachmentView(Type.VAULT, "money", amount, displayName);
    }
}
