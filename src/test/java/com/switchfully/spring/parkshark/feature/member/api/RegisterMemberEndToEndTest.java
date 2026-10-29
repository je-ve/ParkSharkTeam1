package com.switchfully.spring.parkshark.feature.member.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterMemberEndToEndTest {

    @LocalServerPort
    private int port;

    @Test
    void givenAValidMember_whenRegisteringWithoutAuthentication_thenReturns201() {
        RequestSpecification request = RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(validMember(uniqueEmail()));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.CREATED.value());
    }

    private static String uniqueEmail() {
        return "john-" + UUID.randomUUID() + "@doe.be";
    }

    private static String validMember(String email) {
        return """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "address": {
                    "streetName": "Meir",
                    "streetNumber": "10a",
                    "postalCode": "2000",
                    "city": "Antwerp"
                  },
                  "telephone": "+32 470 12 34 56",
                  "licensePlate": {
                    "number": "1-ABC-123",
                    "issuingCountry": "BE"
                  },
                  "membershipLevel": "GOLD",
                  "email": "%s",
                  "password": "secret123"
                }
                """.formatted(email);
    }
}
