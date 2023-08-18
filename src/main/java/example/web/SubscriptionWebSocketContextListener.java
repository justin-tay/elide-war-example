package example.web;

import java.time.Duration;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettings;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.datastores.jms.websocket.SubscriptionWebSocketConfigurator;
import com.yahoo.elide.graphql.subscriptions.hooks.SubscriptionScanner;
import com.yahoo.elide.graphql.subscriptions.websocket.SubscriptionWebSocket;
import com.yahoo.elide.graphql.subscriptions.websocket.SubscriptionWebSocket.UserFactory;

import example.config.properties.ActiveMQProperties;
import example.config.properties.GraphQLControllerProperties;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * {@link ServletContextLister} for configuring the subscription web socket.
 */
@WebListener
public class SubscriptionWebSocketContextListener implements ServletContextListener {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionWebSocketContextListener.class);

    private EmbeddedActiveMQ embeddedActiveMq = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Elide elide = beanManager.createInstance().select(Elide.class, new NamedAnnotation("elide")).get();
        GraphQLControllerProperties graphqlControllerProperties = beanManager.createInstance()
                .select(GraphQLControllerProperties.class)
                .get();
        ActiveMQProperties activeMqProperties = beanManager.createInstance().select(ActiveMQProperties.class).get();

        if (!graphqlControllerProperties.enabled() || !graphqlControllerProperties.subscription().enabled()) {
            return;
        }

        startEmbeddedActiveMq(activeMqProperties);

        // Register the subscription websocket
        ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                .getAttribute("jakarta.websocket.server.ServerContainer");

        logger.info("Setting up WebSocket");

        String path = graphqlControllerProperties.subscription().path();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqProperties.brokerUrl());

        ElideSettings settings = elide.getElideSettings();

        SubscriptionScanner scanner = SubscriptionScanner.builder()
                // Things you may want to override...
                .deliveryDelay(Message.DEFAULT_DELIVERY_DELAY)
                .messagePriority(Message.DEFAULT_PRIORITY)
                .timeToLive(Message.DEFAULT_TIME_TO_LIVE)
                .deliveryMode(Message.DEFAULT_DELIVERY_MODE)

                // Things you probably don't care about...
                .scanner(elide.getScanner())
                .dictionary(elide.getElideSettings().getDictionary())
                .connectionFactory(connectionFactory)
                .build();
        scanner.bindLifecycleHooks();

        ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder
                .create(SubscriptionWebSocket.class, path)
                .subprotocols(SubscriptionWebSocket.SUPPORTED_WEBSOCKET_SUBPROTOCOLS)
                .configurator(SubscriptionWebSocketConfigurator.builder()
                        .baseUrl(path)
                        .sendPingOnSubscribe(graphqlControllerProperties.subscription().sendPingOnSubscribe())
                        .connectionTimeout(
                                Duration.ofMillis(graphqlControllerProperties.subscription().connectionTimeout()))
                        .maxSubscriptions(graphqlControllerProperties.subscription().maxSubscriptions())
                        .maxMessageSize(graphqlControllerProperties.subscription().maxMessageSize())
                        .maxIdleTimeout(Duration.ofMillis(graphqlControllerProperties.subscription().idleTimeout()))
                        .connectionFactory(connectionFactory)
                        .userFactory(DEFAULT_USER_FACTORY)
                        .auditLogger(settings.getAuditLogger())
                        .verboseErrors(true)
                        .errorMapper(settings.getErrorMapper())
                        .build())
                .build();
        try {
            serverContainer.addEndpoint(serverEndpointConfig);
        } catch (DeploymentException e) {
            logger.error("Failed to deploy SubscriptionWebSocket", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        stopEmbeddedActiveMq();
    }

    private void startEmbeddedActiveMq(ActiveMQProperties activeMqProperties) {
        // Startup up an embedded active MQ.
        try {
            logger.info("Starting EmbeddedActiveMQ");
            embeddedActiveMq = new EmbeddedActiveMQ();
            Configuration configuration = new ConfigurationImpl();
            configuration.addAcceptorConfiguration("default", activeMqProperties.brokerUrl());
            configuration.setPersistenceEnabled(false);
            configuration.setSecurityEnabled(false);
            configuration.setJournalType(JournalType.NIO);
            embeddedActiveMq.setConfiguration(configuration);
            embeddedActiveMq.start();
        } catch (Exception e) {
            logger.error("Failed to start EmbeddedActiveMQ", e);
            embeddedActiveMq = null;
        }
    }

    private void stopEmbeddedActiveMq() {
        if (embeddedActiveMq != null) {
            logger.info("Stopping EmbeddedActiveMQ");
            try {
                embeddedActiveMq.stop();
            } catch (Exception e) {
                logger.error("Failed to stop EmbeddedActiveMQ", e);
            }
        }
    }

    public static class NamedAnnotation extends AnnotationLiteral<Named> implements Named {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private final String value;

        public NamedAnnotation(final String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public static final UserFactory DEFAULT_USER_FACTORY = session -> new User(session.getUserPrincipal());
}
