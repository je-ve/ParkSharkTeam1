package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.common.MutableClock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterMemberEndToEndTest {

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneId.of("Europe/Brussels")));
        }
    }

    @LocalServerPort
    private int port;

    @Test
    void givenAValidMember_whenRegisteringWithoutAuthentication_thenReturns201WithTheCreatedMember() {
        String email = uniqueEmail();
        RequestSpecification request = jsonRequest().body(validMember(email, "\"GOLD\""));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("address.city", equalTo("Antwerp"))
                .body("licensePlate.number", equalTo("1-ABC-123"))
                .body("registrationDate", equalTo("2026-01-01"))
                .body("membershipLevel", equalTo("GOLD"))
                .body("email", equalTo(email));
    }

    @Test
    void givenNoMembershipLevel_whenRegistering_thenTheMemberIsBronze() {
        RequestSpecification request = jsonRequest().body(validMember(uniqueEmail(), "null"));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("membershipLevel", equalTo("BRONZE"));
    }

    @Test
    void givenAnEmailWithCapitals_whenRegistering_thenTheEmailIsStoredInLowerCase() {
        String email = uniqueEmail();
        RequestSpecification request = jsonRequest().body(validMember(email.toUpperCase(), "\"SILVER\""));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("email", equalTo(email));
    }

    @Test
    void givenAnEmailAlreadyInUse_whenRegistering_thenReturns409() {
        String email = uniqueEmail();
        jsonRequest().body(validMember(email, "\"GOLD\"")).post("/members").then().statusCode(HttpStatus.CREATED.value());
        RequestSpecification request = jsonRequest().body(validMember(email, "\"BRONZE\""));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("detail", equalTo("Email " + email + " is already in use"));
    }

    @Test
    void givenMissingAndInvalidFields_whenRegistering_thenReturns400ListingEveryInvalidField() {
        RequestSpecification request = jsonRequest().body("""
                {
                  "firstName": "",
                  "address": {
                    "streetName": "Meir",
                    "streetNumber": "10a",
                    "postalCode": "2000"
                  },
                  "telephone": "+32 470 12 34 56",
                  "licensePlate": {
                    "issuingCountry": "BE"
                  },
                  "email": "not-an-email",
                  "password": "short"
                }
                """);

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("detail", equalTo("Validation failed"))
                .body("errors.firstName", hasItem("First name is required"))
                .body("errors.lastName", hasItem("Last name is required"))
                .body("errors.'address.city'", hasItem("City is required"))
                .body("errors.'licensePlate.number'", hasItem("License plate number is required"))
                .body("errors.email", hasItem("Email must be valid"))
                .body("errors.password", hasItem("Password must be between 8 and 72 characters"));
    }

    @Test
    void givenMissingAddressAndLicensePlate_whenRegistering_thenReturns400() {
        RequestSpecification request = jsonRequest().body("""
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "telephone": "+32 470 12 34 56",
                  "email": "%s",
                  "password": "secret123"
                }
                """.formatted(uniqueEmail()));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.address", hasItem("Address is required"))
                .body("errors.licensePlate", hasItem("License plate is required"));
    }

    @Test
    void givenAnUnknownMembershipLevel_whenRegistering_thenReturns400NamingTheField() {
        RequestSpecification request = jsonRequest().body(validMember(uniqueEmail(), "\"PLATINUM\""));

        Response response = request.post("/members");

        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("detail", equalTo("Invalid value for field 'membershipLevel'"));
    }

    private RequestSpecification jsonRequest() {
        return RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON);
    }

    private static String uniqueEmail() {
        return "john-" + UUID.randomUUID() + "@doe.be";
    }

    private static String validMember(String email, String membershipLevel) {
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
                  "membershipLevel": %s,
                  "email": "%s",
                  "password": "secret123"
                }
                """.formatted(membershipLevel, email);
    }
}
