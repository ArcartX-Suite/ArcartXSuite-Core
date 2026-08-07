package xuanmo.arcartxsuite.api.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xuanmo.arcartxsuite.api.placeholder.PlaceholderResolverAPI;

public final class CurrencyBridgeManager implements CurrencyBridgeAPI {

    private static final String RANGE_FAILURE = "金额超出该货币可处理范围。";

    private final JavaPlugin plugin;
    private final Map<String, CurrencyDefinition> registeredDefinitions;
    private volatile Map<String, CurrencyDefinition> definitions;
    private volatile Map<String, CurrencyBridge> bridges;
    private final PlaceholderResolverAPI placeholderResolver;
    private final Map<UUID, Object> customOperationLocks = new ConcurrentHashMap<>();

    public CurrencyBridgeManager(JavaPlugin plugin) {
        this(plugin, Map.of(), null);
    }

    public CurrencyBridgeManager(JavaPlugin plugin, Map<String, CurrencyDefinition> definitions) {
        this(plugin, definitions, null);
    }

    public CurrencyBridgeManager(JavaPlugin plugin, Map<String, CurrencyDefinition> definitions, PlaceholderResolverAPI placeholderResolver) {
        this.plugin = plugin;
        this.registeredDefinitions = definitions == null ? new LinkedHashMap<>() : new LinkedHashMap<>(definitions);
        this.definitions = immutableCopy(this.registeredDefinitions);
        this.bridges = Map.of();
        this.placeholderResolver = placeholderResolver;
    }

    public synchronized void initialize() {
        LinkedHashMap<String, CurrencyDefinition> definitionSnapshot = new LinkedHashMap<>(registeredDefinitions);
        LinkedHashMap<String, CurrencyBridge> bridgeSnapshot = new LinkedHashMap<>();
        for (CurrencyDefinition definition : definitionSnapshot.values()) {
            bridgeSnapshot.put(definition.id(), createBridge(definition));
        }
        definitions = immutableCopy(definitionSnapshot);
        bridges = immutableCopy(bridgeSnapshot);
    }

    /**
     * 动态注册额外的货币定义（不覆盖已有）。
     * 注册后需调用 {@link #initialize()} 生效。
     */
    public synchronized void registerCurrencies(Map<String, CurrencyDefinition> additional) {
        if (additional == null || additional.isEmpty()) {
            return;
        }
        for (Map.Entry<String, CurrencyDefinition> entry : additional.entrySet()) {
            registeredDefinitions.putIfAbsent(normalizeId(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public CurrencyBridge bridge(String currencyId) {
        return bridges.get(normalizeId(currencyId));
    }

    @Override
    public CurrencyDefinition definition(String currencyId) {
        return definitions.get(normalizeId(currencyId));
    }

    @Override
    public Collection<CurrencyDefinition> definitions() {
        return definitions.values();
    }

    @Override
    public Set<String> currencyIds() {
        return bridges.keySet();
    }

    @Override
    public String format(String currencyId, BigDecimal amount) {
        CurrencyBridge bridge = bridge(currencyId);
        CurrencyDefinition definition = bridge == null ? definitions.get(normalizeId(currencyId)) : bridge.definition();
        if (definition == null) {
            return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
        }
        BigDecimal scaled = (amount == null ? BigDecimal.ZERO : amount).setScale(definition.scale(), roundingMode(definition.rounding()));
        return scaled.stripTrailingZeros().toPlainString();
    }

    private CurrencyBridge createBridge(CurrencyDefinition definition) {
        return switch (normalizeId(definition.provider())) {
            case "playerpoints" -> new PlayerPointsBridge(definition);
            case "xconomy" -> new XConomyCurrencyBridge(definition);
            case "placeholder-command", "command", "custom" -> new CommandCurrencyBridge(definition, placeholderResolver);
            case "rondo" -> new RondoCurrencyBridge(definition);
            default -> new VaultCurrencyBridge(definition);
        };
    }

    private abstract class AbstractCurrencyBridge implements CurrencyBridge {

        private final CurrencyDefinition definition;

        private AbstractCurrencyBridge(CurrencyDefinition definition) {
            this.definition = definition;
        }

        @Override
        public CurrencyDefinition definition() {
            return definition;
        }

        protected BigDecimal normalize(BigDecimal amount) {
            if (amount == null) {
                return BigDecimal.ZERO;
            }
            return amount.max(BigDecimal.ZERO).setScale(definition.scale(), roundingMode(definition.rounding()));
        }

        protected String formatAmount(BigDecimal amount) {
            return normalize(amount).stripTrailingZeros().toPlainString();
        }
    }

    private final class VaultCurrencyBridge extends AbstractCurrencyBridge {

        private net.milkbowl.vault.economy.Economy economy;
        private String unavailableReason = "";

        private VaultCurrencyBridge(CurrencyDefinition definition) {
            super(definition);
            initializeVault();
        }

        @Override
        public boolean available() {
            if (economy == null) {
                initializeVault();
            }
            return economy != null;
        }

        @Override
        public String unavailableReason() {
            return unavailableReason;
        }

        @Override
        public BigDecimal balance(Player player) {
            if (!available() || player == null) {
                return BigDecimal.ZERO;
            }
            try {
                return BigDecimal.valueOf(economy.getBalance(player));
            } catch (Exception | LinkageError exception) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        public CurrencyTransactionResult withdraw(Player player, BigDecimal amount) {
            return invokeEconomyTransaction(player, amount, "扣款失败。", true);
        }

        @Override
        public CurrencyTransactionResult deposit(Player player, BigDecimal amount) {
            return invokeEconomyTransaction(player, amount, "入账失败。", false);
        }

        private void initializeVault() {
            if (economy != null) {
                return;
            }
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault == null) {
                unavailableReason = "Vault 未安装";
                return;
            }
            try {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> registration =
                    Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (registration == null || registration.getProvider() == null) {
                    unavailableReason = "Vault Economy 服务未注册";
                    return;
                }
                economy = registration.getProvider();
                unavailableReason = "";
            } catch (Exception | LinkageError exception) {
                unavailableReason = "Vault API 初始化失败: " + exception.getMessage();
            }
        }

        private CurrencyTransactionResult invokeEconomyTransaction(Player player, BigDecimal amount, String defaultMessage, boolean isWithdraw) {
            if (!available()) {
                return CurrencyTransactionResult.failure(unavailableReason());
            }
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            try {
                double value = normalize(amount).doubleValue();
                net.milkbowl.vault.economy.EconomyResponse response = isWithdraw
                    ? economy.withdrawPlayer(player, value)
                    : economy.depositPlayer(player, value);
                return response.transactionSuccess()
                    ? CurrencyTransactionResult.ok()
                    : CurrencyTransactionResult.failure(response.errorMessage != null && !response.errorMessage.isBlank() ? response.errorMessage : defaultMessage);
            } catch (Exception | LinkageError exception) {
                return CurrencyTransactionResult.failure(defaultMessage);
            }
        }
    }

    private final class PlayerPointsBridge extends AbstractCurrencyBridge {

        private org.black_ixx.playerpoints.PlayerPointsAPI api;
        private String unavailableReason = "";

        private PlayerPointsBridge(CurrencyDefinition definition) {
            super(definition);
            initializePlayerPoints();
        }

        @Override
        public boolean available() {
            return api != null;
        }

        @Override
        public String unavailableReason() {
            return unavailableReason;
        }

        @Override
        public BigDecimal balance(Player player) {
            if (!available() || player == null) {
                return BigDecimal.ZERO;
            }
            try {
                return BigDecimal.valueOf(api.look(player.getUniqueId()));
            } catch (Exception | LinkageError exception) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        public CurrencyTransactionResult withdraw(Player player, BigDecimal amount) {
            return invokePointsTransaction(player, amount, "扣除点券失败。", true);
        }

        @Override
        public CurrencyTransactionResult deposit(Player player, BigDecimal amount) {
            return invokePointsTransaction(player, amount, "发放点券失败。", false);
        }

        private void initializePlayerPoints() {
            Plugin playerPoints = Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (playerPoints == null) {
                unavailableReason = "PlayerPoints 未安装";
                return;
            }
            try {
                if (playerPoints instanceof org.black_ixx.playerpoints.PlayerPoints pp) {
                    api = pp.getAPI();
                } else {
                    org.black_ixx.playerpoints.PlayerPoints pp = org.black_ixx.playerpoints.PlayerPoints.getInstance();
                    api = pp.getAPI();
                }
                if (api == null) {
                    unavailableReason = "PlayerPoints API 不可用";
                }
            } catch (Exception | LinkageError exception) {
                unavailableReason = "PlayerPoints API 初始化失败: " + exception.getMessage();
            }
        }

        private CurrencyTransactionResult invokePointsTransaction(Player player, BigDecimal amount, String defaultMessage, boolean isTake) {
            if (!available()) {
                return CurrencyTransactionResult.failure(unavailableReason());
            }
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            int points;
            try {
                points = normalize(amount).intValueExact();
            } catch (ArithmeticException exception) {
                return CurrencyTransactionResult.failure(RANGE_FAILURE);
            }
            if (points <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            try {
                boolean success = isTake
                    ? api.take(player.getUniqueId(), points)
                    : api.give(player.getUniqueId(), points);
                return success ? CurrencyTransactionResult.ok() : CurrencyTransactionResult.failure(defaultMessage);
            } catch (Exception | LinkageError exception) {
                return CurrencyTransactionResult.failure(defaultMessage);
            }
        }
    }

    private final class XConomyCurrencyBridge extends AbstractCurrencyBridge {

        private me.yic.xconomy.api.XConomyAPI api;
        private String unavailableReason = "";

        private XConomyCurrencyBridge(CurrencyDefinition definition) {
            super(definition);
            initializeXConomy();
        }

        @Override
        public boolean available() {
            return api != null;
        }

        @Override
        public String unavailableReason() {
            return unavailableReason;
        }

        @Override
        public BigDecimal balance(Player player) {
            if (!available() || player == null) {
                return BigDecimal.ZERO;
            }
            try {
                me.yic.xconomy.data.syncdata.PlayerData playerData = api.getPlayerData(player.getUniqueId());
                if (playerData == null) {
                    return BigDecimal.ZERO;
                }
                BigDecimal balance = playerData.getBalance();
                return balance != null ? balance : BigDecimal.ZERO;
            } catch (Exception | LinkageError exception) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        public CurrencyTransactionResult withdraw(Player player, BigDecimal amount) {
            return changeBalance(player, amount, Boolean.FALSE, "扣款失败。");
        }

        @Override
        public CurrencyTransactionResult deposit(Player player, BigDecimal amount) {
            return changeBalance(player, amount, Boolean.TRUE, "入账失败。");
        }

        private CurrencyTransactionResult changeBalance(
            Player player,
            BigDecimal amount,
            Boolean isAdd,
            String defaultMessage
        ) {
            if (!available()) {
                return CurrencyTransactionResult.failure(unavailableReason());
            }
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            try {
                int result = api.changePlayerBalance(
                    player.getUniqueId(),
                    player.getName(),
                    normalize(amount),
                    isAdd,
                    "ArcartXSuite"
                );
                return switch (result) {
                    case 0 -> CurrencyTransactionResult.ok();
                    case 1 -> CurrencyTransactionResult.failure("当前不允许修改余额。");
                    case 2 -> CurrencyTransactionResult.failure("余额不足。");
                    case 3 -> CurrencyTransactionResult.failure(RANGE_FAILURE);
                    default -> CurrencyTransactionResult.failure(defaultMessage);
                };
            } catch (Exception | LinkageError exception) {
                return CurrencyTransactionResult.failure(defaultMessage);
            }
        }

        private void initializeXConomy() {
            Plugin xconomy = Bukkit.getPluginManager().getPlugin("XConomy");
            if (xconomy == null) {
                unavailableReason = "XConomy 未安装";
                return;
            }
            try {
                api = new me.yic.xconomy.api.XConomyAPI();
            } catch (Exception | LinkageError exception) {
                unavailableReason = "XConomy API 初始化失败: " + exception.getMessage();
            }
        }
    }

    private final class CommandCurrencyBridge extends AbstractCurrencyBridge {

        private final PlaceholderResolverAPI placeholderResolver;
        private String unavailableReason = "";

        private CommandCurrencyBridge(CurrencyDefinition definition, PlaceholderResolverAPI placeholderResolver) {
            super(definition);
            this.placeholderResolver = placeholderResolver;
            initializePlaceholderApi();
        }

        @Override
        public boolean available() {
            return placeholderResolver != null
                && !definition().balancePlaceholder().isBlank()
                && !definition().withdrawCommand().isBlank()
                && !definition().depositCommand().isBlank();
        }

        @Override
        public String unavailableReason() {
            return unavailableReason;
        }

        @Override
        public BigDecimal balance(Player player) {
            if (!available() || player == null) {
                return BigDecimal.ZERO;
            }
            String resolved = placeholderResolver.applyPlaceholders(player, definition().balancePlaceholder());
            return parseNumericString(resolved);
        }

        @Override
        public CurrencyTransactionResult withdraw(Player player, BigDecimal amount) {
            if (!available()) {
                return CurrencyTransactionResult.failure(unavailableReason());
            }
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            Object lock = customOperationLocks.computeIfAbsent(player.getUniqueId(), ignored -> new Object());
            synchronized (lock) {
                if (balance(player).compareTo(normalize(amount)) < 0) {
                    return CurrencyTransactionResult.failure("余额不足。");
                }
                // 仅串行化本插件内的操作；外部插件并发修改余额仍由外部系统负责。
                return dispatchConsoleCommand(definition().withdrawCommand(), player, amount)
                    ? CurrencyTransactionResult.ok()
                    : CurrencyTransactionResult.failure("执行扣款命令失败。");
            }
        }
        @Override
        public CurrencyTransactionResult deposit(Player player, BigDecimal amount) {
            if (!available()) {
                return CurrencyTransactionResult.failure(unavailableReason());
            }
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            return dispatchConsoleCommand(definition().depositCommand(), player, amount)
                ? CurrencyTransactionResult.ok()
                : CurrencyTransactionResult.failure("执行发放命令失败。");
        }

        private void initializePlaceholderApi() {
            if (placeholderResolver == null) {
                unavailableReason = "PlaceholderAPI 解析器未注入";
                return;
            }
            if (!placeholderResolver.available()) {
                unavailableReason = "PlaceholderAPI 未安装";
            }
        }

        private boolean dispatchConsoleCommand(String template, Player player, BigDecimal amount) {
            String rendered = template
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%amount%", formatAmount(amount));
            if (rendered.startsWith("/")) {
                rendered = rendered.substring(1);
            }
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rendered);
        }
    }

    private final class RondoCurrencyBridge extends AbstractCurrencyBridge {

        // Rondo 原生 API（多货币场景使用）
        private priv.seventeen.artist.rondo.api.RondoAPI rondoApi;
        private String rondoUnavailableReason = "";

        // Vault 回退（Rondo 是 Vault 提供者时，单货币走 Vault 更可靠）
        private net.milkbowl.vault.economy.Economy vaultEconomy;
        private String vaultUnavailableReason = "";

        private RondoCurrencyBridge(CurrencyDefinition definition) {
            super(definition);
            initializeVault();
            initializeRondo();
        }

        @Override
        public boolean available() {
            return vaultEconomy != null || rondoApi != null;
        }

        @Override
        public String unavailableReason() {
            if (available()) {
                return "";
            }
            if (!vaultUnavailableReason.isBlank()) {
                return vaultUnavailableReason;
            }
            return rondoUnavailableReason.isBlank() ? "Rondo/Vault 货币后端不可用" : rondoUnavailableReason;
        }

        @Override
        public BigDecimal balance(Player player) {
            if (player == null) return BigDecimal.ZERO;
            // 优先 Vault（Rondo 作为 Vault 提供者时无需货币 ID 映射）
            if (vaultEconomy != null) {
                try {
                    return BigDecimal.valueOf(vaultEconomy.getBalance(player));
                } catch (Exception | LinkageError ignored) {
                }
            }
            // 回退 Rondo 原生
            if (rondoApi != null) {
                try {
                    BigDecimal result = rondoApi.getBalance(player.getUniqueId(), definition().id());
                    return result != null ? result : BigDecimal.ZERO;
                } catch (Exception | LinkageError ignored) {
                }
            }
            return BigDecimal.ZERO;
        }

        @Override
        public CurrencyTransactionResult withdraw(Player player, BigDecimal amount) {
            return invokeTransaction(player, amount, "扣款失败。", true);
        }

        @Override
        public CurrencyTransactionResult deposit(Player player, BigDecimal amount) {
            return invokeTransaction(player, amount, "入账失败。", false);
        }

        private CurrencyTransactionResult invokeTransaction(
            Player player, BigDecimal amount, String defaultMessage, boolean isWithdraw
        ) {
            if (player == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return CurrencyTransactionResult.failure("金额必须大于 0。");
            }
            // 优先 Vault
            if (vaultEconomy != null) {
                try {
                    double value = normalize(amount).doubleValue();
                    net.milkbowl.vault.economy.EconomyResponse response = isWithdraw
                        ? vaultEconomy.withdrawPlayer(player, value)
                        : vaultEconomy.depositPlayer(player, value);
                    return response.transactionSuccess()
                        ? CurrencyTransactionResult.ok()
                        : CurrencyTransactionResult.failure(response.errorMessage != null && !response.errorMessage.isBlank() ? response.errorMessage : defaultMessage);
                } catch (Exception | LinkageError ignored) {
                }
            }
            // 回退 Rondo 原生
            if (rondoApi != null) {
                try {
                    boolean success = isWithdraw
                        ? rondoApi.withdraw(player.getUniqueId(), definition().id(), normalize(amount), "ArcartXSuite")
                        : rondoApi.deposit(player.getUniqueId(), definition().id(), normalize(amount), "ArcartXSuite");
                    return success ? CurrencyTransactionResult.ok() : CurrencyTransactionResult.failure("Rondo " + defaultMessage);
                } catch (Exception | LinkageError ignored) {
                }
            }
            return CurrencyTransactionResult.failure(defaultMessage);
        }

        private void initializeVault() {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault == null) {
                vaultUnavailableReason = "Vault 未安装";
                return;
            }
            try {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> registration =
                    Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (registration == null || registration.getProvider() == null) {
                    vaultUnavailableReason = "Vault Economy 服务未注册";
                    return;
                }
                vaultEconomy = registration.getProvider();
                vaultUnavailableReason = "";
            } catch (Exception | LinkageError exception) {
                vaultUnavailableReason = "Vault API 初始化失败: " + exception.getMessage();
            }
        }

        private void initializeRondo() {
            Plugin rondo = Bukkit.getPluginManager().getPlugin("Rondo");
            if (rondo == null) {
                rondoUnavailableReason = "Rondo 未安装";
                return;
            }
            try {
                rondoApi = priv.seventeen.artist.rondo.api.RondoAPI.INSTANCE;
                if (rondoApi == null) {
                    rondoUnavailableReason = "Rondo API 实例不可用";
                }
            } catch (Exception | LinkageError exception) {
                rondoUnavailableReason = "Rondo API 初始化失败: " + exception.getMessage();
            }
        }
    }

    private static <T> Map<String, T> immutableCopy(Map<String, T> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static RoundingMode roundingMode(String value) {
        if (value == null || value.isBlank()) {
            return RoundingMode.DOWN;
        }
        try {
            return RoundingMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return RoundingMode.DOWN;
        }
    }

    private static BigDecimal parseNumericString(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalized = rawValue
            .replace(",", "")
            .replaceAll("[^0-9.\\-]", "");
        if (normalized.isBlank() || "-".equals(normalized) || ".".equals(normalized) || "-.".equals(normalized)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private static String normalizeId(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
