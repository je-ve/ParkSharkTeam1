package com.switchfully.spring.parkshark.feature.security;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationEndToEndTest {

    @LocalServerPort
    private int port;

    @ParameterizedTest(name = "Authorization: \"{0}\" is rejected")
    @ValueSource(strings = {"", "Bearer some-token", "Basic !!!not-base64!!!", "Basic bm8tY29sb24="})
    void givenAMissingOrMalformedBasicHeader_whenCallingAProtectedEndpoint_thenReturns401WithABasicChallenge(String authorization) {
        RequestSpecification request = RestAssured.given().port(port).header(HttpHeaders.AUTHORIZATION, authorization);

        Response response = request.get("/protected");

        response.then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Parkshark\"")
                .body("detail", equalTo("Invalid credentials"));
    }

    @Test
    void givenAnUnknownEmail_whenCallingAProtectedEndpoint_thenReturns401InvalidCredentials() {
        RequestSpecification request = RestAssured.given().port(port).auth().preemptive().basic("nobody@parkshark.be", "secret123");

        Response response = request.get("/protected");

        response.then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("detail", equalTo("Invalid credentials"));
    }
}
