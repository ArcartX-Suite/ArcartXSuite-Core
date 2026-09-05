package xuanmo.arcartxsuite.api.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * 模块存储层统一基类。
 * <p>
 * 自 1.5.0 起重构为单一模式：只依赖一个由 {@link StorageManager#resolveModuleDataSource}
 * 解析出的 {@link DataSource}（永不为 {@code null}），连接池生命周期完全由
 * {@link StorageManager} 统一管控。子类只需关注建表逻辑和业务 SQL。
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // 在 Module.startService 中：
 * DataSource ds = storageManager.resolveModuleDataSource("mymodule",
 *     config.storage().sqliteFileName(), dataFolder,
 *     config.storage().hasOverride() ? config.storage().toDescriptor() : null);
 * StorageDescriptor desc = config.storage().hasOverride()
 *     ? config.storage().toDescriptor()
 *     : storageManager.getDescriptor().withTablePrefix(config.storage().tablePrefix());
 * repo = new JdbcMyRepository("AXS-MyModule", ds, desc, dataFolder, logger);
 * repo.initialize();  // 只建表，不建池（池由 StorageManager 管理）
 * }</pre>
 *
 * @since 1.0.0
 */
public abstract class AbstractModuleRepository {

    protected final String poolName;
    protected final File dataFolder;
    protected final Logger logger;

    /** 存储描述符，提供方言信息（{@code isMysql()}）和表前缀。 */
    protected final StorageDescriptor descriptor;

    /** 底层数据源（由 StorageManager 解析，永不为 null）。 */
    private final DataSource dataSource;

    /** 模块表前缀。 */
    private final String tablePrefix;

    /**
     * 构造模块存储基类。
     *
     * @param poolName   连接池名（日志/诊断用）
     * @param dataSource 数据源（由 {@link StorageManager#resolveModuleDataSource} 解析，永不为 null）
     * @param descriptor 存储描述符（提供方言信息和表前缀）
     * @param dataFolder 模块数据目录（迁移 SQLite 目标文件时使用）
     * @param logger     模块 logger
     * @since 1.5.0
     */
    protected AbstractModuleRepository(String poolName, DataSource dataSource,
                                       StorageDescriptor descriptor, File dataFolder,
                                       Logger logger) {
        this.poolName = poolName;
        this.dataSource = dataSource;
        this.descriptor = descriptor;
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.tablePrefix = descriptor != null ? descriptor.tablePrefix() : "";
    }

    public final StorageDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * 返回模块表前缀。
     *
     * @return 表前缀，无前缀时返回空字符串
     */
    public final String tablePrefix() {
        return tablePrefix;
    }

    // ─── 生命周期 ─────────────────────────────────────────────

    /**
     * 建表。幂等——重复调用安全（依赖 {@code CREATE TABLE IF NOT EXISTS}）。
     * <p>
     * 连接池由 {@link StorageManager} 管理，本方法只通过传入连接执行建表逻辑。
     */
    public final void initialize() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            onInitialize(conn);
        }
        logger.info(poolName + " 数据库已初始化 (" + (isMysql() ? "MySQL" : "SQLite") + ")");
    }

    /**
     * 空操作——连接池由 {@link StorageManager} 统一管理。
     * <p>
     * 模块应在 {@code stopService()} 中调用
     * {@code storageManager.closeModuleDataSource(moduleId)} 释放 SQLite 文件锁。
     * 保留本方法仅为向后兼容，调用它不会产生任何副作用。
     */
    public void shutdown() {
        // no-op：连接池生命周期由 StorageManager 统一管控
    }

    public final boolean isAvailable() {
        return dataSource != null;
    }

    /**
     * 获取底层数据源，供 DAO 或服务层直接使用。
     */
    public final DataSource dataSource() {
        return dataSource;
    }

    // ─── 玩家数据删除 ────────────────────────────────────────

    /**
     * 删除指定玩家在该模块的全部数据。
     * <p>
     * 所有表的删除操作在同一个事务中执行，确保删到一半失败时不会留下半残数据。
     *
     * @param playerUuid 玩家 UUID
     * @return 总受影响行数
     */
    public final int deletePlayerData(UUID playerUuid) throws SQLException {
        // 优先使用子类自定义的多列删除逻辑（适用于不同表用不同 UUID 列名的模块）
        int custom = onPurgePlayerData(playerUuid);
        if (custom >= 0) {
            return custom;
        }
        List<String> tables = playerDataTables();
        if (tables == null || tables.isEmpty()) {
            return 0;
        }
        String column = playerUuidColumn();
        int total = 0;
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for (String table : tables) {
                    String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, playerUuid.toString());
                        total += ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
        return total;
    }

    /**
     * 清空该模块所有玩家数据表。
     * <p>
     * 所有表的清空操作在同一个事务中执行，确保中途失败时不会留下半残数据。
     *
     * @return 总受影响行数
     */
    public final int deleteAllPlayerData() throws SQLException {
        List<String> tables = playerDataTables();
        if (tables == null || tables.isEmpty()) {
            return 0;
        }
        int total = 0;
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for (String table : tables) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        total += stmt.executeUpdate("DELETE FROM " + table);
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
        return total;
    }

    // ─── 子类需实现 ─────────────────────────────────────────

    /**
     * 建表 / 索引逻辑。在首次 {@link #initialize()} 时通过传入的连接调用一次。
     */
    protected abstract void onInitialize(Connection connection) throws SQLException;

    /**
     * 返回该模块存储玩家数据的所有表名。
     * 用于 {@link #deletePlayerData(UUID)} 统一清除。
     * 若模块无玩家数据可返回空列表。
     */
    protected abstract List<String> playerDataTables();

    /**
     * 玩家 UUID 在表中的列名，默认 {@code "player_uuid"}。
     */
    protected String playerUuidColumn() {
        return "player_uuid";
    }

    /**
     * 自定义玩家数据删除逻辑（适用于不同表使用不同 UUID 列名的模块）。
     * <p>
     * 默认返回 {@code -1}，表示走 {@link #playerDataTables()} + {@link #playerUuidColumn()} 统一删除路径。
     * 子类可覆写此方法，返回 {@code >= 0} 的受影响行数，跳过默认路径。
     *
     * @param playerUuid 玩家 UUID
     * @return 受影响行数（{@code >= 0}），或 {@code -1} 表示使用默认路径
     */
    protected int onPurgePlayerData(UUID playerUuid) throws SQLException {
        return -1;
    }

    // ─── 工具方法 ─────────────────────────────────────────────

    /**
     * 获取连接。子类业务方法使用。
     */
    protected final Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException(poolName + " 数据源不可用");
        }
        return dataSource.getConnection();
    }

    /**
     * 判断当前是否为 MySQL 方言。
     */
    protected final boolean isMysql() {
        return descriptor != null && descriptor.isMysql();
    }

    /**
     * MySQL 使用 AUTO_INCREMENT，SQLite 使用 AUTOINCREMENT。
     */
    protected final String autoIncrement() {
        return isMysql() ? "AUTO_INCREMENT" : "AUTOINCREMENT";
    }

    /**
     * 静默执行 SQL（忽略异常，适用于 CREATE INDEX IF NOT EXISTS 等）。
     */
    protected final void tryExecute(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    /**
     * 在现有活跃 Connection 句柄上静默执行 SQL。
     */
    protected final void tryExecute(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    /**
     * 执行单条查询并返回第一行第一列的 int 值，不存在则返回 defaultValue。
     */
    protected final int queryInt(String sql, int defaultValue, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : defaultValue;
            }
        }
    }

    /**
     * 该 Repository 负责的所有数据表名列表，包括业务表和玩家表。
     * 用于跨数据源一键迁移，默认返回 {@link #playerDataTables()}。
     */
    protected List<String> allTables() {
        return playerDataTables();
    }

    /**
     * 跨源一键将当前数据源中的数据，完全克隆/迁移到目标数据源对应的连接池上。
     * <p>
     * 本方法委托给 {@link MigrationService} 执行，支持 SQLite ↔ MySQL 双向互迁，
     * 自动创建目标表结构并支持事务分批透传。
     *
     * @param targetDescriptor 目标数据库描述符
     * @param overwriteTarget  是否覆盖目标表原有数据
     * @return 迁移报告
     */
    public final MigrationResult migrateData(StorageDescriptor targetDescriptor, boolean overwriteTarget) {
        if (!isAvailable()) {
            return new MigrationResult(false, 0, 0);
        }
        List<String> tables = allTables();
        if (tables == null || tables.isEmpty()) {
            return new MigrationResult(true, 0, 0);
        }
        return MigrationService.migrate(
            dataSource(),
            targetDescriptor,
            dataFolder,
            poolName,
            conn -> {
                try {
                    onInitialize(conn);
                } catch (SQLException e) {
                    throw new RuntimeException("目标建表失败: " + e.getMessage(), e);
                }
                return null;
            },
            tables,
            overwriteTarget
        );
    }
}
