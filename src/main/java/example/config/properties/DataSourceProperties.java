package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for DataSource.
 */
@ConfigMapping(prefix = "elide.datasource")
public interface DataSourceProperties {
    @WithDefault("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
    String url();

    @WithDefault("sa")
    String username();

    @WithDefault("")
    ConfigValue password();

    @WithDefault("org.h2.Driver")
    String driverClassName();
}
