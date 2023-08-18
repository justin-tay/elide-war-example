package example;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.yahoo.elide.jsonapi.JsonApi;

import io.restassured.RestAssured;

/**
 * Tests the JSON-API endpoint.
 */
@TestMethodOrder(OrderAnnotation.class)
public class JsonApiIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/api";
    }

    @Test
    @Order(1)
    void operationsAdd() {
        String body = """
                {
                  "atomic:operations": [
                    {
                      "op": "add",
                      "data": {
                        "type": "group",
                        "id": "io.swagger.core.v3",
                        "attributes": {
                          "description": "Swagger"
                        }
                      }
                    },
                    {
                      "op": "add",
                      "href": "/group/io.swagger.core.v3/products",
                      "data": {
                        "type": "product",
                        "id": "swagger-core",
                        "attributes": {
                          "description": "Swagger Core"
                        }
                      }
                    },
                    {
                      "op": "add",
                      "href": "/group/io.swagger.core.v3/products/swagger-core/versions",
                      "data": {
                        "type": "version",
                        "lid": "dfb0e20c-cea9-4233-9e7d-32ba15596bdf",
                        "attributes": {
                          "version": "2.2.15"
                        }
                      }
                    },
                    {
                      "op": "add",
                      "href": "/group/io.swagger.core.v3/products/swagger-core/versions",
                      "data": {
                        "type": "version",
                        "lid": "b6fce0b4-2034-4146-a6a6-d6e7777165b3",
                        "attributes": {
                          "version": "2.1.13"
                        }
                      }
                    }
                  ]
                }""";
        given().accept(JsonApi.AtomicOperations.MEDIA_TYPE)
                .contentType(JsonApi.AtomicOperations.MEDIA_TYPE)
                .body(body)
                .when()
                .post("/operations")
                .then()
                .statusCode(200)
                .body("'atomic:results'[0].data.type", equalTo("group"))
                .body("'atomic:results'[0].data.id", equalTo("io.swagger.core.v3"))
                .body("'atomic:results'[1].data.type", equalTo("product"))
                .body("'atomic:results'[1].data.id", equalTo("swagger-core"))
                .body("'atomic:results'[2].data.type", equalTo("version"))
                .body("'atomic:results'[2].data.attributes.version", equalTo("2.2.15"))
                .body("'atomic:results'[3].data.type", equalTo("version"))
                .body("'atomic:results'[3].data.attributes.version", equalTo("2.1.13"));
    }

    @Test
    @Order(2)
    void get() {
        given().accept(JsonApi.AtomicOperations.MEDIA_TYPE)
                .contentType(JsonApi.AtomicOperations.MEDIA_TYPE)
                .when()
                .get("/group?include=products,products.versions&filter=id==io.swagger.core.v3")
                .then()
                .statusCode(200)
                .body("data[0].type", equalTo("group"))
                .body("data[0].id", equalTo("io.swagger.core.v3"))
                .body("data[0].attributes.description", equalTo("Swagger"))
                .body("included[0].type", equalTo("product"))
                .body("included[0].id", equalTo("swagger-core"))
                .body("included[1].type", equalTo("version"))
                .body("included[1].attributes.version", equalTo("2.2.15"))
                .body("included[2].type", equalTo("version"))
                .body("included[2].attributes.version", equalTo("2.1.13"));
    }

    @Test
    @Order(3)
    void operationsDelete() {
        String body = """
                {
                  "atomic:operations": [
                    {
                      "op": "remove",
                      "href": "/group/io.swagger.core.v3/products/swagger-core"
                    },
                    {
                      "op": "remove",
                      "ref": {
                        "type": "group",
                        "id": "io.swagger.core.v3"
                      }
                    }
                  ]
                }""";
        String value = given().accept(JsonApi.AtomicOperations.MEDIA_TYPE)
                .contentType(JsonApi.AtomicOperations.MEDIA_TYPE)
                .body(body)
                .when()
                .post("/operations")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        String expected = """
                {"atomic:results":[{"data":null},{"data":null}]}""";
        assertThat(value).isEqualTo(expected);
    }
}
