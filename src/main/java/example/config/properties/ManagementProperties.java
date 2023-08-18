package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for management endpoints.
 */
@ConfigMapping(prefix = "elide.management")
public interface ManagementProperties {
    @WithDefault("true")
    boolean enabled();

    @WithDefault("/app")
    String path();

    Health health();

    interface Health {
        @WithDefault("true")
        boolean enabled();

        @WithDefault("/health")
        String path();
    }
}
