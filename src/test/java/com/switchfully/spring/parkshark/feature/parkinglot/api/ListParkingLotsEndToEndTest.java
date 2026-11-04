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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListParkingLotsEndToEndTest {

    private static final String ADDRESS = """
            {"streetName":"Meir","streetNumber":"1","postalCode":"2000","city":"Antwerp"}""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DivisionRepository divisionRepository;

    private Division division;

    @BeforeEach
    void createDivision() {
        division = divisionRepository.save(new Division("Division-" + UUID.randomUUID(), "Old company", "Boss"));
    }

    private static String uniqueEmail() {
        return "contact-" + UUID.randomUUID() + "@company.be";
    }

    private String createParkingLot(String name, String email, String mobilePhone, String telephoneNumber) throws Exception {
        String json = """
                {"name":"%s","category":"ABOVE_GROUND_BUILDING","capacity":120,"address":%s,\
                "pricePerHour":3.20,"divisionId":"%s",\
                "contactPerson":{"name":"Jane Doe","mobilePhone":%s,"telephoneNumber":%s,"email":"%s","address":%s}}"""
                .formatted(name, ADDRESS, division.getId(), quoted(mobilePhone), quoted(telephoneNumber), email, ADDRESS);
        String response = mockMvc.perform(post("/parking-lots")
                        .with(csrf())
                        .with(user("manager").authorities(new SimpleGrantedAuthority("CREATE_PARKING_LOT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private static String quoted(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    @Nested
    @DisplayName("PL2 · Overview of all parking lots")
    class Overview {

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOTS")
        void givenParkingLots_whenGetAll_thenReturns200WithOnlyTheOverviewFields() throws Exception {
            String email = uniqueEmail();
            String id = createParkingLot("Overview lot", email, "+32 470 12 34 56", "+32 3 123 45 67");

            String response = mockMvc.perform(get("/parking-lots"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Map<String, Object>> matches = JsonPath.read(response, "$[?(@.id == '%s')]".formatted(id));
            assertThat(matches).hasSize(1);
            assertThat(matches.get(0)).containsOnlyKeys(
                    "id", "name", "capacity", "contactEmail", "contactMobilePhone", "contactTelephoneNumber");
            assertThat(matches.get(0))
                    .containsEntry("name", "Overview lot")
                    .containsEntry("capacity", 120)
                    .containsEntry("contactEmail", email)
                    .containsEntry("contactMobilePhone", "+32 470 12 34 56")
                    .containsEntry("contactTelephoneNumber", "+32 3 123 45 67");
        }

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOTS")
        void givenParkingLots_whenGetAll_thenTheyAreSortedByName() throws Exception {
            String suffix = UUID.randomUUID().toString();
            createParkingLot("ZZ last " + suffix, uniqueEmail(), "+32 470 12 34 56", null);
            createParkingLot("AA first " + suffix, uniqueEmail(), "+32 470 12 34 56", null);

            String response = mockMvc.perform(get("/parking-lots"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<String> names = JsonPath.read(response, "$[*].name");
            assertThat(names).isSorted();
        }

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOTS")
        void givenContactWithOnlyAMobilePhone_whenGetAll_thenTelephoneIsNull() throws Exception {
            String id = createParkingLot("Mobile only lot", uniqueEmail(), "+32 470 12 34 56", null);

            String response = mockMvc.perform(get("/parking-lots"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Map<String, Object>> matches = JsonPath.read(response, "$[?(@.id == '%s')]".formatted(id));
            assertThat(matches.get(0))
                    .containsEntry("contactMobilePhone", "+32 470 12 34 56")
                    .containsEntry("contactTelephoneNumber", null);
        }

        @Test
        void givenNoCredentials_whenGetAll_thenReturns401() throws Exception {
            mockMvc.perform(get("/parking-lots"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        void givenMemberWithoutFeature_whenGetAll_thenReturns403() throws Exception {
            mockMvc.perform(get("/parking-lots"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PL3 · One parking lot with its division")
    class Detail {

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOT")
        void givenExistingId_whenGetById_thenReturns200WithAllDataAndDivision() throws Exception {
            String email = uniqueEmail();
            String id = createParkingLot("Detail lot", email, "+32 470 12 34 56", null);

            mockMvc.perform(get("/parking-lots/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Detail lot"))
                    .andExpect(jsonPath("$.category").value("ABOVE_GROUND_BUILDING"))
                    .andExpect(jsonPath("$.capacity").value(120))
                    .andExpect(jsonPath("$.pricePerHour").value(3.2))
                    .andExpect(jsonPath("$.address.city").value("Antwerp"))
                    .andExpect(jsonPath("$.contactPerson.email").value(email))
                    .andExpect(jsonPath("$.contactPerson.mobilePhone").value("+32 470 12 34 56"))
                    .andExpect(jsonPath("$.division.id").value(division.getId().toString()))
                    .andExpect(jsonPath("$.division.name").value(division.getName()))
                    .andExpect(jsonPath("$.division.director").value("Boss"));
        }

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOT")
        void givenUnknownId_whenGetById_thenReturns404() throws Exception {
            mockMvc.perform(get("/parking-lots/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = "VIEW_PARKING_LOT")
        void givenInvalidUuid_whenGetById_thenReturns400() throws Exception {
            mockMvc.perform(get("/parking-lots/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoCredentials_whenGetById_thenReturns401() throws Exception {
            mockMvc.perform(get("/parking-lots/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        void givenMemberWithoutFeature_whenGetById_thenReturns403() throws Exception {
            mockMvc.perform(get("/parking-lots/{id}", UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }
}
