package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for JSON-API.
 */
@ConfigMapping(prefix = "elide.json-api")
public interface JsonApiControllerProperties {
    @WithDefault("false")
    boolean enabled();

    @WithDefault("/")
    String path();

    Links links();

    interface Links {
        @WithDefault("false")
        boolean enabled();
    }
}
