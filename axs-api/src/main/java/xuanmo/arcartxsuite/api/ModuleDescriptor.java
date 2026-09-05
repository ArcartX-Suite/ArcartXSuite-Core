package xuanmo.arcartxsuite.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 模块元数据描述符。
 * <p>
 * 通常从模块 Jar 内的 {@code module.yml} 解析得到，
 * 也可由模块主类直接构造返回。
 *
 * @since 1.0.0
 */
public record ModuleDescriptor(
    String id,
    String name,
    String version,
    String mainClass,
    List<String> depends,
    List<String> softDepends,
    List<String> externalDepends,
    List<String> externalSoftDepends,
    String signature
) {

    /**
     * 紧凑构造方法：校验必填字段并应用默认值。
     */
    public ModuleDescriptor {
        Objects.requireNonNull(id, "id");
        if (name == null || name.isBlank()) {
            name = id;
        }
        if (version == null || version.isBlank()) {
            version = "1.0.0";
        }
        if (mainClass == null) {
            mainClass = "";
        }
        depends = depends != null ? List.copyOf(depends) : List.of();
        softDepends = softDepends != null ? List.copyOf(softDepends) : List.of();
        externalDepends = externalDepends != null ? List.copyOf(externalDepends) : List.of();
        externalSoftDepends = externalSoftDepends != null ? List.copyOf(externalSoftDepends) : List.of();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private String name;
        private String version;
        private String mainClass;
        private List<String> depends = Collections.emptyList();
        private List<String> softDepends = Collections.emptyList();
        private List<String> externalDepends = Collections.emptyList();
        private List<String> externalSoftDepends = Collections.emptyList();
        private String signature;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Builder depends(List<String> depends) {
            this.depends = depends != null ? depends : Collections.emptyList();
            return this;
        }

        public Builder softDepends(List<String> softDepends) {
            this.softDepends = softDepends != null ? softDepends : Collections.emptyList();
            return this;
        }

        public Builder externalDepends(List<String> externalDepends) {
            this.externalDepends = externalDepends != null ? externalDepends : Collections.emptyList();
            return this;
        }

        public Builder externalSoftDepends(List<String> externalSoftDepends) {
            this.externalSoftDepends = externalSoftDepends != null ? externalSoftDepends : Collections.emptyList();
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public ModuleDescriptor build() {
            return new ModuleDescriptor(
                id, name, version, mainClass,
                depends, softDepends, externalDepends, externalSoftDepends,
                signature
            );
        }
    }
}
