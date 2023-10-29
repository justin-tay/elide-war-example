package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for Elide.
 */
@ConfigMapping(prefix = "elide")
public interface ElideProperties {
    @WithDefault("")
    ConfigValue baseUrl();

    @WithDefault("500")
    int defaultPageSize();

    @WithDefault("10000")
    int maxPageSize();

    @WithDefault("false")
    boolean verboseErrors();

    @WithDefault("true")
    boolean stripAuthorizationHeaders();
}
