package xuanmo.arcartxsuite.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.Nullable;

/**
 * 资源上下文：提供模块 Jar 资源读取、导出、UI 绑定等资源管理能力。
 *
 * @since 1.5.0
 */
public interface ResourceContext {

    InputStream openResource(String resourcePath, ClassLoader loader);

    void exportResource(String resourcePath, File target, boolean overwrite);

    UiBinding prepareUiBinding(String moduleName, String configuredUiId, boolean registerOnEnable, File uiFile);

    boolean hasPlugin(String pluginName);

    File exportUiResource(String resourcePath, String relativeUiPath, boolean overwrite, ClassLoader loader) throws IOException;

    File exportConfigResource(String resourcePath, String targetRelativePath, boolean overwrite, ClassLoader loader);

    void unregisterUi(@Nullable String registeredUiId);
}
