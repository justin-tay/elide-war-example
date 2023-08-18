package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for Api Docs.
 */
@ConfigMapping(prefix = "elide.api-docs")
public interface ApiDocsControllerProperties {
    @WithDefault("false")
    boolean enabled();

    @WithDefault("/")
    String path();

    @WithDefault("3.0")
    String version();
}
