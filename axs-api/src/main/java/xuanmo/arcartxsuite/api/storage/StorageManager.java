package xuanmo.arcartxsuite.api.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 宿主统一数据源管理器。
 * <p>
 * 由本体（axs-core）实现并在启动时创建唯一一个 HikariCP 连接池，
 * 通过 {@link xuanmo.arcartxsuite.api.ModuleContext#storageManager()} 注入给模块。
 * <p>
 * <h3>设计目标</h3>
 * <ul>
 *   <li><b>统一管控</b>：连接池生命周期由本体统一管理，模块无需自行 {@code initialize()}/{@code shutdown()}，
 *       彻底消除遗漏调用导致的「数据源不可用」和连接池泄漏问题。</li>
 *   <li><b>向后兼容</b>：模块若在自身配置中声明了独立的 storage 节（覆盖模式），
 *       仍可自建连接池走旧路径，旧 MySQL 数据库零改动。</li>
 *   <li><b>SQLite 独立</b>：SQLite 模式下各模块仍使用各自的 {@code .db} 文件，
 *       由本体按模块名分配独立连接，数据不合并。</li>
 * </ul>
 *
 * <h3>模块使用方式</h3>
 * <pre>{@code
 * // 在 startService 中：
 * JdbcMyRepository repo = new JdbcMyRepository(
 *     context.storageManager(),           // 本体共享数据源
 *     config.storage().tablePrefix(),     // 模块自己的表前缀
 *     logger);
 * repo.initialize();  // 只建表，不建池（池由本体管理）
 * }</pre>
 *
 * @since 1.4.0
 */
@ApiStability.Stable
public interface StorageManager {

    /**
     * 获取本体共享数据源。
     * <p>
     * MySQL 模式下返回全局唯一的 HikariCP 连接池；
     * SQLite 模式下返回按模块名分配的独立连接（各模块 {@code .db} 文件隔离）。
     *
     * @return 共享数据源，本体未配置存储时返回 {@code null}
     */
    DataSource getDataSource();

    /**
     * 获取当前全局存储描述符。
     *
     * @return 本体配置的 {@link StorageDescriptor}，本体未配置存储时返回 {@code null}
     */
    StorageDescriptor getDescriptor();

    /**
     * 数据源是否可用（已初始化且未关闭）。
     *
     * @return {@code true} 表示数据源可用
     */
    boolean isAvailable();

    /**
     * 从共享数据源获取连接。
     * <p>
     * 等价于 {@code getDataSource().getConnection()}，但会在数据源不可用时
     * 抛出带上下文的 {@link SQLException}。
     *
     * @return 数据库连接
     * @throws SQLException 数据源不可用或获取连接失败
     */
    Connection getConnection() throws SQLException;

    /**
     * 为指定模块名获取（或按需创建）SQLite 独立连接的数据源。
     * <p>
     * 仅 SQLite 模式下有意义：各模块使用各自的 {@code <moduleId>.db} 文件，
     * 本体按模块名缓存对应的 HikariCP 连接池（maximumPoolSize=1）。
     * <p>
     * MySQL 模式下此方法返回全局共享数据源（与 {@link #getDataSource()} 相同），
     * {@code sqliteFileName} 参数被忽略。
     *
     * @param moduleId       模块 ID（用于构造 SQLite 文件名）
     * @param sqliteFileName 模块自定义的 SQLite 文件名（如 {@code "lottery.db"}），
     *                       为 {@code null} 或空时使用 {@code <moduleId>.db}
     * @return 该模块对应的数据源
     * @throws SQLException 数据源不可用或创建失败
     */
    DataSource getModuleDataSource(String moduleId, String sqliteFileName) throws SQLException;

    /**
     * 关闭指定模块的 SQLite 独立数据源（仅 SQLite 模式下有效）。
     * <p>
     * 用于模块热更新（{@code /axs update}）时释放旧连接池，避免 SQLite 文件锁占用
     * 导致新模块加载时连接超时。MySQL 模式下此方法为空操作（全局共享池由本体统一管理）。
     *
     * @param moduleId 模块 ID，为 {@code null} 时按 {@code "default"} 处理
     * @since 1.4.1
     */
    void closeModuleDataSource(String moduleId);

    /**
     * 获取宿主本体专用的数据源。
     * <p>
     * 用于宿主自身维护的全局表（如 {@code axs_pending_rewards}），与模块数据源隔离。
     * <p>
     * MySQL 模式下返回全局共享数据源（与 {@link #getDataSource()} 相同）；
     * SQLite 模式下返回宿主专用的 {@code axs_host.db} 独立连接池。
     *
     * @return 宿主专用数据源
     * @throws SQLException 数据源不可用或创建失败
     * @since 1.4.2
     */
    DataSource getHostDataSource() throws SQLException;

    /**
     * 从宿主本体专用数据源获取连接。
     * <p>
     * 等价于 {@code getHostDataSource().getConnection()}，但会在数据源不可用时
     * 抛出带上下文的 {@link SQLException}。
     * <p>
     * MySQL 模式下与 {@link #getConnection()} 行为一致（共享全局池）；
     * SQLite 模式下返回宿主专用 {@code axs_host.db} 的连接。
     *
     * @return 数据库连接
     * @throws SQLException 数据源不可用或获取连接失败
     * @since 1.4.2
     */
    Connection getHostConnection() throws SQLException;

    /**
     * 根据模块配置解析出数据源（共享或自建），统一由 {@link StorageManager} 管理连接池生命周期。
     * <p>
     * 这是模块获取数据源的推荐入口，取代旧 {@code AbstractModuleRepository} 的双模式构造。
     *
     * <h4>共享模式（overrideDescriptor = null）</h4>
     * <ul>
     *   <li>MySQL：返回全局共享连接池（与 {@link #getDataSource()} 相同）。</li>
     *   <li>SQLite：按 {@code moduleId} + {@code sqliteFileName} 分配独立连接池，
     *       文件放在本体 {@code data/<moduleId>/} 目录下。</li>
     * </ul>
     *
     * <h4>自建模式（overrideDescriptor != null）</h4>
     * <ul>
     *   <li>根据 overrideDescriptor 创建独立 HikariCP 连接池并按 {@code moduleId} 缓存。</li>
     *   <li>SQLite 文件放在 {@code moduleDataFolder} 目录下（保持旧路径，零迁移）。</li>
     *   <li>MySQL 使用 overrideDescriptor 中的连接参数。</li>
     * </ul>
     *
     * @param moduleId           模块 ID（用于缓存键和 SQLite 文件名）
     * @param sqliteFileName     共享模式下 SQLite 文件名（如 {@code "mail.db"}），
     *                           为 {@code null} 或空时使用 {@code <moduleId>.db}；
     *                           自建模式下此参数被忽略（使用 overrideDescriptor.sqliteFileName()）
     * @param moduleDataFolder   模块数据目录（自建 SQLite 模式下文件存放于此）；
     *                           共享模式下此参数被忽略
     * @param overrideDescriptor 模块自建存储描述符，{@code null} 表示走共享模式
     * @return 该模块对应的数据源（永不为 {@code null}）
     * @throws SQLException 数据源不可用或创建失败
     * @since 1.5.0
     */
    DataSource resolveModuleDataSource(String moduleId, String sqliteFileName,
                                       File moduleDataFolder,
                                       StorageDescriptor overrideDescriptor) throws SQLException;
}
