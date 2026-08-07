package xuanmo.arcartxsuite.api.aubade.storage;

/**
 * 数据仓库接口。
 */
public interface Repository<T extends DataObject> {

  void save(T entity);

  void delete(T entity);
}
