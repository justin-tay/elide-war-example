package example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

/**
 * Tests that the static resources are present and accessible.
 */
public class StaticResourceIT {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    void index() {
        given().when().get("/").then().statusCode(200).contentType(ContentType.HTML);
    }

    @Test
    void graphiql() {
        given().when().get("/graphiql/").then().statusCode(200).contentType(ContentType.HTML);
        given().when().get("/graphiql/subscription.html").then().statusCode(200).contentType(ContentType.HTML);
    }
    
    @Test
    void swagger() {
        given().when().get("/swagger/").then().statusCode(200).contentType(ContentType.HTML);
    }
}
