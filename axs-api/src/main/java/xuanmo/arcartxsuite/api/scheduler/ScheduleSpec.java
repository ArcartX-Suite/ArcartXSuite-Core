package xuanmo.arcartxsuite.api.scheduler;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.api.bridge.ApiStability;

/**
 * 预置调度规格（不使用 cron 字符串，用枚举模式表达触发时机）。
 * <p>
 * 支持 4 种模式：
 * <ul>
 *   <li>{@link Mode#HOURLY} — 每个自然整点触发（如 1:00、2:00…）</li>
 *   <li>{@link Mode#DAILY} — 每天指定时:分触发</li>
 *   <li>{@link Mode#WEEKLY} — 每周指定星期几的指定时:分触发</li>
 *   <li>{@link Mode#INTERVAL} — 固定间隔触发（分钟/秒/tick 三选一）</li>
 * </ul>
 *
 * <h3>从 yml 配置节加载</h3>
 * <pre>{@code
 * schedule:
 *   mode: hourly               # hourly | daily | weekly | interval
 *   time: "08:00"              # daily/weekly 时用，格式 "HH:mm"
 *   day: monday                # weekly 时用（monday~sunday，大小写不敏感）
 *   interval-minutes: 15       # mode=interval 时用（与 interval-seconds / interval-ticks 三选一）
 * }</pre>
 *
 * @since 1.6.0
 */
@ApiStability.Stable
public final class ScheduleSpec {

    public enum Mode {
        /** 每个自然整点触发 */
        HOURLY,
        /** 每天指定时:分触发 */
        DAILY,
        /** 每周指定星期几的指定时:分触发 */
        WEEKLY,
        /** 固定间隔触发 */
        INTERVAL
    }

    private final Mode mode;
    private final int hour;
    private final int minute;
    private final DayOfWeek dayOfWeek;
    private final long intervalTicks;

    private ScheduleSpec(Mode mode, int hour, int minute, DayOfWeek dayOfWeek, long intervalTicks) {
        this.mode = mode;
        this.hour = hour;
        this.minute = minute;
        this.dayOfWeek = dayOfWeek;
        this.intervalTicks = intervalTicks;
    }

    // ── 工厂方法 ──────────────────────────────────────────

    /** 每个自然整点触发（如 1:00、2:00…）。 */
    public static ScheduleSpec hourly() {
        return new ScheduleSpec(Mode.HOURLY, 0, 0, null, 0);
    }

    /** 每天指定 hour:minute 触发（24 小时制，hour 0-23，minute 0-59）。 */
    public static ScheduleSpec dailyAt(int hour, int minute) {
        validateTime(hour, minute);
        return new ScheduleSpec(Mode.DAILY, hour, minute, null, 0);
    }

    /** 每周指定星期几的 hour:minute 触发。 */
    public static ScheduleSpec weeklyAt(@NotNull DayOfWeek day, int hour, int minute) {
        Objects.requireNonNull(day, "day");
        validateTime(hour, minute);
        return new ScheduleSpec(Mode.WEEKLY, hour, minute, day, 0);
    }

    /** 每 N 分钟触发一次（等价于 N*60*20 tick）。 */
    public static ScheduleSpec everyMinutes(int minutes) {
        if (minutes <= 0) throw new IllegalArgumentException("interval minutes must be > 0: " + minutes);
        return new ScheduleSpec(Mode.INTERVAL, 0, 0, null, minutes * 60L * 20L);
    }

    /** 每 N 秒触发一次（等价于 N*20 tick）。 */
    public static ScheduleSpec everySeconds(int seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("interval seconds must be > 0: " + seconds);
        return new ScheduleSpec(Mode.INTERVAL, 0, 0, null, seconds * 20L);
    }

    /** 每 N tick 触发一次。 */
    public static ScheduleSpec everyTicks(long ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("interval ticks must be > 0: " + ticks);
        return new ScheduleSpec(Mode.INTERVAL, 0, 0, null, ticks);
    }

    /**
     * 从 yml 配置节解析调度规格。
     * <p>
     * 配置格式见类注释。
     *
     * @param section 配置节，null 时返回 {@link #hourly()}
     * @return 调度规格
     */
    public static @NotNull ScheduleSpec load(@Nullable ConfigurationSection section) {
        if (section == null) return hourly();
        String modeStr = section.getString("mode", "hourly").trim().toLowerCase(Locale.ROOT);
        switch (modeStr) {
            case "hourly":
                return hourly();
            case "daily": {
                int[] hm = parseTime(section.getString("time", "00:00"));
                return dailyAt(hm[0], hm[1]);
            }
            case "weekly": {
                int[] hm = parseTime(section.getString("time", "00:00"));
                DayOfWeek day = parseDay(section.getString("day", "monday"));
                return weeklyAt(day, hm[0], hm[1]);
            }
            case "interval": {
                if (section.contains("interval-ticks")) {
                    return everyTicks(section.getLong("interval-ticks", 1200L));
                }
                if (section.contains("interval-seconds")) {
                    return everySeconds(section.getInt("interval-seconds", 60));
                }
                return everyMinutes(section.getInt("interval-minutes", 1));
            }
            default:
                throw new IllegalArgumentException("未知的 schedule mode: " + modeStr
                    + "（支持: hourly / daily / weekly / interval）");
        }
    }

    // ── 计算下次执行时间 ──────────────────────────────────

    /**
     * 计算从 {@code from} 之后下一次应触发的时间（基于服务器时区）。
     * <p>
     * INTERVAL 模式返回 null（调用方用固定间隔调度，不需要计算下次时间）。
     *
     * @param from 基准时间（通常为当前时间）
     * @return 下次触发时间，INTERVAL 模式返回 null
     */
    public @Nullable ZonedDateTime nextExecutionAfter(@NotNull ZonedDateTime from) {
        Objects.requireNonNull(from, "from");
        switch (mode) {
            case HOURLY: {
                // 下一个整点
                ZonedDateTime next = from.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                if (next.equals(from.withMinute(0).withSecond(0).withNano(0))) {
                    // from 正好是整点，下一个整点是 from + 1h
                }
                return next;
            }
            case DAILY: {
                ZonedDateTime today = from.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
                return today.isAfter(from) ? today : today.plusDays(1);
            }
            case WEEKLY: {
                ZonedDateTime today = from.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
                ZonedDateTime candidate = today;
                for (int i = 0; i < 7; i++) {
                    if (candidate.getDayOfWeek() == dayOfWeek && candidate.isAfter(from)) {
                        return candidate;
                    }
                    candidate = candidate.plusDays(1);
                }
                return today.plusDays(7);
            }
            case INTERVAL:
            default:
                return null;
        }
    }

    // ── getter ────────────────────────────────────────────

    public Mode mode() { return mode; }
    public int hour() { return hour; }
    public int minute() { return minute; }
    public @Nullable DayOfWeek dayOfWeek() { return dayOfWeek; }
    public long intervalTicks() { return intervalTicks; }

    // ── 内部工具 ──────────────────────────────────────────

    private static void validateTime(int hour, int minute) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("hour must be 0-23: " + hour);
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("minute must be 0-59: " + minute);
    }

    private static int[] parseTime(String time) {
        if (time == null || time.isBlank()) return new int[]{0, 0};
        String[] parts = time.trim().split(":");
        if (parts.length != 2) throw new IllegalArgumentException("time 格式应为 HH:mm: " + time);
        try {
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            validateTime(h, m);
            return new int[]{h, m};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("time 格式应为 HH:mm: " + time, e);
        }
    }

    private static DayOfWeek parseDay(String day) {
        if (day == null || day.isBlank()) return DayOfWeek.MONDAY;
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("day 应为 monday~sunday: " + day, e);
        }
    }

    @Override
    public String toString() {
        switch (mode) {
            case HOURLY: return "ScheduleSpec[hourly]";
            case DAILY: return "ScheduleSpec[daily at " + hour + ":" + minute + "]";
            case WEEKLY: return "ScheduleSpec[weekly " + dayOfWeek + " at " + hour + ":" + minute + "]";
            case INTERVAL: return "ScheduleSpec[interval " + intervalTicks + " ticks]";
            default: return "ScheduleSpec[" + mode + "]";
        }
    }
}
