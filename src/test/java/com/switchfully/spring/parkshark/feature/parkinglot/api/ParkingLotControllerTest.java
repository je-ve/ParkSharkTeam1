package com.switchfully.spring.parkshark.feature.parkinglot.api;

import com.jayway.jsonpath.JsonPath;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingLotControllerTest {

    private static final String ADDRESS = """
            {"streetName":"Meir","streetNumber":"1","postalCode":"2000","city":"Antwerp"}""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DivisionRepository divisionRepository;

    private UUID divisionId;

    @BeforeEach
    void createDivision() {
        divisionId = divisionRepository.save(
                new Division("Division-" + UUID.randomUUID(), "Old company", "Boss")).getId();
    }

    private static String uniqueEmail() {
        return "contact-" + UUID.randomUUID() + "@company.be";
    }

    private static String newContact(String email) {
        return """
                "contactPerson":{"name":"Jane Doe","mobilePhone":"+32 470 12 34 56","email":"%s","address":%s}"""
                .formatted(email, ADDRESS);
    }

    private static String existingContact(Object id) {
        return "\"contactPersonId\":\"%s\"".formatted(id);
    }

    private static String parkingLot(Object divisionId, String contactPart) {
        String contact = contactPart.isEmpty() ? "" : "," + contactPart;
        return """
                {"name":"Meir Underground","category":"UNDERGROUND_BUILDING","capacity":250,\
                "address":%s,"pricePerHour":2.50,"divisionId":"%s"%s}"""
                .formatted(ADDRESS, divisionId, contact);
    }

    private ResultActions postParkingLot(String json) throws Exception {
        return mockMvc.perform(post("/parking-lots")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Nested
    @DisplayName("Testing CREATED")
    @WithMockUser(authorities = "CREATE_PARKING_LOT")
    class TestingCreated {

        @Test
        void givenNewContactPerson_whenPostParkingLot_thenReturns201AndDto() throws Exception {
            String email = uniqueEmail();

            postParkingLot(parkingLot(divisionId, newContact(email)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("Meir Underground"))
                    .andExpect(jsonPath("$.category").value("UNDERGROUND_BUILDING"))
                    .andExpect(jsonPath("$.capacity").value(250))
                    .andExpect(jsonPath("$.pricePerHour").value(2.5))
                    .andExpect(jsonPath("$.address.city").value("Antwerp"))
                    .andExpect(jsonPath("$.divisionId").value(divisionId.toString()))
                    .andExpect(jsonPath("$.contactPerson.id").isNotEmpty())
                    .andExpect(jsonPath("$.contactPerson.name").value("Jane Doe"))
                    .andExpect(jsonPath("$.contactPerson.email").value(email));
        }

        @Test
        void givenExistingContactPersonId_whenPostParkingLot_thenBothLotsShareTheContact() throws Exception {
            String first = postParkingLot(parkingLot(divisionId, newContact(uniqueEmail())))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            String contactId = JsonPath.read(first, "$.contactPerson.id");

            postParkingLot(parkingLot(divisionId, existingContact(contactId)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contactPerson.id").value(contactId));
        }
    }

    @Nested
    @DisplayName("Testing BAD REQUEST")
    @WithMockUser(authorities = "CREATE_PARKING_LOT")
    class TestingBadRequest {

        @Test
        void givenBothContactIdAndNewContact_whenPostParkingLot_thenReturns400() throws Exception {
            postParkingLot(parkingLot(divisionId,
                    existingContact(UUID.randomUUID()) + "," + newContact(uniqueEmail())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoContactPerson_whenPostParkingLot_thenReturns400() throws Exception {
            postParkingLot(parkingLot(divisionId, ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenContactWithoutPhoneNumber_whenPostParkingLot_thenReturns400() throws Exception {
            String contact = newContact(uniqueEmail()).replace("\"mobilePhone\":\"+32 470 12 34 56\",", "");

            postParkingLot(parkingLot(divisionId, contact))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNegativeCapacity_whenPostParkingLot_thenReturns400() throws Exception {
            postParkingLot(parkingLot(divisionId, newContact(uniqueEmail())).replace("\"capacity\":250", "\"capacity\":-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenUnknownCategory_whenPostParkingLot_thenReturns400() throws Exception {
            postParkingLot(parkingLot(divisionId, newContact(uniqueEmail())).replace("UNDERGROUND_BUILDING", "ROOFTOP"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testing NOT FOUND")
    @WithMockUser(authorities = "CREATE_PARKING_LOT")
    class TestingNotFound {

        @Test
        void givenUnknownDivision_whenPostParkingLot_thenReturns404() throws Exception {
            postParkingLot(parkingLot(UUID.randomUUID(), newContact(uniqueEmail())))
                    .andExpect(status().isNotFound());
        }

        @Test
        void givenUnknownContactPersonId_whenPostParkingLot_thenReturns404() throws Exception {
            postParkingLot(parkingLot(divisionId, existingContact(UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testing CONFLICT")
    @WithMockUser(authorities = "CREATE_PARKING_LOT")
    class TestingConflict {

        @Test
        void givenNewContactWithExistingEmail_whenPostParkingLot_thenReturns409() throws Exception {
            String email = uniqueEmail();
            postParkingLot(parkingLot(divisionId, newContact(email))).andExpect(status().isCreated());

            postParkingLot(parkingLot(divisionId, newContact(email)))
                    .andExpect(status().isConflict());
        }

        @Test
        void givenSameEmailInOtherCapitals_whenPostParkingLot_thenReturns409() throws Exception {
            String email = uniqueEmail();
            postParkingLot(parkingLot(divisionId, newContact(email))).andExpect(status().isCreated());

            postParkingLot(parkingLot(divisionId, newContact(email.toUpperCase())))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Testing UNAUTHORIZED|FORBIDDEN")
    class TestingSecurity {

        @Test
        void givenNoCredentials_whenPostParkingLot_thenReturns401() throws Exception {
            postParkingLot(parkingLot(divisionId, newContact(uniqueEmail())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISIONS")
        void givenUserWithoutFeature_whenPostParkingLot_thenReturns403() throws Exception {
            postParkingLot(parkingLot(divisionId, newContact(uniqueEmail())))
                    .andExpect(status().isForbidden());
        }
    }
}
