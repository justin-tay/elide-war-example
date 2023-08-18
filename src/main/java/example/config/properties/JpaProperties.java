package example.config.properties;

import java.util.Map;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for JPA.
 */
@ConfigMapping(prefix = "elide.jpa")
public interface JpaProperties {
    @WithDefault("false")
    boolean showSql();

    Map<String, String> properties();
}
