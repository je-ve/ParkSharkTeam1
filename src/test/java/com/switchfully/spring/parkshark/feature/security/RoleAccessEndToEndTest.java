package com.switchfully.spring.parkshark.feature.security;

import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.CredentialsRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Every secured endpoint, called with real accounts through HTTP Basic (no mocked users):
 * the right role gets in, the other role gets 403, and a wrong password gets 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoleAccessEndToEndTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    record Endpoint(HttpMethod method, String path, String body) {
        @Override
        public String toString() {
            return method + " " + path;
        }
    }

    private static String unique() {
        return UUID.randomUUID().toString();
    }

    private static String division() {
        return """
                {"name":"Division-%s","originalName":"Old company","director":"Boss"}""".formatted(unique());
    }

    private static String parkingLot() {
        String address = """
                {"streetName":"Meir","streetNumber":"1","postalCode":"2000","city":"Antwerp"}""";
        return """
                {"name":"Lot","category":"UNDERGROUND_BUILDING","capacity":10,"address":%s,"pricePerHour":2.50,\
                "divisionId":"%s","contactPerson":{"name":"Jane","mobilePhone":"0470","email":"c-%s@company.be","address":%s}}"""
                .formatted(address, UUID.randomUUID(), unique(), address);
    }

    static Stream<Endpoint> managerEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/divisions", division()),
                new Endpoint(HttpMethod.GET, "/divisions", null),
                new Endpoint(HttpMethod.POST, "/divisions/" + UUID.randomUUID(), division()),
                new Endpoint(HttpMethod.GET, "/divisions/" + UUID.randomUUID(), null),
                new Endpoint(HttpMethod.POST, "/parking-lots", parkingLot()),
                new Endpoint(HttpMethod.GET, "/parking-lots", null),
                new Endpoint(HttpMethod.GET, "/parking-lots/" + UUID.randomUUID(), null),
                new Endpoint(HttpMethod.GET, "/members", null),
                new Endpoint(HttpMethod.GET, "/members/" + UUID.randomUUID(), null),
                new Endpoint(HttpMethod.GET, "/invoices", null),
                new Endpoint(HttpMethod.POST, "/invoices/999999999/close", null));
    }

    static Stream<Endpoint> memberEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/allocations",
                        """
                        {"licencePlate":"1-ABC-123","parkingLotId":"%s"}""".formatted(UUID.randomUUID())),
                new Endpoint(HttpMethod.PATCH, "/allocations/" + UUID.randomUUID() + "/stop", null),
                new Endpoint(HttpMethod.POST, "/invoices", null));
    }

    static Stream<Endpoint> allSecuredEndpoints() {
        return Stream.concat(managerEndpoints(), memberEndpoints());
    }

    private String saveAccount(Role role) {
        String email = role.name().toLowerCase(Locale.ROOT) + "-" + unique() + "@parkshark.be";
        credentialsRepository.save(new Credentials(email, passwordEncoder.encode(PASSWORD), role));
        return email;
    }

    private int call(Endpoint endpoint, String email, String password) throws Exception {
        MockHttpServletRequestBuilder request = request(endpoint.method(), endpoint.path())
                .with(httpBasic(email, password));
        if (endpoint.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        return mockMvc.perform(request).andReturn().getResponse().getStatus();
    }

    @ParameterizedTest(name = "manager is allowed on {0}")
    @MethodSource("managerEndpoints")
    void givenRealManager_whenCallingManagerEndpoint_thenAccessIsGranted(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, saveAccount(Role.MANAGER), PASSWORD)).isNotIn(401, 403);
    }

    @ParameterizedTest(name = "member gets 403 on {0}")
    @MethodSource("managerEndpoints")
    void givenRealMember_whenCallingManagerEndpoint_thenReturns403(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, saveAccount(Role.MEMBER), PASSWORD)).isEqualTo(403);
    }

    @ParameterizedTest(name = "member is allowed on {0}")
    @MethodSource("memberEndpoints")
    void givenRealMember_whenCallingMemberEndpoint_thenAccessIsGranted(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, saveAccount(Role.MEMBER), PASSWORD)).isNotIn(401, 403);
    }

    @ParameterizedTest(name = "manager gets 403 on {0}")
    @MethodSource("memberEndpoints")
    void givenRealManager_whenCallingMemberEndpoint_thenReturns403(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, saveAccount(Role.MANAGER), PASSWORD)).isEqualTo(403);
    }

    @ParameterizedTest(name = "wrong password gets 401 on {0}")
    @MethodSource("allSecuredEndpoints")
    void givenWrongPassword_whenCallingAnySecuredEndpoint_thenReturns401(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, saveAccount(Role.MANAGER), "wrong-password")).isEqualTo(401);
    }

    @ParameterizedTest(name = "unknown user gets 401 on {0}")
    @MethodSource("allSecuredEndpoints")
    void givenUnknownUser_whenCallingAnySecuredEndpoint_thenReturns401(Endpoint endpoint) throws Exception {
        assertThat(call(endpoint, "nobody-" + unique() + "@parkshark.be", PASSWORD)).isEqualTo(401);
    }
}
