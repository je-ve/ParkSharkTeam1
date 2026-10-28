package com.switchfully.spring.parkshark.common.openapi;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiEndToEndTest {

    @LocalServerPort
    private int port;

    private JsonPath apiDocs;

    @BeforeEach
    void readTheApiDocumentation() {
        apiDocs = RestAssured.given().port(port).get("/v3/api-docs").jsonPath();
    }

    @Test
    void givenTheApiDocumentation_whenReadingTheSchemas_thenDescribesTheProblemDetailErrorFormat() {
        String problemDetail = "components.schemas.ProblemDetail.properties";

        Map<String, Object> properties = apiDocs.getMap(problemDetail);

        assertThat(properties).containsKeys("type", "title", "status", "detail", "instance", "errors");
    }

    @Test
    void givenTheApiDocumentation_whenReadingItsDescription_thenExplainsHowToAuthenticateAndTheErrorFormat() {
        String description = apiDocs.getString("info.description");

        assertThat(description).contains("HTTP Basic").contains("ProblemDetail");
    }
}
