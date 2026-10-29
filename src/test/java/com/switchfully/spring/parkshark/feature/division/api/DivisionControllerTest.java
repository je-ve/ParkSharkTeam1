package com.switchfully.spring.parkshark.feature.division.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DivisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "CREATE_DIVISION")
    void givenGoodCredentials_whenPostDivision_thenReturns201AndDto() throws Exception {
        mockMvc.perform(post("/divisions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"MyCompany","originalName":"MyOldCompany","director":"MyBoss"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("MyCompany"));
    }

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