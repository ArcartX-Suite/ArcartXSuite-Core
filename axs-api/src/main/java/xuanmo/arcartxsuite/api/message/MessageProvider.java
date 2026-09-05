package xuanmo.arcartxsuite.api.message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 模块消息提供者：从 messages.yml 加载可自定义消息文本。
 * <p>
 * 使用方式：
 * <pre>{@code
 * MessageProvider msg = new MessageProvider(dataFolder, "messages.yml", getClass().getClassLoader(), logger);
 * msg.load();
 * String text = msg.get("purge.confirm", "10");  // 用 {0} 占位符
 * }</pre>
 */
public final class MessageProvider {

    /** 匹配 {@code &#rrggbb} 十六进制颜色码（1.16+）。 */
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final File file;
    private final String resourcePath;
    private final ClassLoader classLoader;
    private final Logger logger;
    private final Function<String, InputStream> resourceLoader;
    private final Map<String, String> messages = new HashMap<>();

    public MessageProvider(@NotNull File dataFolder, @NotNull String fileName,
                           @NotNull ClassLoader classLoader, @NotNull Logger logger) {
        this(dataFolder, fileName, classLoader, logger, classLoader::getResourceAsStream);
    }

    public MessageProvider(@NotNull File dataFolder, @NotNull String fileName,
                           @NotNull ClassLoader classLoader, @NotNull Logger logger,
                           @NotNull Function<String, InputStream> resourceLoader) {
        this.file = new File(dataFolder, fileName);
        this.resourcePath = fileName;
        this.classLoader = classLoader;
        this.logger = logger;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 翻译颜色码：先处理 {@code &#rrggbb} 十六进制颜色（1.16+），再处理 {@code &} 单字符颜色码。
     *
     * @param input 原始文本
     * @return 翻译后的文本
     */
    @NotNull
    public static String translateColors(@NotNull String input) {
        if (input.indexOf('&') < 0) {
            return input;
        }
        // 先处理 &#rrggbb 十六进制颜色
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer(input.length() + 16);
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(ChatColor.of("#" + matcher.group(1)).toString()));
        }
        matcher.appendTail(sb);
        // 再处理 & 单字符颜色码
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    /**
     * 加载或重载消息文件。先导出默认文件（若不存在），再读取用户自定义版本。
     */
    public void load() {
        messages.clear();
        exportDefaultIfAbsent();
        if (!file.exists()) {
            return;
        }
        loadBundledDefaults();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(true)) {
            if (yaml.isString(key)) {
                messages.put(key, translateColors(yaml.getString(key, "")));
            }
        }
        logger.fine("[Messages] 加载 " + messages.size() + " 条消息 from " + file.getName());
    }

    /**
     * 获取消息，支持 {0} {1} ... 占位符替换。
     *
     * @param key  消息键（YAML 路径，如 "purge.confirm"）
     * @param args 替换参数
     * @return 处理后的消息文本；键不存在时返回原始键名
     */
    @NotNull
    public String get(@NotNull String key, @Nullable Object... args) {
        String template = messages.get(key);
        if (template == null) {
            return key;
        }
        if (args != null && args.length > 0) {
            // 先对 args 中的 { 和 } 做临时转义，避免替换后的内容被后续占位符二次匹配
            for (int i = args.length - 1; i >= 0; i--) {
                String arg = args[i] == null ? "" : args[i].toString().replace("{", "\uFE5B").replace("}", "\uFE5D");
                template = template.replace("{" + i + "}", arg);
            }
            // 还原转义
            template = template.replace("\uFE5B", "{").replace("\uFE5D", "}");
        }
        return template;
    }

    /**
     * 检查消息键是否存在。
     */
    public boolean has(@NotNull String key) {
        return messages.containsKey(key);
    }

    /**
     * 消息总数。
     */
    public int size() {
        return messages.size();
    }

    public static YamlConfiguration loadYamlWithBundledDefaults(
            @NotNull File file,
            @NotNull String resourcePath,
            @NotNull ClassLoader classLoader,
            @NotNull Logger logger) {
        return loadYamlWithBundledDefaults(file, resourcePath, classLoader, logger,
            classLoader::getResourceAsStream);
    }

    public static YamlConfiguration loadYamlWithBundledDefaults(
            @NotNull File file,
            @NotNull String resourcePath,
            @NotNull ClassLoader classLoader,
            @NotNull Logger logger,
            @NotNull Function<String, InputStream> resourceLoader) {
        YamlConfiguration defaults = new YamlConfiguration();
        try (InputStream input = resourceLoader.apply(resourcePath)) {
            if (input != null) {
                defaults.loadFromString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            logger.warning("[Messages] Failed to load bundled defaults: " + e.getMessage());
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : defaults.getKeys(true)) {
            if (defaults.isString(key) && !configuration.contains(key)) {
                configuration.set(key, defaults.getString(key, ""));
            }
        }
        return configuration;
    }

    private void exportDefaultIfAbsent() {
        if (file.exists()) return;
        try (InputStream input = resourceLoader.apply(resourcePath)) {
            if (input != null) {
                file.getParentFile().mkdirs();
                Files.copy(input, file.toPath());
            }
        } catch (IOException e) {
            logger.warning("[Messages] 导出默认消息文件失败: " + e.getMessage());
        }
    }

    private void loadBundledDefaults() {
        try (InputStream input = resourceLoader.apply(resourcePath)) {
            if (input == null) {
                return;
            }
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.loadFromString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            for (String key : defaults.getKeys(true)) {
                if (defaults.isString(key)) {
                    messages.put(key, translateColors(defaults.getString(key, "")));
                }
            }
        } catch (Exception e) {
            logger.warning("[Messages] 加载内置默认消息失败，所有消息将返回键名: " + e.getMessage());
        }
    }
}
