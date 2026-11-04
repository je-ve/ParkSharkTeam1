package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.security.service.CredentialsService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListMembersEndToEndTest {

    private static final String PASSWORD = "secret123";

    @LocalServerPort
    private int port;

    @Autowired
    private CredentialsService credentialsService;

    private String managerEmail;

    @BeforeEach
    void createManager() {
        managerEmail = uniqueEmail("manager");
        credentialsService.create(managerEmail, PASSWORD, Role.MANAGER);
    }

    @Test
    void givenAManager_whenGettingAllMembers_thenReturns200WithOnlyTheOverviewFields() {
        String email = uniqueEmail("john");
        String id = registerMember(email);
        RequestSpecification request = asUser(managerEmail);

        Response response = request.get("/members");

        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", hasItem(id));
        String member = "find { it.email == '%s' }".formatted(email);
        response.then()
                .body(member + ".keySet()", containsInAnyOrder(
                        "id", "firstName", "lastName", "licensePlateNumber", "telephone", "email", "registrationDate"))
                .body(member + ".firstName", equalTo("John"))
                .body(member + ".lastName", equalTo("Doe"))
                .body(member + ".licensePlateNumber", equalTo("1-ABC-123"))
                .body(member + ".telephone", equalTo("+32 470 12 34 56"));
    }

    @Test
    void givenAManager_whenGettingAllMembers_thenNoPasswordOrHashIsReturned() {
        registerMember(uniqueEmail("john"));
        RequestSpecification request = asUser(managerEmail);

        Response response = request.get("/members");

        response.then().statusCode(HttpStatus.OK.value());
        assertThat(response.asString()).doesNotContainIgnoringCase("password").doesNotContainIgnoringCase("hash");
    }

    @Test
    void givenAManager_whenGettingOneMember_thenReturns200WithAllTheirData() {
        String email = uniqueEmail("john");
        String id = registerMember(email);
        RequestSpecification request = asUser(managerEmail);

        Response response = request.get("/members/{id}", id);

        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(id))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("address.streetName", equalTo("Meir"))
                .body("address.city", equalTo("Antwerp"))
                .body("telephone", equalTo("+32 470 12 34 56"))
                .body("licensePlate.number", equalTo("1-ABC-123"))
                .body("licensePlate.issuingCountry", equalTo("BE"))
                .body("membershipLevel", equalTo("GOLD"))
                .body("email", equalTo(email));
        assertThat(response.asString()).doesNotContainIgnoringCase("password").doesNotContainIgnoringCase("hash");
    }

    @Test
    void givenAnUnknownId_whenGettingOneMember_thenReturns404() {
        UUID unknownId = UUID.randomUUID();
        RequestSpecification request = asUser(managerEmail);

        Response response = request.get("/members/{id}", unknownId);

        response.then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("detail", equalTo("Member with id " + unknownId + " was not found"));
    }

    @Test
    void givenAnIdThatIsNotAUuid_whenGettingOneMember_thenReturns400() {
        RequestSpecification request = asUser(managerEmail);

        Response response = request.get("/members/not-a-uuid");

        response.then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenAMember_whenGettingAllMembers_thenReturns403() {
        String memberEmail = uniqueEmail("member");
        registerMember(memberEmail);
        RequestSpecification request = asUser(memberEmail);

        Response response = request.get("/members");

        response.then().statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void givenAMember_whenGettingOneMember_thenReturns403() {
        String memberEmail = uniqueEmail("member");
        String id = registerMember(memberEmail);
        RequestSpecification request = asUser(memberEmail);

        Response response = request.get("/members/{id}", id);

        response.then().statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void givenNoCredentials_whenGettingAllMembers_thenReturns401() {
        RequestSpecification request = RestAssured.given().port(port);

        Response response = request.get("/members");

        response.then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void givenAWrongPassword_whenGettingAllMembers_thenReturns401() {
        RequestSpecification request = RestAssured.given().port(port)
                .auth().preemptive().basic(managerEmail, "wrong-password");

        Response response = request.get("/members");

        response.then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void givenNoCredentials_whenGettingOneMember_thenReturns401() {
        RequestSpecification request = RestAssured.given().port(port);

        Response response = request.get("/members/{id}", UUID.randomUUID());

        response.then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private RequestSpecification asUser(String email) {
        return RestAssured.given()
                .port(port)
                .auth().preemptive().basic(email, PASSWORD);
    }

    private String registerMember(String email) {
        return RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("""
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
                          "password": "%s"
                        }
                        """.formatted(email, PASSWORD))
                .post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@doe.be";
    }
}
