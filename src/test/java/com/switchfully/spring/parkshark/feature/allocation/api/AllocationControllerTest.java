package com.switchfully.spring.parkshark.feature.allocation.api;

import com.jayway.jsonpath.JsonPath;
import com.switchfully.spring.parkshark.common.MutableClock;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPersonRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import com.switchfully.spring.parkshark.feature.security.authentication.AuthenticatedUser;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.CredentialsRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AllocationControllerTest.TestClockConfig.class)
class AllocationControllerTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final String MEMBER_PLATE = "1-ABC-123";
    private static final String OTHER_PLATE = "2-XYZ-999";
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneId.of("Europe/Brussels"));

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(NOW);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MutableClock clock;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DivisionRepository divisionRepository;
    @Autowired
    private ContactPersonRepository contactPersonRepository;
    @Autowired
    private ParkingLotRepository parkingLotRepository;
    @Autowired
    private AllocationRepository allocationRepository;

    @BeforeEach
    void resetClock() {
        clock.setTo(NOW);
    }

    private Credentials saveCredentials(Role role) {
        return credentialsRepository.save(
                new Credentials(role.name().toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID() + "@parkshark.be", "hash", role));
    }

    private Member saveMember(MembershipLevel level) {
        return memberRepository.save(new Member("John", "Doe", ADDRESS, "0258748582",
                new LicensePlate(MEMBER_PLATE, "BE"), LocalDate.of(2026, 1, 1), level, saveCredentials(Role.MEMBER)));
    }

    private ParkingLot saveParkingLot(int capacity) {
        Division division = divisionRepository.save(
                new Division("Division-" + UUID.randomUUID(), "Old company", "Boss"));
        ContactPerson contactPerson = contactPersonRepository.save(
                new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "contact-" + UUID.randomUUID() + "@company.be", ADDRESS));
        return parkingLotRepository.save(new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING,
                capacity, contactPerson, ADDRESS, division, new BigDecimal("2.50")));
    }

    private static String body(String licencePlate, Object parkingLotId) {
        return """
                {"licencePlate":"%s","parkingLotId":"%s"}""".formatted(licencePlate, parkingLotId);
    }

    private ResultActions postAllocation(Credentials credentials, String json) throws Exception {
        return mockMvc.perform(post("/allocations")
                .with(user(AuthenticatedUser.from(credentials)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions postAllocationWithoutCredentials(String json) throws Exception {
        return mockMvc.perform(post("/allocations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private Allocation saveOngoingAllocation(Member member, ParkingLot parkingLot) {
        return allocationRepository.save(
                new Allocation(LocalDateTime.of(2026, 1, 1, 8, 0), member, MEMBER_PLATE, parkingLot));
    }

    private ResultActions patchStopAllocation(Credentials credentials, Object allocationId) throws Exception {
        return mockMvc.perform(patch("/allocations/{id}/stop", allocationId)
                .with(user(AuthenticatedUser.from(credentials)))
                .with(csrf()));
    }

    private ResultActions patchStopAllocationWithoutCredentials(Object allocationId) throws Exception {
        return mockMvc.perform(patch("/allocations/{id}/stop", allocationId)
                .with(csrf()));
    }

    private String statusOf(UUID allocationId) {
        return jdbcTemplate.queryForObject("select status from allocation where id = ?", String.class, allocationId);
    }

    private LocalDateTime endTimeOf(UUID allocationId) {
        return jdbcTemplate.queryForObject("select end_time from allocation where id = ?", LocalDateTime.class, allocationId);
    }

    private UUID memberOfAllocation(String allocationId) {
        return jdbcTemplate.queryForObject("select member_id from allocation where id = ?",
                UUID.class, UUID.fromString(allocationId));
    }

    @Nested
    @DisplayName("Testing CREATED")
    class TestingCreated {

        @Test
        void givenMemberWithOwnPlate_whenPostAllocation_thenReturns201WithIdAndServerStartTime() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.allocationId").isNotEmpty())
                    .andExpect(jsonPath("$.startTime").value("2026-01-01T09:00:00"));
        }

        @Test
        void givenGoldMemberWithOtherPlate_whenPostAllocation_thenReturns201() throws Exception {
            Member member = saveMember(MembershipLevel.GOLD);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.allocationId").isNotEmpty());
        }

        @Test
        void givenValidRequest_whenPostAllocation_thenAllocationIsStoredAsOngoing() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isEqualTo(1);
        }

        @Test
        void givenLoggedInMember_whenPostAllocation_thenAllocationBelongsToThatMember() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            String response = postAllocation(member.getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            assertThat(memberOfAllocation(JsonPath.read(response, "$.allocationId"))).isEqualTo(member.getId());
        }

        @Test
        void givenBodyWithAnotherMembersId_whenPostAllocation_thenAllocationIsStillStartedForTheLoggedInMember() throws Exception {
            Member loggedIn = saveMember(MembershipLevel.BRONZE);
            Member someoneElse = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);
            String json = """
                    {"memberId":"%s","licencePlate":"%s","parkingLotId":"%s"}"""
                    .formatted(someoneElse.getId(), MEMBER_PLATE, parkingLot.getId());

            String response = postAllocation(loggedIn.getCredentials(), json)
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            assertThat(memberOfAllocation(JsonPath.read(response, "$.allocationId")))
                    .isEqualTo(loggedIn.getId())
                    .isNotEqualTo(someoneElse.getId());
        }
    }

    @Nested
    @DisplayName("Testing BAD REQUEST")
    class TestingBadRequest {

        @Test
        void givenBronzeMemberWithOtherPlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenSilverMemberWithOtherPlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.SILVER);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenRefusedPlate_whenPostAllocation_thenNothingIsStored() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body(OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isZero();
        }

        @Test
        void givenNoLicencePlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), """
                    {"parkingLotId":"%s"}""".formatted(parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenBlankLicencePlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(member.getCredentials(), body("  ", parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoParkingLotId_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation(member.getCredentials(), """
                    {"licencePlate":"%s"}""".formatted(MEMBER_PLATE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenMalformedParkingLotId_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation(member.getCredentials(), body(MEMBER_PLATE, "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenEmptyBody_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation(member.getCredentials(), "")
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testing NOT FOUND")
    class TestingNotFound {

        @Test
        void givenUnknownParkingLot_whenPostAllocation_thenReturns404() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation(member.getCredentials(), body(MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void givenLoggedInUserWithoutMemberRecord_whenPostAllocation_thenReturns404() throws Exception {
            Credentials credentialsWithoutMember = saveCredentials(Role.MEMBER);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(credentialsWithoutMember, body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testing CONFLICT")
    class TestingConflict {

        @Test
        void givenFullParkingLot_whenPostAllocation_thenReturns409() throws Exception {
            ParkingLot parkingLot = saveParkingLot(1);
            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isConflict());
        }

        @Test
        void givenFullParkingLot_whenPostAllocation_thenNoExtraAllocationIsStored() throws Exception {
            ParkingLot parkingLot = saveParkingLot(1);
            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isConflict());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isEqualTo(1);
        }

        @Test
        void givenParkingLotWithOneSpotLeft_whenPostAllocation_thenReturns201() throws Exception {
            ParkingLot parkingLot = saveParkingLot(2);
            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(saveMember(MembershipLevel.BRONZE).getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Testing UNAUTHORIZED|FORBIDDEN")
    class TestingSecurity {

        @Test
        void givenNoCredentials_whenPostAllocation_thenReturns401() throws Exception {
            postAllocationWithoutCredentials(body(MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void givenManager_whenPostAllocation_thenReturns403() throws Exception {
            Credentials manager = saveCredentials(Role.MANAGER);

            postAllocation(manager, body(MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Testing STOPPED")
    class TestingStopped {

        @Test
        void givenOwnerOfOngoingAllocation_whenStopAllocation_thenReturns200WithStoppedAllocation() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(member, saveParkingLot(10));

            patchStopAllocation(member.getCredentials(), allocation.getId())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(allocation.getId().toString()))
                    .andExpect(jsonPath("$.startTime").value("2026-01-01T08:00:00"))
                    .andExpect(jsonPath("$.stopTime").value("2026-01-01T09:00:00"))
                    .andExpect(jsonPath("$.licensePlateNumber").value(MEMBER_PLATE))
                    .andExpect(jsonPath("$.status").value("STOPPED"));
        }

        @Test
        void givenOwnerOfOngoingAllocation_whenStopAllocation_thenStatusAndStopTimeAreStored() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(member, saveParkingLot(10));

            patchStopAllocation(member.getCredentials(), allocation.getId())
                    .andExpect(status().isOk());

            assertThat(statusOf(allocation.getId())).isEqualTo("STOPPED");
            assertThat(endTimeOf(allocation.getId())).isEqualTo(LocalDateTime.of(2026, 1, 1, 9, 0));
        }

        @Test
        void givenFullParkingLot_whenTheOwnerStopsTheirAllocation_thenTheSpotIsFreeAgain() throws Exception {
            ParkingLot parkingLot = saveParkingLot(1);
            Member parked = saveMember(MembershipLevel.BRONZE);
            Member waiting = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(parked, parkingLot);
            postAllocation(waiting.getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isConflict());

            patchStopAllocation(parked.getCredentials(), allocation.getId())
                    .andExpect(status().isOk());

            postAllocation(waiting.getCredentials(), body(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Testing STOP FORBIDDEN|CONFLICT|BAD REQUEST")
    class TestingStopRefused {

        @Test
        void givenAllocationOfAnotherMember_whenStopAllocation_thenReturns403AndAllocationStaysOngoing() throws Exception {
            Member owner = saveMember(MembershipLevel.BRONZE);
            Member someoneElse = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(owner, saveParkingLot(10));

            patchStopAllocation(someoneElse.getCredentials(), allocation.getId())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("Allocation %s belongs to another member.".formatted(allocation.getId())));

            assertThat(statusOf(allocation.getId())).isEqualTo("ONGOING");
            assertThat(endTimeOf(allocation.getId())).isNull();
        }

        @Test
        void givenStoppedAllocationOfAnotherMember_whenStopAllocation_thenReturns403NotRevealingItsStatus() throws Exception {
            Member owner = saveMember(MembershipLevel.BRONZE);
            Member someoneElse = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(owner, saveParkingLot(10));
            patchStopAllocation(owner.getCredentials(), allocation.getId()).andExpect(status().isOk());

            patchStopAllocation(someoneElse.getCredentials(), allocation.getId())
                    .andExpect(status().isForbidden());
        }

        @Test
        void givenAlreadyStoppedAllocation_whenStopAllocation_thenReturns409AndStopTimeIsUnchanged() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            Allocation allocation = saveOngoingAllocation(member, saveParkingLot(10));
            patchStopAllocation(member.getCredentials(), allocation.getId())
                    .andExpect(status().isOk());
            clock.setTo(NOW.plusHours(1));

            patchStopAllocation(member.getCredentials(), allocation.getId())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Allocation %s is not active and cannot be stopped.".formatted(allocation.getId())));

            assertThat(endTimeOf(allocation.getId())).isEqualTo(LocalDateTime.of(2026, 1, 1, 9, 0));
        }

        @Test
        void givenMalformedAllocationId_whenStopAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            patchStopAllocation(member.getCredentials(), "not-a-uuid")
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testing START validation and messages")
    class TestingStartValidation {

        @Test
        void givenLicencePlateLongerThan20Characters_whenPostAllocation_thenReturns400NotParkingLotFull() throws Exception {
            Member gold = saveMember(MembershipLevel.GOLD);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(gold.getCredentials(), body("X".repeat(21), parkingLot.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.licencePlate[0]").value("License plate must be at most 20 characters"));
        }

        @Test
        void givenUnknownParkingLot_whenPostAllocation_thenReturns404WithClearMessage() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            UUID unknown = UUID.randomUUID();

            postAllocation(member.getCredentials(), body(MEMBER_PLATE, unknown))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString(unknown.toString())))
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("parking lot")));
        }
    }

    @Nested
    @DisplayName("Testing STOP NOT FOUND")
    class TestingStopNotFound {

        @Test
        void givenUnknownAllocation_whenStopAllocation_thenReturns404() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            patchStopAllocation(member.getCredentials(), UUID.randomUUID())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testing STOP UNAUTHORIZED|FORBIDDEN")
    class TestingStopSecurity {

        @Test
        void givenNoCredentials_whenStopAllocation_thenReturns401() throws Exception {
            patchStopAllocationWithoutCredentials(UUID.randomUUID())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void givenManager_whenStopAllocation_thenReturns403() throws Exception {
            Credentials manager = saveCredentials(Role.MANAGER);

            patchStopAllocation(manager, UUID.randomUUID())
                    .andExpect(status().isForbidden());
        }
    }
}