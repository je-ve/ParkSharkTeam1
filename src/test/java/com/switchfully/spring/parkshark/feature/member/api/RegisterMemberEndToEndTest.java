package com.switchfully.spring.parkshark.feature.member.api;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterMemberEndToEndTest {

    @LocalServerPort
    private int port;

    @Test
    void givenAValidMember_whenRegisteringWithoutAuthentification_thenReturns201() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
  {
    "firstName": "John",
    "lastName": "Doe",
    "address": {
      "street": "123 Main Street",
      "city": "Antwerp",
      "postalCode": "2000",
      "country": "Belgium"
    },
    "telephone": "+32470000000",
    "licensePlate": {
      "number": "1-ABC-123",
      "countryCode": "BE"
    },
    "registrationDate": "2026-10-29",
    "membershipLevel": "GOLD",
    "credentials": {
      "username": "johndoe88",
      "password": "SecurePassword123!"
    }
  }
  """)
                .when()
                .post("/members")
                .then()
                .statusCode(201);
    }



}
