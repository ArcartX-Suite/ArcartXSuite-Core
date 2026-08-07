package xuanmo.arcartxsuite.api.util;

/**
 * 服务端平台检测工具。
 * <p>
 * 通过类是否存在检测 Paper / Folia / Spigot，零反射开销，类加载时一次性判定。
 * 参考 Asteroid 的 PaperCompat 设计，但不依赖外部库。
 */
public final class PlatformCompat {

    private static final boolean IS_PAPER;
    private static final boolean IS_FOLIA;
    private static final boolean IS_SPIGOT;

    static {
        IS_PAPER = classExists("io.papermc.paper.configuration.Configuration")
                || classExists("com.destroystokyo.paper.PaperConfig");
        IS_FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        IS_SPIGOT = !IS_PAPER && !IS_FOLIA;
    }

    private PlatformCompat() {}

    public static boolean isPaper() { return IS_PAPER; }

    public static boolean isFolia() { return IS_FOLIA; }

    public static boolean isSpigot() { return IS_SPIGOT; }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
