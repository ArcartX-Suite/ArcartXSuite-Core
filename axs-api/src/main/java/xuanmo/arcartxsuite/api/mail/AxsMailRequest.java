package xuanmo.arcartxsuite.api.mail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 邮件发送请求。
 * <p>
 * 传给 {@link AxsMailService#send(AxsMailRequest)} 和
 * {@link AxsMailService#sendDurable(AxsMailRequest)}。
 * <p>
 * 使用 Builder 模式构造，参考 ArcartX SystemMail API 设计。
 * 支持物品附件、货币附件和命令附件。
 *
 * @since 1.5.0
 */
@ApiStability.Stable
public final class AxsMailRequest {

    private final @NotNull String idempotencyKey;
    private final @NotNull List<UUID> recipients;
    private final @NotNull String title;
    private final @NotNull String content;
    private final @Nullable String senderName;
    private final @Nullable String senderAvatar;
    private final @NotNull List<ItemStack> items;
    private final @NotNull List<CurrencyAttachment> currencies;
    private final @NotNull List<CommandAttachment> commands;
    private final @NotNull String sourcePlugin;
    private final @Nullable Instant deliverAt;
    private final @Nullable Instant expireAt;

    private AxsMailRequest(@NotNull String idempotencyKey, @NotNull List<UUID> recipients,
                           @NotNull String title, @NotNull String content,
                           @Nullable String senderName, @Nullable String senderAvatar,
                           @NotNull List<ItemStack> items, @NotNull List<CurrencyAttachment> currencies,
                           @NotNull List<CommandAttachment> commands, @NotNull String sourcePlugin,
                           @Nullable Instant deliverAt, @Nullable Instant expireAt) {
        this.idempotencyKey = idempotencyKey;
        this.recipients = recipients;
        this.title = title;
        this.content = content;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.items = items;
        this.currencies = currencies;
        this.commands = commands;
        this.sourcePlugin = sourcePlugin;
        this.deliverAt = deliverAt;
        this.expireAt = expireAt;
    }

    /**
     * 创建 Builder。
     *
     * @param idempotencyKey 去重标识，同一业务动作保持不变
     * @param title          邮件标题
     * @return Builder
     */
    public static @NotNull Builder builder(@NotNull String idempotencyKey, @NotNull String title) {
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(title, "title");
        if (idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey 不能为空");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title 不能为空");
        }
        return new Builder(idempotencyKey, title);
    }

    public @NotNull String idempotencyKey() { return idempotencyKey; }

    public @NotNull List<UUID> recipients() { return recipients; }

    public @NotNull String title() { return title; }

    public @NotNull String content() { return content; }

    public @Nullable String senderName() { return senderName; }

    public @Nullable String senderAvatar() { return senderAvatar; }

    public @NotNull List<ItemStack> items() { return items; }

    public @NotNull List<CurrencyAttachment> currencies() { return currencies; }

    public @NotNull List<CommandAttachment> commands() { return commands; }

    public @NotNull String sourcePlugin() { return sourcePlugin; }

    public @Nullable Instant deliverAt() { return deliverAt; }

    public @Nullable Instant expireAt() { return expireAt; }

    /** 货币附件。 */
    public record CurrencyAttachment(@NotNull String provider, double amount, @Nullable String displayName) {
        public CurrencyAttachment {
            Objects.requireNonNull(provider, "provider");
            if (provider.isBlank()) {
                throw new IllegalArgumentException("provider 不能为空");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("amount 必须 > 0");
            }
        }
    }

    /** 命令附件。 */
    public record CommandAttachment(
        @NotNull String command,
        @NotNull CommandExecutor executor,
        @Nullable String displayTitle,
        @Nullable String icon
    ) {
        public CommandAttachment {
            Objects.requireNonNull(command, "command");
            Objects.requireNonNull(executor, "executor");
            if (command.isBlank()) {
                throw new IllegalArgumentException("command 不能为空");
            }
        }
    }

    /** 命令执行者。 */
    public enum CommandExecutor {
        /** 控制台执行 */
        CONSOLE,
        /** 玩家执行 */
        PLAYER
    }

    public static final class Builder {
        private final @NotNull String idempotencyKey;
        private final @NotNull String title;
        private final @NotNull List<UUID> recipients = new ArrayList<>();
        private @NotNull String content = "";
        private @Nullable String senderName;
        private @Nullable String senderAvatar;
        private final @NotNull List<ItemStack> items = new ArrayList<>();
        private final @NotNull List<CurrencyAttachment> currencies = new ArrayList<>();
        private final @NotNull List<CommandAttachment> commands = new ArrayList<>();
        private @NotNull String sourcePlugin = "unknown";
        private @Nullable Instant deliverAt;
        private @Nullable Instant expireAt;

        private Builder(@NotNull String idempotencyKey, @NotNull String title) {
            this.idempotencyKey = idempotencyKey;
            this.title = title;
        }

        /** 添加单个收件人 */
        public @NotNull Builder recipient(@NotNull UUID uuid) {
            this.recipients.add(Objects.requireNonNull(uuid, "uuid"));
            return this;
        }

        /** 设置多个收件人（替换） */
        public @NotNull Builder recipients(@NotNull List<UUID> uuids) {
            this.recipients.clear();
            this.recipients.addAll(Objects.requireNonNull(uuids, "uuids"));
            return this;
        }

        /** 邮件正文，默认空字符串，多行用 \n */
        public @NotNull Builder content(@NotNull String content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        /** 发件人显示名，默认"系统" */
        public @NotNull Builder senderName(@Nullable String senderName) {
            this.senderName = senderName;
            return this;
        }

        /** AXUI 头像路径 */
        public @NotNull Builder senderAvatar(@Nullable String senderAvatar) {
            this.senderAvatar = senderAvatar;
            return this;
        }

        /** 添加物品附件 */
        public @NotNull Builder item(@NotNull ItemStack itemStack) {
            this.items.add(Objects.requireNonNull(itemStack, "itemStack"));
            return this;
        }

        /** 设置多个物品附件（替换） */
        public @NotNull Builder items(@NotNull List<ItemStack> items) {
            this.items.clear();
            this.items.addAll(Objects.requireNonNull(items, "items"));
            return this;
        }

        /** 添加货币附件 */
        public @NotNull Builder currency(@NotNull String provider, double amount) {
            return currency(provider, amount, null);
        }

        /** 添加货币附件（带显示名） */
        public @NotNull Builder currency(@NotNull String provider, double amount, @Nullable String displayName) {
            this.currencies.add(new CurrencyAttachment(provider, amount, displayName));
            return this;
        }

        /** 添加命令附件 */
        public @NotNull Builder command(@NotNull String command, @NotNull CommandExecutor executor) {
            return command(command, executor, null, null);
        }

        /** 添加命令附件（带展示标题和图标） */
        public @NotNull Builder command(@NotNull String command, @NotNull CommandExecutor executor,
                                        @Nullable String displayTitle, @Nullable String icon) {
            this.commands.add(new CommandAttachment(command, executor, displayTitle, icon));
            return this;
        }

        /** 调用方插件标识 */
        public @NotNull Builder sourcePlugin(@NotNull String sourcePlugin) {
            this.sourcePlugin = Objects.requireNonNull(sourcePlugin, "sourcePlugin");
            return this;
        }

        /** 投递时间，默认当前时间 */
        public @NotNull Builder deliverAt(@Nullable Instant deliverAt) {
            this.deliverAt = deliverAt;
            return this;
        }

        /** 过期时间，默认永不过期 */
        public @NotNull Builder expireAt(@Nullable Instant expireAt) {
            this.expireAt = expireAt;
            return this;
        }

        public @NotNull AxsMailRequest build() {
            if (recipients.isEmpty()) {
                throw new IllegalStateException("至少需要一个收件人");
            }
            return new AxsMailRequest(
                idempotencyKey,
                Collections.unmodifiableList(new ArrayList<>(recipients)),
                title,
                content,
                senderName,
                senderAvatar,
                Collections.unmodifiableList(new ArrayList<>(items)),
                Collections.unmodifiableList(new ArrayList<>(currencies)),
                Collections.unmodifiableList(new ArrayList<>(commands)),
                sourcePlugin,
                deliverAt,
                expireAt
            );
        }
    }
}
