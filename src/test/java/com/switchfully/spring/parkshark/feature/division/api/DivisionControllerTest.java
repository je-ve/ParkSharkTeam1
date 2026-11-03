package com.switchfully.spring.parkshark.feature.division.api;

import com.jayway.jsonpath.JsonPath;
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

import static org.hamcrest.Matchers.contains;
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

    private UUID createDivision(String name) throws Exception {
        String response = mockMvc.perform(post("/divisions")
                        .with(csrf())
                        .with(user("manager").authorities(new SimpleGrantedAuthority("CREATE_DIVISION")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(division(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response);
    }

    private UUID createSubdivision(String name, UUID parentId) throws Exception {
        String response = mockMvc.perform(post("/divisions/{id}", parentId)
                        .with(csrf())
                        .with(user("manager").authorities(new SimpleGrantedAuthority("CREATE_SUBDIVISION")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(division(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response);
    }

    private static UUID idOf(String jsonResponse) {
        String id = JsonPath.read(jsonResponse, "$.id");
        return UUID.fromString(id);
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

        @Test
        @WithMockUser(authorities = "CREATE_SUBDIVISION")
        void givenAnExistingParent_whenPostSubdivision_thenReturns201WithTheParent() throws Exception {
            String parentName = uniqueName();
            UUID parentId = createDivision(parentName);
            String name = uniqueName();

            mockMvc.perform(post("/divisions/{id}", parentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(name)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value(name))
                    .andExpect(jsonPath("$.originalName").value("MyOldCompany"))
                    .andExpect(jsonPath("$.director").value("MyBoss"))
                    .andExpect(jsonPath("$.parentDivision.id").value(parentId.toString()))
                    .andExpect(jsonPath("$.parentDivision.name").value(parentName))
                    .andExpect(jsonPath("$.childrenDivisions").isEmpty());
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISION")
        void givenATopLevelDivisionWithASubdivision_whenGetDivision_thenReturns200WithoutParentAndWithItsChild() throws Exception {
            String name = uniqueName();
            UUID id = createDivision(name);
            String childName = uniqueName();
            createSubdivision(childName, id);

            mockMvc.perform(get("/divisions/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value(name))
                    .andExpect(jsonPath("$.parentDivision").doesNotExist())
                    .andExpect(jsonPath("$.childrenDivisions[*].name", contains(childName)));
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISION")
        void givenASubdivision_whenGetDivision_thenReturns200WithItsParentAndNoChildren() throws Exception {
            String parentName = uniqueName();
            UUID parentId = createDivision(parentName);
            String name = uniqueName();
            UUID id = createSubdivision(name, parentId);

            mockMvc.perform(get("/divisions/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(name))
                    .andExpect(jsonPath("$.parentDivision.id").value(parentId.toString()))
                    .andExpect(jsonPath("$.parentDivision.name").value(parentName))
                    .andExpect(jsonPath("$.childrenDivisions").isEmpty());
        }

        // GET /divisions/{id} only returns DIRECT subdivisions, not grandchildren.
        @Test
        @WithMockUser(authorities = "VIEW_DIVISION")
        void givenThreeLevelsOfDivisions_whenGetTopDivision_thenOnlyDirectChildrenAreReturned() throws Exception {
            UUID topId = createDivision(uniqueName());
            String middleName = uniqueName();
            UUID middleId = createSubdivision(middleName, topId);
            String bottomName = uniqueName();
            createSubdivision(bottomName, middleId);

            mockMvc.perform(get("/divisions/{id}", topId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.childrenDivisions[*].name", contains(middleName)));

            mockMvc.perform(get("/divisions/{id}", middleId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.parentDivision.id").value(topId.toString()))
                    .andExpect(jsonPath("$.childrenDivisions[*].name", contains(bottomName)));
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

        @Test
        @WithMockUser(authorities = "CREATE_SUBDIVISION")
        void givenBlankFields_whenPostSubdivision_thenReturns400ListingEveryInvalidField() throws Exception {
            UUID parentId = createDivision(uniqueName());

            mockMvc.perform(post("/divisions/{id}", parentId)
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
        @WithMockUser(authorities = "CREATE_SUBDIVISION")
        void givenAnIdThatIsNotAUuid_whenPostSubdivision_thenReturns400() throws Exception {
            mockMvc.perform(post("/divisions/{id}", "not-a-uuid")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISION")
        void givenAnIdThatIsNotAUuid_whenGetDivision_thenReturns400() throws Exception {
            mockMvc.perform(get("/divisions/{id}", "not-a-uuid")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
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

        @Test
        @WithMockUser(authorities = "CREATE_SUBDIVISION")
        void givenAnExistingName_whenPostSubdivision_thenReturns409() throws Exception {
            UUID parentId = createDivision(uniqueName());
            String name = uniqueName();
            createDivision(name);

            mockMvc.perform(post("/divisions/{id}", parentId)
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

        @Test
        @WithMockUser(username = "member@parkshark.com")
        void givenAMemberWithoutFeatures_whenPostSubdivision_thenReturns403() throws Exception {
            mockMvc.perform(post("/divisions/{id}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "member@parkshark.com")
        void givenAMemberWithoutFeatures_whenGetDivision_thenReturns403() throws Exception {
            mockMvc.perform(get("/divisions/{id}", UUID.randomUUID())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        // CREATE_DIVISION and CREATE_SUBDIVISION are two separate features
        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenOnlyCreateDivision_whenPostSubdivision_thenReturns403() throws Exception {
            mockMvc.perform(post("/divisions/{id}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isForbidden());
        }

        // VIEW_DIVISIONS (list) and VIEW_DIVISION (single) are two separate features
        @Test
        @WithMockUser(authorities = "VIEW_DIVISIONS")
        void givenOnlyViewDivisions_whenGetDivision_thenReturns403() throws Exception {
            mockMvc.perform(get("/divisions/{id}", UUID.randomUUID())
                            .accept(MediaType.APPLICATION_JSON))
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

        @Test
        void givenNoCredentials_whenPostSubdivision_thenReturns401() throws Exception {
            mockMvc.perform(post("/divisions/{id}", UUID.randomUUID())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void givenNoCredentials_whenGetDivision_thenReturns401() throws Exception {
            mockMvc.perform(get("/divisions/{id}", UUID.randomUUID())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Testing NOT_FOUND")
    class TestingNotFound {

        @Test
        @WithMockUser(authorities = "CREATE_SUBDIVISION")
        void givenAnUnknownParent_whenPostSubdivision_thenReturns404() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(post("/divisions/{id}", unknownId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(division(uniqueName())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Division %s not found.".formatted(unknownId)));
        }

        @Test
        @WithMockUser(authorities = "VIEW_DIVISION")
        void givenAnUnknownId_whenGetDivision_thenReturns404() throws Exception {
            UUID unknownId = UUID.randomUUID();

            mockMvc.perform(get("/divisions/{id}", unknownId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Division %s not found.".formatted(unknownId)));
        }
    }
}
