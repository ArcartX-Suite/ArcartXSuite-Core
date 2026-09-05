package xuanmo.arcartxsuite.api.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 跨数据源一键迁移服务。
 * <p>
 * 从 {@link AbstractModuleRepository} 抽离的迁移逻辑，支持 SQLite ↔ MySQL 双向互迁。
 * 自动创建目标表结构并支持事务分批透传。
 *
 * <h3>改进点（相比原 AbstractModuleRepository.migrateData）</h3>
 * <ul>
 *   <li>独立类，符合 SRP，可独立测试与扩展</li>
 *   <li>分批提交大小从 500 提升到 2000，减少大表迁移的 commit 次数</li>
 *   <li>源连接按表逐次获取释放，避免大表迁移期间长期占用连接池</li>
 *   <li>目标连接建表后立即 commit，避免建表 SQL 与数据插入混在同一事务</li>
 * </ul>
 *
 * @since 1.5.0
 */
public final class MigrationService {

    /** 分批提交的行数阈值。 */
    private static final int BATCH_SIZE = 2000;

    private MigrationService() {}

    /**
     * 执行跨源数据迁移。
     *
     * @param sourceDataSource   源数据源
     * @param targetDescriptor   目标数据库描述符
     * @param dataFolder         SQLite 文件存放目录（SQLite 目标时使用）
     * @param poolName           连接池名（日志/诊断用）
     * @param tableInitializer   目标建表回调（传入目标连接，执行 CREATE TABLE）
     * @param tables             待迁移的表名列表
     * @param overwriteTarget    是否覆盖目标表原有数据
     * @return 迁移报告
     */
    public static MigrationResult migrate(
        DataSource sourceDataSource,
        StorageDescriptor targetDescriptor,
        File dataFolder,
        String poolName,
        Function<Connection, Void> tableInitializer,
        List<String> tables,
        boolean overwriteTarget
    ) {
        if (sourceDataSource == null) {
            return new MigrationResult(false, 0, 0);
        }
        if (tables == null || tables.isEmpty()) {
            return new MigrationResult(true, 0, 0);
        }

        HikariDataSource targetDS = null;
        try {
            targetDS = createTargetDataSource(targetDescriptor, dataFolder, poolName);

            // 建表（独立事务，立即 commit）
            try (Connection conn = targetDS.getConnection()) {
                conn.setAutoCommit(true);
                tableInitializer.apply(conn);
            }

            int migratedCount = 0;
            long totalRows = 0;
            MigrationResult result = new MigrationResult(true, 0, 0);

            for (String table : tables) {
                try {
                    long tableRows = migrateSingleTable(table, overwriteTarget, sourceDataSource, targetDS);
                    result.addTableRow(table, tableRows);
                    totalRows += tableRows;
                    migratedCount++;
                } catch (Exception e) {
                    result.addError("迁移表 " + table + " 失败: " + e.getMessage());
                }
            }

            final int finalMigratedCount = migratedCount;
            final long finalTotalRows = totalRows;
            MigrationResult report = new MigrationResult(result.errors().isEmpty(), finalMigratedCount, finalTotalRows);
            report.tableRows().putAll(result.tableRows());
            report.errors().addAll(result.errors());
            return report;

        } catch (Exception e) {
            MigrationResult res = new MigrationResult(false, 0, 0);
            res.addError("初始化目标迁移连接池失败: " + e.getMessage());
            return res;
        } finally {
            if (targetDS != null && !targetDS.isClosed()) {
                targetDS.close();
            }
        }
    }

    private static HikariDataSource createTargetDataSource(StorageDescriptor desc, File dataFolder, String poolName) {
        HikariConfig hc = new HikariConfig();
        hc.setPoolName(poolName + "-TargetTemp");
        hc.setMinimumIdle(1);
        hc.setAutoCommit(false);

        if (desc.isMysql()) {
            hc.setMaximumPoolSize(2);
            String jdbcUrl = "jdbc:mysql://" + desc.host() + ":" + desc.port()
                + "/" + desc.database()
                + "?useSSL=true&characterEncoding=UTF-8&serverTimezone=UTC";
            hc.setJdbcUrl(jdbcUrl);
            hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hc.setUsername(desc.username());
            hc.setPassword(desc.password());
        } else {
            File sqliteFile = new File(dataFolder, desc.sqliteFileName());
            if (!sqliteFile.getParentFile().exists()) {
                sqliteFile.getParentFile().mkdirs();
            }
            hc.setJdbcUrl("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
            hc.setDriverClassName("org.sqlite.JDBC");
            hc.setMaximumPoolSize(1);
            hc.setConnectionTestQuery("SELECT 1");
        }
        return new HikariDataSource(hc);
    }

    /**
     * 迁移单表数据。源连接按表逐次获取释放，避免长期占用源连接池。
     */
    private static long migrateSingleTable(String table, boolean overwrite,
                                           DataSource sourceDS, HikariDataSource targetDS) throws SQLException {
        long count = 0;
        // 源连接：按表逐次获取，读完即释放
        try (Connection srcConn = sourceDS.getConnection();
             PreparedStatement srcPs = srcConn.prepareStatement("SELECT * FROM " + table);
             ResultSet rs = srcPs.executeQuery()) {

            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // 构造 INSERT SQL
            StringBuilder insertSql = new StringBuilder("INSERT INTO " + table + " (");
            StringBuilder placeholders = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    insertSql.append(", ");
                    placeholders.append(", ");
                }
                insertSql.append(meta.getColumnName(i));
                placeholders.append("?");
            }
            insertSql.append(") VALUES (").append(placeholders).append(")");

            // 目标连接：独立获取，事务内执行
            try (Connection destConn = targetDS.getConnection()) {
                destConn.setAutoCommit(false);

                if (overwrite) {
                    try (java.sql.Statement stmt = destConn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM " + table);
                    }
                }

                try (PreparedStatement destPs = destConn.prepareStatement(insertSql.toString())) {
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            destPs.setObject(i, rs.getObject(i));
                        }
                        destPs.addBatch();
                        count++;

                        if (count % BATCH_SIZE == 0) {
                            destPs.executeBatch();
                            destConn.commit();
                        }
                    }
                    destPs.executeBatch();
                    destConn.commit();
                } catch (SQLException e) {
                    destConn.rollback();
                    throw e;
                }
            }
        }
        return count;
    }
}
