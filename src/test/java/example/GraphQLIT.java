package example;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import com.yahoo.elide.graphql.parser.GraphQLQuery;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

/**
 * Tests the GraphQL endpoint.
 */
@TestMethodOrder(OrderAnnotation.class)
public class GraphQLIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/graphql/api";
    }

    @Test
    @Order(1)
    void upsert() {
        String query = """
                mutation {
                  group(op: UPSERT, data: {groupId: "com.yahoo.elide", description: "Elide"}) {
                    edges {
                      node {
                        groupId
                        description
                        products(
                          op: UPSERT
                          data: {productId: "elide-core", description: "Elide Core", name: "Elide Core"}
                        ) {
                          edges {
                            node {
                              productId
                              description
                              name
                              versions(op: UPSERT, data: {version: "7.0.0"}) {
                                edges {
                                  node {
                                    versionId
                                    version
                                    createdOn
                                  }
                                }
                              }
                            }
                          }
                        }
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
                .body("data.group.edges[0].node.groupId", equalTo("com.yahoo.elide"))
                .body("data.group.edges[0].node.description", equalTo("Elide"));
    }

    @Test
    @Order(1)
    void query() {
        String query = """
                query {
                  group (ids: "com.yahoo.elide") {
                    edges {
                      node {
                        groupId
                        description
                        products {
                          edges {
                            node {
                              productId
                              description
                              name
                              versions {
                                edges {
                                  node {
                                    versionId
                                    version
                                    createdOn
                                  }
                                }
                              }
                            }
                          }
                        }
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
                .body("data.group.edges[0].node.groupId", equalTo("com.yahoo.elide"))
                .body("data.group.edges[0].node.description", equalTo("Elide"));
    }
}
