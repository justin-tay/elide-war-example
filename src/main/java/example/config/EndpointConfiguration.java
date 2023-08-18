package example.config;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.elide.graphql.GraphQLEndpoint;
import com.yahoo.elide.jsonapi.resources.JsonApiEndpoint;
import com.yahoo.elide.swagger.resources.ApiDocsEndpoint;

import example.config.properties.ApiDocsControllerProperties;
import example.config.properties.GraphQLControllerProperties;
import example.config.properties.JsonApiControllerProperties;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;

/**
 * Configuration for Endpoints.
 * <p>
 * Dynamic registration of JAX-RS resources.
 * <p>
 * As this registers on the context root, if static resources need to be served,
 * Jersey needs to be configured as a filter instead of a servlet in web.xml.
 * The JerseyServletContainerInitializer does not have the option to register as
 * a filter and will always register as a servlet.
 */
@ApplicationPath("/")
public class EndpointConfiguration extends ResourceConfig {
    private Logger logger = LoggerFactory.getLogger(EndpointConfiguration.class);

    @Inject
    public EndpointConfiguration(JsonApiControllerProperties jsonApiControllerProperties,
            ApiDocsControllerProperties apiDocsControllerProperties,
            GraphQLControllerProperties graphqlControllerProperties) {
        Set<Resource> resources = new HashSet<>();
        if (jsonApiControllerProperties.enabled()) {
            logger.info("Registering JSON-API endpoint at [{}]", jsonApiControllerProperties.path());
            resources.add(Resource.builder(JsonApiEndpoint.class).path(jsonApiControllerProperties.path()).build());
        }
        if (apiDocsControllerProperties.enabled()) {
            logger.info("Registering OpenAPI endpoint at [{}]", apiDocsControllerProperties.path());
            resources.add(Resource.builder(ApiDocsEndpoint.class).path(apiDocsControllerProperties.path()).build());
        }
        if (graphqlControllerProperties.enabled()) {
            logger.info("Registering GraphQL endpoint at [{}]", graphqlControllerProperties.path());
            resources.add(Resource.builder(GraphQLEndpoint.class).path(graphqlControllerProperties.path()).build());
        }
        registerResources(resources);
    }
}
