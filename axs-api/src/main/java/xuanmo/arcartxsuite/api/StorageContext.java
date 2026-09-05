package xuanmo.arcartxsuite.api;

import org.jetbrains.annotations.NotNull;
import xuanmo.arcartxsuite.api.bridge.ApiStability;
import xuanmo.arcartxsuite.api.storage.StorageManager;

/**
 * 存储上下文：提供宿主统一数据源管理器。
 *
 * @since 1.5.0
 */
public interface StorageContext {

    @ApiStability.Stable
    @NotNull StorageManager storageManager();
}
