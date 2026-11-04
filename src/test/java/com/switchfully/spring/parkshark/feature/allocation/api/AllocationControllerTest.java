package com.switchfully.spring.parkshark.feature.allocation.api;

import com.switchfully.spring.parkshark.common.MutableClock;
import com.switchfully.spring.parkshark.common.domain.Address;
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
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.CredentialsRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneId.of("Europe/Brussels")));
        }
    }

    @Autowired
    private MockMvc mockMvc;
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

    private Member saveMember(MembershipLevel level) {
        Credentials credentials = credentialsRepository.save(
                new Credentials("member-" + UUID.randomUUID() + "@parkshark.be", "hash", Role.MEMBER));
        return memberRepository.save(new Member("John", "Doe", ADDRESS, "0258748582",
                new LicensePlate(MEMBER_PLATE, "BE"), LocalDate.of(2026, 1, 1), level, credentials));
    }

    private ParkingLot saveParkingLot(int capacity) {
        Division division = divisionRepository.save(
                new Division("Division-" + UUID.randomUUID(), "Old company", "Boss"));
        ContactPerson contactPerson = contactPersonRepository.save(
                new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "contact-" + UUID.randomUUID() + "@company.be", ADDRESS));
        return parkingLotRepository.save(new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING,
                capacity, contactPerson, ADDRESS, division, new BigDecimal("2.50")));
    }

    private static String body(Object memberId, String licencePlate, Object parkingLotId) {
        return """
                {"memberId":"%s","licencePlate":"%s","parkingLotId":"%s"}""".formatted(memberId, licencePlate, parkingLotId);
    }

    private ResultActions postAllocation(String json) throws Exception {
        return mockMvc.perform(post("/allocations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Nested
    @DisplayName("Testing CREATED")
    @WithMockUser(authorities = "START_ALLOCATION")
    class TestingCreated {

        @Test
        void givenMemberWithOwnPlate_whenPostAllocation_thenReturns201WithIdAndServerStartTime() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.allocationId").isNotEmpty())
                    .andExpect(jsonPath("$.startTime").value("2026-01-01T09:00:00"));
        }

        @Test
        void givenGoldMemberWithOtherPlate_whenPostAllocation_thenReturns201() throws Exception {
            Member member = saveMember(MembershipLevel.GOLD);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.allocationId").isNotEmpty());
        }

        @Test
        void givenValidRequest_whenPostAllocation_thenAllocationIsStoredAsOngoing() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Testing BAD REQUEST")
    @WithMockUser(authorities = "START_ALLOCATION")
    class TestingBadRequest {

        @Test
        void givenBronzeMemberWithOtherPlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenSilverMemberWithOtherPlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.SILVER);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenRefusedPlate_whenPostAllocation_thenNothingIsStored() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), OTHER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isZero();
        }

        @Test
        void givenNoMemberId_whenPostAllocation_thenReturns400() throws Exception {
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation("""
                    {"licencePlate":"%s","parkingLotId":"%s"}""".formatted(MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoLicencePlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation("""
                    {"memberId":"%s","parkingLotId":"%s"}""".formatted(member.getId(), parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenBlankLicencePlate_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(member.getId(), "  ", parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoParkingLotId_whenPostAllocation_thenReturns400() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation("""
                    {"memberId":"%s","licencePlate":"%s"}""".formatted(member.getId(), MEMBER_PLATE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenMalformedMemberId_whenPostAllocation_thenReturns400() throws Exception {
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body("not-a-uuid", MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenEmptyBody_whenPostAllocation_thenReturns400() throws Exception {
            postAllocation("")
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Testing NOT FOUND")
    @WithMockUser(authorities = "START_ALLOCATION")
    class TestingNotFound {

        @Test
        void givenUnknownMember_whenPostAllocation_thenReturns404() throws Exception {
            ParkingLot parkingLot = saveParkingLot(10);

            postAllocation(body(UUID.randomUUID(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void givenUnknownParkingLot_whenPostAllocation_thenReturns404() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);

            postAllocation(body(member.getId(), MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Testing CONFLICT")
    @WithMockUser(authorities = "START_ALLOCATION")
    class TestingConflict {

        @Test
        void givenFullParkingLot_whenPostAllocation_thenReturns409() throws Exception {
            ParkingLot parkingLot = saveParkingLot(1);
            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isConflict());
        }

        @Test
        void givenFullParkingLot_whenPostAllocation_thenNoExtraAllocationIsStored() throws Exception {
            ParkingLot parkingLot = saveParkingLot(1);
            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isConflict());

            assertThat(allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
                    .isEqualTo(1);
        }

        @Test
        void givenParkingLotWithOneSpotLeft_whenPostAllocation_thenReturns201() throws Exception {
            ParkingLot parkingLot = saveParkingLot(2);
            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());

            postAllocation(body(saveMember(MembershipLevel.BRONZE).getId(), MEMBER_PLATE, parkingLot.getId()))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Testing UNAUTHORIZED|FORBIDDEN")
    class TestingSecurity {

        @Test
        void givenNoCredentials_whenPostAllocation_thenReturns401() throws Exception {
            postAllocation(body(UUID.randomUUID(), MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "CREATE_PARKING_LOT")
        void givenUserWithoutStartAllocationFeature_whenPostAllocation_thenReturns403() throws Exception {
            postAllocation(body(UUID.randomUUID(), MEMBER_PLATE, UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }
}