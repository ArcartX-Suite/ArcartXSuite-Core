package xuanmo.arcartxsuite.api;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * 模块 UI 资源规约，合并原 {@code AbstractAXSModule} 的 2 个 UI 钩子为单一对象。
 *
 * @param resourceMappings  模块 Jar 内资源路径 → 宿主数据目录相对输出路径
 * @param overwrite         是否覆盖已有 UI 文件（默认 false，保留用户自定义）
 * @since 1.5.0
 */
public record ModuleUiSpec(
    @NotNull Map<String, String> resourceMappings,
    boolean overwrite
) {

    /** 无 UI 资源 */
    public static final ModuleUiSpec NONE = new ModuleUiSpec(Map.of(), false);

    public ModuleUiSpec {
        resourceMappings = resourceMappings != null ? Map.copyOf(resourceMappings) : Map.of();
    }

    public static ModuleUiSpec of(@NotNull Map<String, String> mappings) {
        return new ModuleUiSpec(mappings, false);
    }

    public static ModuleUiSpec of(@NotNull Map<String, String> mappings, boolean overwrite) {
        return new ModuleUiSpec(mappings, overwrite);
    }
}
