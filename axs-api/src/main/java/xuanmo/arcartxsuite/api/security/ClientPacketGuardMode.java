package xuanmo.arcartxsuite.api.security;

import java.util.Locale;

/**
 * 客户端包频率限制器的处置模式。
 * <ul>
 *   <li>{@link #SILENT} — 静默丢弃超频包</li>
 *   <li>{@link #NOTIFY} — 丢弃并提示玩家</li>
 *   <li>{@link #PUNISH} — 丢弃并执行惩罚命令</li>
 * </ul>
 */
public enum ClientPacketGuardMode {
    SILENT("silent"),
    NOTIFY("notify"),
    PUNISH("punish");

    private final String configValue;

    ClientPacketGuardMode(String configValue) {
        this.configValue = configValue;
    }

    /** 返回用于配置文件的小写标识。 */
    public String configValue() {
        return configValue;
    }

    /**
     * 从配置文本解析模式，匹配小写 configValue。
     *
     * @param rawValue 原始文本，null/空白时返回 fallback
     * @param fallback 兜底模式
     * @return 解析得到的模式
     */
    public static ClientPacketGuardMode parse(String rawValue, ClientPacketGuardMode fallback) {
        if (rawValue == null || rawValue.isBlank()) {
            return fallback;
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        for (ClientPacketGuardMode mode : values()) {
            if (mode.configValue.equals(normalized)) {
                return mode;
            }
        }
        return fallback;
    }
}
