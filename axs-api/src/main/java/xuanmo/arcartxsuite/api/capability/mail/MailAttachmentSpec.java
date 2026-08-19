package xuanmo.arcartxsuite.api.capability.mail;

import java.util.Locale;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件附件描述，接口层公开的附件规格。
 * <p>
 * 由调用方构造，传给 {@link MailDispatchable#sendMail(MailSendRequest)}。
 * 支持两种附件类型：
 * <ul>
 *   <li>{@link Type#ITEM} — 物品附件，需提供 {@link ItemStack}</li>
 *   <li>{@link Type#CURRENCY} — 货币附件，需提供货币 ID 与数量；
 *       {@code currencyId} 为 {@code "money"} 时走 Vault 通道，其余走 CurrencyBridge</li>
 * </ul>
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class MailAttachmentSpec {

    /** 附件类型。 */
    public enum Type {
        /** 物品附件 */
        ITEM,
        /** 货币附件（"money" 走 Vault，其余走 CurrencyBridge） */
        CURRENCY
    }

    private final @NotNull Type type;
    private final @Nullable ItemStack itemStack;
    private final @Nullable String currencyId;
    private final double amount;

    private MailAttachmentSpec(@NotNull Type type, @Nullable ItemStack itemStack,
                               @Nullable String currencyId, double amount) {
        this.type = type;
        this.itemStack = itemStack;
        this.currencyId = currencyId;
        this.amount = amount;
    }

    /**
     * 构造物品附件。
     *
     * @param itemStack 物品，不可为空
     * @return 物品附件规格
     */
    public static @NotNull MailAttachmentSpec item(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack");
        return new MailAttachmentSpec(Type.ITEM, itemStack.clone(), null, 0);
    }

    /**
     * 构造货币附件。
     * <p>
     * {@code currencyId} 为 {@code "money"} 时走 Vault 通道，其余走 CurrencyBridge。
     *
     * @param currencyId 货币 ID（如 "money""coin"），不可为空
     * @param amount     货币数量，必须 &gt; 0
     * @return 货币附件规格
     */
    public static @NotNull MailAttachmentSpec currency(@NotNull String currencyId, double amount) {
        Objects.requireNonNull(currencyId, "currencyId");
        if (currencyId.isBlank()) {
            throw new IllegalArgumentException("currencyId 不能为空");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount 必须 > 0，实际: " + amount);
        }
        return new MailAttachmentSpec(Type.CURRENCY, null, currencyId.trim().toLowerCase(Locale.ROOT), amount);
    }

    public @NotNull Type type() { return type; }

    public @Nullable ItemStack itemStack() { return itemStack == null ? null : itemStack.clone(); }

    public @Nullable String currencyId() { return currencyId; }

    public double amount() { return amount; }
}
