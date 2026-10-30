package com.switchfully.spring.parkshark.feature.division.api;

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

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DivisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String uniqueName() {
        return "Division-" + UUID.randomUUID();
    }

    private static String division(String name) {
        return """
                {"name":"%s","originalName":"MyOldCompany","director":"MyBoss"}
                """.formatted(name);
    }

    private void createDivision(String name) throws Exception {
        mockMvc.perform(post("/divisions")
                        .with(csrf())
                        .with(user("manager").authorities(new SimpleGrantedAuthority("CREATE_DIVISION")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(division(name)))
                .andExpect(status().isCreated());
    }

    @Nested
    @DisplayName("Testing OK|CREATED")
    class TestingOkCreated {

        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenGoodCredentials_whenPostDivision_thenReturns201AndDto() throws Exception {
            String name = uniqueName();

            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(name)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value(name))
                    .andExpect(jsonPath("$.originalName").value("MyOldCompany"))
                    .andExpect(jsonPath("$.director").value("MyBoss"));
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISIONS")
        void givenGoodCredentials_whenGetAllDivisions_thenReturns200WithTheCreatedDivisions() throws Exception {
            String firstName = uniqueName();
            String secondName = uniqueName();
            createDivision(firstName);
            createDivision(secondName);

            mockMvc.perform(get("/divisions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].name", hasItems(firstName, secondName)));
        }
    }

    @Nested
    @DisplayName("Testing BAD_REQUEST")
    class TestingBadRequest {

        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenBlankFields_whenPostDivision_thenReturns400ListingEveryInvalidField() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","originalName":"   "}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.name", hasItem("Name is required")))
                    .andExpect(jsonPath("$.errors.originalName", hasItem("Original name is required")))
                    .andExpect(jsonPath("$.errors.director", hasItem("Director is required")));
        }

        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenANameLongerThan50Characters_whenPostDivision_thenReturns400() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division("N".repeat(51))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name", hasItem("Name must be at most 50 characters")));
        }
    }

    @Nested
    @DisplayName("Testing CONFLICT")
    class TestingConflict {

        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenAnExistingName_whenPostDivision_thenReturns409() throws Exception {
            String name = uniqueName();
            createDivision(name);

            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(name)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Division %s already exists.".formatted(name)));
        }
    }

    @Nested
    @DisplayName("Testing FORBIDDEN")
    class TestingForbidden {

        @Test
        @WithMockUser(username = "member@parkshark.com")
        void givenAMemberWithoutFeatures_whenPostDivision_thenReturns403() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "member@parkshark.com")
        void givenAMemberWithoutFeatures_whenGetAllDivisions_thenReturns403() throws Exception {
            mockMvc.perform(get("/divisions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISIONS")
        void givenOnlyViewDivisions_whenPostDivision_thenReturns403() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Testing UNAUTHORIZED")
    class TestingUnauthorized {

        @Test
        void givenNoCredentials_whenPostDivision_thenReturns401() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void givenNoCredentials_whenGetAllDivisions_thenReturns401() throws Exception {
            mockMvc.perform(get("/divisions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void givenWrongCredentials_whenGetAllDivisions_thenReturns401() throws Exception {
            mockMvc.perform(get("/divisions")
                            .with(httpBasic("nobody@parkshark.be", "wrong-password"))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
