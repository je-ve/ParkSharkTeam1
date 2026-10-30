package com.switchfully.spring.parkshark.feature.division.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DivisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private void createDivision(String name) throws Exception {
        mockMvc.perform(post("/divisions")
                        .with(csrf())
                        .with(user("admin").authorities(new SimpleGrantedAuthority("CREATE_DIVISION")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"name":"%s","originalName":"MyOldCompany","director":"MyBoss"}
                """.formatted(name)))
                .andExpect(status().isCreated());
    }

    @Nested
    @DisplayName("Testing OK|CREATED")
    class TestingOkCreated {
        @Test
        @WithMockUser(authorities = "VIEW_DIVISIONS")
        void givenGoodCredentials_whenGetAllDivisions_thenReturns200AndDto() throws Exception {
            createDivision("MyCompany");
            createDivision("MyGreatCompany");
            createDivision("MyGreatestCompany");
            mockMvc.perform(get("/divisions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(4))); //4 because the create_division add an extra one
        }

        @Test
        @WithMockUser(authorities = "CREATE_DIVISION")
        void givenGoodCredentials_whenPostDivision_thenReturns201AndDto() throws Exception {
            mockMvc.perform(post("/divisions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"MyOldCompany","originalName":"MyOldCompany","director":"MyBoss"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("MyOldCompany"));
        }
    }

    @Nested
    @DisplayName("Testing FORBIDDEN")
    class TestingForbidden {
        @Test
        @WithMockUser(authorities = "CREATE_THE_GREATER_DIVISION")
        void givenWrongCredentials_whenPostDivision_thenReturns403() throws Exception {
            MockHttpServletResponse result = mockMvc.perform(post("/divisions")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"name":"MyCompany","originalName":"MyOldCompany","director":"MyBoss"}
                            """)).andReturn().getResponse();
            assertThat(result.getStatus()).isEqualTo(403);
            assertThat(HttpStatus.valueOf(result.getStatus())).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @WithMockUser(authorities = "CREATE_THE_GREATER_DIVISION")
        void givenWrongCredentials_whenGetAllDivisions_thenReturns403() throws Exception {
            MockHttpServletResponse result = mockMvc.perform(get("/divisions")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden()).andReturn().getResponse();

            assertThat(result.getStatus()).isEqualTo(403);
            assertThat(HttpStatus.valueOf(result.getStatus())).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Testing UNAUTHORIZED")
    class TestUnauthorized {
        @Test
        void givenNoCredentials_whenPostDivision_thenReturns401() throws Exception {
            MockHttpServletResponse result = mockMvc.perform(post("/divisions")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"name":"MyCompany","originalName":"MyOldCompany","director":"MyBoss"}
                            """)).andReturn().getResponse();
            assertThat(result.getStatus()).isEqualTo(401);
            assertThat(HttpStatus.valueOf(result.getStatus())).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void givenNoCredentials_whenGetAllDivisions_thenReturns401() throws Exception {
        MockHttpServletResponse result = mockMvc.perform(get("/divisions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn().getResponse();
        assertThat(result.getStatus()).isEqualTo(401);
        assertThat(HttpStatus.valueOf(result.getStatus())).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}