package example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

/**
 * Tests the management endpoints.
 */
public class ManagementIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    void health() {
        given().when()
                .get("/app/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("checks[0].name", equalTo("db"))
                .body("checks[0].status", equalTo("UP"));
    }
}
