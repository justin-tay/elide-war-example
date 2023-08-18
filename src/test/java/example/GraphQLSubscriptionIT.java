package example;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.yahoo.elide.datastores.jms.websocket.SubscriptionWebSocketTestClient;
import com.yahoo.elide.graphql.parser.GraphQLQuery;

import graphql.ExecutionResult;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

/**
 * Tests the GraphQL Subscription endpoint.
 */
@TestMethodOrder(OrderAnnotation.class)
public class GraphQLSubscriptionIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/graphql/api";
    }

    @Test
    @Order(1)
    void subscribe() throws IOException, DeploymentException, URISyntaxException, InterruptedException {
        int port = 8080;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        String subscription = """
                subscription {
                  group(topic: ADDED) {
                    groupId
                    description
                  }
                }
                """;

        SubscriptionWebSocketTestClient client = new SubscriptionWebSocketTestClient(1, List.of(subscription));

        try (Session session = container.connectToServer(client, new URI("ws://localhost:" + port + "/subscription"))) {
            client.waitOnSubscribe(10);
            upsert();
            List<ExecutionResult> results = client.waitOnClose(10);
            delete();
            assertThat(results).hasSize(1);
        }
    }

    void upsert() {
        String query = """
                mutation {
                  group(op: UPSERT, data: {groupId: "com.graphql-java", description: "GraphQL Java"}) {
                    edges {
                      node {
                        groupId
                        description
                      }
                    }
                  }
                }""";
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(GraphQLQuery.builder().query(query).build())
                .when()
                .post("/")
                .then()
                .statusCode(200)
                .body("data.group.edges[0].node.groupId", equalTo("com.graphql-java"))
                .body("data.group.edges[0].node.description", equalTo("GraphQL Java"));
    }

    void delete() {
        String query = """
                mutation {
                  group (op: DELETE, ids: ["com.graphql-java"]) {
                    edges {
                      node {
                        groupId
                      }
                    }
                  }
                }""";
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(GraphQLQuery.builder().query(query).build())
                .when()
                .post("/")
                .then()
                .statusCode(200);

    }
}
