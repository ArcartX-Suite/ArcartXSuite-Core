package xuanmo.arcartxsuite.api.storage;

/**
 * 数据库连接描述符，由各模块的配置类构造后传给 {@link AbstractModuleRepository}。
 * <p>
 * 统一 MySQL / SQLite 双方言所需的全部参数。
 */
public record StorageDescriptor(
    boolean isMysql,
    String host,
    int port,
    String database,
    String username,
    String password,
    int poolSize,
    String sqliteFileName,
    String tablePrefix
) {

    /**
     * 快速构造 SQLite 描述符。
     */
    public static StorageDescriptor sqlite(String fileName) {
        return new StorageDescriptor(false, "", 0, "", "", "", 1, fileName, "");
    }

    /**
     * 快速构造 MySQL 描述符。
     */
    public static StorageDescriptor mysql(String host, int port, String database,
                                          String username, String password,
                                          int poolSize, String tablePrefix) {
        return new StorageDescriptor(true, host, port, database, username, password,
            poolSize, "", tablePrefix);
    }

    /**
     * 返回一个仅替换 tablePrefix 的新描述符副本，其余字段不变。
     * <p>
     * 用于共享模式下将本体全局描述符的空 tablePrefix 替换为模块自己的表前缀。
     *
     * @param newTablePrefix 新的表前缀，为 {@code null} 时使用空字符串
     * @return 带新 tablePrefix 的描述符副本
     * @since 1.5.0
     */
    public StorageDescriptor withTablePrefix(String newTablePrefix) {
        return new StorageDescriptor(isMysql, host, port, database, username, password,
            poolSize, sqliteFileName, newTablePrefix != null ? newTablePrefix : "");
    }

    /**
     * 脱敏的 toString，避免 password 通过日志/异常栈泄露。
     */
    @Override
    public String toString() {
        return "StorageDescriptor{isMysql=" + isMysql
            + ", host=" + host
            + ", port=" + port
            + ", database=" + database
            + ", username=" + username
            + ", password=***"
            + ", poolSize=" + poolSize
            + ", sqliteFileName=" + sqliteFileName
            + ", tablePrefix=" + tablePrefix
            + "}";
    }
}
