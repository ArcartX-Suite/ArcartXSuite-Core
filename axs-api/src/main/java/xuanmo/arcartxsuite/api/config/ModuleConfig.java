package xuanmo.arcartxsuite.api.config;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 模块配置规约，合并原 {@code AbstractAXSModule} 的 8 个配置钩子为单一对象。
 * <p>
 * 子类在 {@code configSpec()} 中返回此对象，基类自动处理：
 * <ul>
 *   <li>配置文件导出（{@link #configFileName} / {@link #messagesFileName}）</li>
 *   <li>配置同步与诊断（{@link #syncPolicy} / {@link #currentVersion} / {@link #versionPath} / {@link #migrationFolder} / {@link #validations}）</li>
 *   <li>附属配置 spec（{@link #additionalSpecs}）</li>
 * </ul>
 *
 * @param configFileName      模块配置文件名（如 "config.yml"），null 表示无独立配置文件
 * @param messagesFileName    模块消息文件名（如 "messages.yml"），null 表示不使用外部化消息
 * @param syncPolicy          主配置同步策略，默认 {@link SyncPolicy#strict()}
 * @param currentVersion      内置配置版本号，默认 1
 * @param versionPath         版本号在 YAML 中的路径，默认 "config-version"
 * @param migrationFolder     模块 jar 内迁移目录，默认 "migrations"，空字符串表示无迁移
 * @param validations         主配置校验规则，默认空列表
 * @param additionalSpecs     附属配置 spec（如 chat/channels/*.yml），默认空列表
 * @since 1.5.0
 */
public record ModuleConfig(
    @Nullable String configFileName,
    @Nullable String messagesFileName,
    @NotNull SyncPolicy syncPolicy,
    int currentVersion,
    @NotNull String versionPath,
    @NotNull String migrationFolder,
    @NotNull List<ValidationRule> validations,
    @NotNull List<ModuleConfigSpec> additionalSpecs
) {

    /** 默认配置：无配置文件、无消息文件、strict 同步、版本 1 */
    public static final ModuleConfig DEFAULT = builder().build();

    /** 无配置文件的默认配置（模块不使用配置文件时用） */
    public static final ModuleConfig NONE = builder().build();

    public ModuleConfig {
        syncPolicy = syncPolicy != null ? syncPolicy : SyncPolicy.strict();
        if (currentVersion < 0) {
            throw new IllegalArgumentException("currentVersion 必须 >= 0: " + currentVersion);
        }
        versionPath = (versionPath == null || versionPath.isBlank()) ? "config-version" : versionPath;
        migrationFolder = migrationFolder != null ? migrationFolder : "migrations";
        validations = validations != null ? List.copyOf(validations) : List.of();
        additionalSpecs = additionalSpecs != null ? List.copyOf(additionalSpecs) : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String configFileName;
        private String messagesFileName;
        private SyncPolicy syncPolicy = SyncPolicy.strict();
        private int currentVersion = 1;
        private String versionPath = "config-version";
        private String migrationFolder = "migrations";
        private List<ValidationRule> validations = List.of();
        private List<ModuleConfigSpec> additionalSpecs = List.of();

        private Builder() {}

        public Builder configFileName(String configFileName) {
            this.configFileName = configFileName;
            return this;
        }

        public Builder messagesFileName(String messagesFileName) {
            this.messagesFileName = messagesFileName;
            return this;
        }

        public Builder syncPolicy(SyncPolicy syncPolicy) {
            this.syncPolicy = syncPolicy;
            return this;
        }

        public Builder currentVersion(int currentVersion) {
            this.currentVersion = currentVersion;
            return this;
        }

        public Builder versionPath(String versionPath) {
            this.versionPath = versionPath;
            return this;
        }

        public Builder migrationFolder(String migrationFolder) {
            this.migrationFolder = migrationFolder;
            return this;
        }

        public Builder validations(List<ValidationRule> validations) {
            this.validations = validations;
            return this;
        }

        public Builder additionalSpecs(List<ModuleConfigSpec> additionalSpecs) {
            this.additionalSpecs = additionalSpecs;
            return this;
        }

        public ModuleConfig build() {
            return new ModuleConfig(
                configFileName, messagesFileName, syncPolicy,
                currentVersion, versionPath, migrationFolder,
                validations, additionalSpecs
            );
        }
    }
}
