package xuanmo.arcartxsuite.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 临时 OP 执行器，在执行单条命令期间短暂授予玩家 OP 权限并立即回收。
 * <p>
 * 通过持久化台账（ledger 文件）记录临时 OP 状态，服务端异常重启后可在
 * {@link #reconcile()} 时清理遗留的 OP 玩家。提供命令黑名单（op/deop/stop 等）
 * 防止提权滥用，并通过反射 {@code CraftHumanEntity.op} 实现"静默 OP"，
 * 避免写入 ops.json 或触发平台事件。
 */
public final class TemporaryOpExecutor {

    private static final Object LEDGER_LOCK = new Object();
    private static final ConcurrentHashMap<UUID, Object> PLAYER_LOCKS = new ConcurrentHashMap<>();
    private static final Set<String> BLOCKED_COMMANDS = Set.of(
        "op", "deop", "stop", "restart", "reload", "rl", "whitelist",
        "ban", "ban-ip", "pardon", "kick", "pex", "lp", "luckperms",
        "perm", "perms", "permission", "permissions", "manuadd", "manuaddp",
        "bukkit:op", "minecraft:op"
    );
    private static final Set<UUID> PENDING_PLAYERS = new HashSet<>();
    private static final Logger FALLBACK_LOGGER =
        Logger.getLogger(TemporaryOpExecutor.class.getName());

    private static File ledgerFile;
    private static Logger logger = FALLBACK_LOGGER;
    private static boolean initialized;
    private static boolean warnedUninitialized;

    // ── Silent OP 反射（避免 setOp 写 ops.json 文件和触发平台事件）──
    private static volatile boolean silentOpInitialized;
    private static volatile boolean silentOpAvailable;
    private static Field opField;

    private TemporaryOpExecutor() {
    }

    /**
     * 初始化执行器，指定台账文件与日志器。
     *
     * @param file          台账文件路径，非 null
     * @param injectedLogger 日志器，null 时使用内置 fallback
     */
    public static void init(File file, Logger injectedLogger) {
        Objects.requireNonNull(file, "file");
        synchronized (LEDGER_LOCK) {
            ledgerFile = file;
            logger = injectedLogger == null ? FALLBACK_LOGGER : injectedLogger;
            initialized = true;
            warnedUninitialized = false;
            PENDING_PLAYERS.clear();
        }
    }

    /**
     * 启动对账：读取台账中遗留的临时 OP 玩家并回收其 OP 权限，然后清空台账。
 * <p>
 * 用于服务端异常重启后清理上次未正常回收的临时 OP。
     */
    public static void reconcile() {
        synchronized (LEDGER_LOCK) {
            if (!initialized || ledgerFile == null) {
                logger.warning("TemporaryOpExecutor 尚未初始化，无法进行临时 OP 台账对账。");
                return;
            }
            Set<UUID> stalePlayers;
            try {
                stalePlayers = readLedgerLocked();
            } catch (IOException exception) {
                logger.log(Level.WARNING, "读取临时 OP 台账失败，跳过启动对账。", exception);
                return;
            }
            for (UUID uuid : stalePlayers) {
                try {
                    Bukkit.getOfflinePlayer(uuid).setOp(false);
                } catch (RuntimeException exception) {
                    logger.log(Level.WARNING,
                        "清理临时 OP 台账玩家失败: " + uuid, exception);
                }
            }
            PENDING_PLAYERS.clear();
            try {
                writeLedgerLocked();
            } catch (IOException exception) {
                logger.log(Level.WARNING, "清空临时 OP 台账失败。", exception);
            }
            if (!stalePlayers.isEmpty()) {
                logger.info("已清理上次异常退出遗留的临时 OP 玩家: "
                    + stalePlayers.stream().map(UUID::toString).sorted().toList());
            }
        }
    }

    /**
     * 以临时 OP 身份为玩家执行单条命令，执行完毕后立即回收 OP。
 * <p>
 * 命令在黑名单中或包含控制字符时拒绝执行。玩家已是 OP 时直接派发不记账。
     *
     * @param player  目标玩家，非 null
     * @param command 命令文本（可带前导斜杠）
     * @return {@code true} 表示命令派发成功
     */
    public static boolean execute(Player player, String command) {
        Objects.requireNonNull(player, "player");
        if (!isAllowedCommand(player, command)) {
            return false;
        }

        String normalizedCommand = commandWithoutLeadingSlash(command);
        boolean wasOp = player.isOp();
        if (wasOp) {
            return Bukkit.dispatchCommand(player, normalizedCommand);
        }

        // 玩家级锁：防止多线程同时为同一玩家执行临时 OP 导致竞态
        // （线程A恢复op=false时线程B还在以OP权限执行命令）
        Object playerLock = PLAYER_LOCKS.computeIfAbsent(player.getUniqueId(), k -> new Object());
        synchronized (playerLock) {
            boolean tracked = trackBeforeElevation(player);
            if (!tracked && initialized) {
                return false;
            }
            boolean usedSilentOp = setOpSilently(player, true);
            try {
                boolean result = Bukkit.dispatchCommand(player, normalizedCommand);
                if (initialized) {
                    logger.fine("临时 OP: player=" + player.getName() + " command=" + sanitizeForLog(normalizedCommand));
                }
                return result;
            } finally {
                boolean restored = false;
                try {
                    setOpSilently(player, false, usedSilentOp);
                    restored = true;
                } catch (RuntimeException exception) {
                    logger.log(Level.WARNING,
                        "恢复临时 OP 失败，台账将保留以便下次启动清理: " + player.getName(),
                        exception);
                }
                if (tracked && restored) {
                    untrackAfterRestoration(player.getUniqueId());
                }
            }
        }
    }

    private static void initSilentOp() {
        if (silentOpInitialized) return;
        synchronized (TemporaryOpExecutor.class) {
            if (silentOpInitialized) return;
            try {
                String pkg = Bukkit.getServer().getClass().getPackage().getName();
                Class<?> craftHumanEntityClass = Class.forName(pkg + ".entity.CraftHumanEntity");
                opField = craftHumanEntityClass.getDeclaredField("op");
                opField.setAccessible(true);
                silentOpAvailable = true;
                logger.fine("TemporaryOpExecutor: Silent OP 已启用（反射 CraftHumanEntity.op）。");
            } catch (Exception e) {
                silentOpAvailable = false;
                logger.fine("TemporaryOpExecutor: Silent OP 不可用，将使用标准 setOp: " + e.getMessage());
            }
            silentOpInitialized = true;
        }
    }

    private static boolean setOpSilently(Player player, boolean value) {
        initSilentOp();
        if (silentOpAvailable) {
            try {
                opField.setBoolean(player, value);
                player.recalculatePermissions();
                if (player.isOp() == value) return true;
                logger.fine("TemporaryOpExecutor: Silent OP 验证失败（isOp 未生效），本次降级到标准 setOp。");
            } catch (Exception e) {
                logger.fine("TemporaryOpExecutor: Silent OP 反射失败，本次降级到标准 setOp: " + e.getMessage());
            }
        }
        player.setOp(value);
        return false;
    }

    private static void setOpSilently(Player player, boolean value, boolean usedSilentOp) {
        if (usedSilentOp && silentOpAvailable) {
            try {
                opField.setBoolean(player, value);
                player.recalculatePermissions();
                if (player.isOp() == value) return;
            } catch (Exception ignored) {
            }
        }
        player.setOp(value);
    }

    private static boolean isAllowedCommand(Player player, String command) {
        if (command == null || command.isBlank()) {
            warnRejected(player, command, "命令为空");
            return false;
        }
        for (int index = 0; index < command.length(); index++) {
            if (Character.isISOControl(command.charAt(index))) {
                warnRejected(player, command, "命令包含控制字符");
                return false;
            }
        }
        String token = commandWithoutLeadingSlash(command).trim();
        int separator = -1;
        for (int index = 0; index < token.length(); index++) {
            if (Character.isWhitespace(token.charAt(index))) {
                separator = index;
                break;
            }
        }
        String commandName = (separator < 0 ? token : token.substring(0, separator))
            .toLowerCase(Locale.ROOT);
        int namespaceSeparator = commandName.lastIndexOf(':');
        String canonicalName = namespaceSeparator >= 0
            ? commandName.substring(namespaceSeparator + 1)
            : commandName;
        if (BLOCKED_COMMANDS.contains(commandName)
            || BLOCKED_COMMANDS.contains(canonicalName)) {
            warnRejected(player, command, "命令属于临时 OP 黑名单");
            return false;
        }
        return true;
    }

    private static String commandWithoutLeadingSlash(String command) {
        String trimmed = command.trim();
        return trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
    }

    private static boolean trackBeforeElevation(Player player) {
        synchronized (LEDGER_LOCK) {
            if (!initialized || ledgerFile == null) {
                if (!warnedUninitialized) {
                    logger.warning("TemporaryOpExecutor 尚未初始化，临时 OP 将跳过持久台账。");
                    warnedUninitialized = true;
                }
                return false;
            }
            UUID uuid = player.getUniqueId();
            PENDING_PLAYERS.add(uuid);
            try {
                writeLedgerLocked();
                return true;
            } catch (IOException exception) {
                PENDING_PLAYERS.remove(uuid);
                logger.log(Level.WARNING,
                    "写入临时 OP 台账失败，拒绝提升玩家权限: " + player.getName(), exception);
                return false;
            }
        }
    }

    private static void untrackAfterRestoration(UUID uuid) {
        synchronized (LEDGER_LOCK) {
            PENDING_PLAYERS.remove(uuid);
            try {
                writeLedgerLocked();
            } catch (IOException exception) {
                PENDING_PLAYERS.add(uuid);
                logger.log(Level.WARNING,
                    "移除临时 OP 台账记录失败，将保留记录以便下次启动清理: " + uuid,
                    exception);
            }
        }
    }

    private static Set<UUID> readLedgerLocked() throws IOException {
        Set<UUID> result = new HashSet<>();
        if (!ledgerFile.exists()) {
            return result;
        }
        for (String line : Files.readAllLines(ledgerFile.toPath(), StandardCharsets.UTF_8)) {
            String value = line.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                result.add(UUID.fromString(value));
            } catch (IllegalArgumentException exception) {
                logger.warning("忽略无效的临时 OP 台账 UUID: " + value);
            }
        }
        return result;
    }

    private static void writeLedgerLocked() throws IOException {
        Path target = ledgerFile.toPath();
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String content = PENDING_PLAYERS.stream()
            .map(UUID::toString)
            .sorted(Comparator.naturalOrder())
            .reduce((left, right) -> left + "\n" + right)
            .map(value -> value + "\n")
            .orElse("");
        Path temporary = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(
                temporary,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )) {
                channel.write(ByteBuffer.wrap(bytes));
                channel.force(true);
            }
            try {
                Files.move(temporary, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void warnRejected(Player player, String command, String reason) {
        logger.warning("拒绝临时 OP 命令: player=" + player.getName()
            + " reason=" + reason + " command=" + sanitizeForLog(command));
    }

    private static String sanitizeForLog(String value) {
        if (value == null) {
            return "<null>";
        }
        String sanitized = value.replaceAll("[\\r\\n\\t]", "?");
        return sanitized.length() > 512 ? sanitized.substring(0, 512) + "..." : sanitized;
    }
}
