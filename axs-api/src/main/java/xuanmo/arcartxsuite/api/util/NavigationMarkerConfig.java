package xuanmo.arcartxsuite.api.util;

/**
 * 导航标记配置接口。
 * <p>
 * 各模块自行实现此接口以提供标记配置参数。
 *
 * @since 1.5.0
 */
public interface NavigationMarkerConfig {

    boolean enabled();

    String modelId();

    double scale();

    String defaultState();

    String animation();

    double yOffset();

    double pathInterval();

    int pathMaxMarkers();

    int pathUpdateTicks();

    double pathMaxDistance();

    int pathMaxIterations();
}
