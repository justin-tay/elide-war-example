package example.config.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for GraphQL.
 */
@ConfigMapping(prefix = "elide.graphql")
public interface GraphQLControllerProperties {
    @WithDefault("false")
    boolean enabled();

    @WithDefault("/")
    String path();

    Subscription subscription();

    interface Subscription {
        @WithDefault("false")
        boolean enabled();

        @WithDefault("/")
        String path();

        @WithDefault("false")
        boolean sendPingOnSubscribe();

        @WithDefault("5000")
        long connectionTimeout();

        @WithDefault("300000")
        long idleTimeout();

        @WithDefault("30")
        int maxSubscriptions();

        @WithDefault("10000")
        int maxMessageSize();

        Publishing publishing();

        interface Publishing {
            @WithDefault("false")
            boolean enabled();
        }
    }
}
