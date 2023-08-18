package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for ActiveMQ.
 */
@ConfigMapping(prefix = "elide.activemq")
public interface ActiveMQProperties {
    @WithDefault("vm://0")
    String brokerUrl();
}
