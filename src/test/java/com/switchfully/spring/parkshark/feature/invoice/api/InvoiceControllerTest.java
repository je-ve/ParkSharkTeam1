package com.switchfully.spring.parkshark.feature.invoice.api;

import com.jayway.jsonpath.JsonPath;
import com.switchfully.spring.parkshark.common.MutableClock;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(InvoiceControllerTest.TestClockConfig.class)
class InvoiceControllerTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 3, 17, 9, 0, 0, 0, ZoneId.of("Europe/Brussels"));
    private static final LocalDate TODAY = NOW.toLocalDate();
    private static final LocalDateTime START = LocalDateTime.of(2026, 3, 10, 9, 0);

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

    private ParkingLot parkingLot;

    @BeforeEach
    void setUp() {
        clock.setTo(NOW);
        Division division = divisionRepository.save(new Division("Division-" + UUID.randomUUID(), "Old company", "Boss"));
        ContactPerson contactPerson = contactPersonRepository.save(new ContactPerson("Jane Doe", "+32 470 12 34 56", null,
                "contact-" + UUID.randomUUID() + "@company.be", ADDRESS));
        parkingLot = parkingLotRepository.save(new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, 100,
                contactPerson, ADDRESS, division, new BigDecimal("2.50")));
    }

    private Credentials saveCredentials(Role role) {
        return credentialsRepository.save(new Credentials(
                role.name().toLowerCase(Locale.ROOT) + "-" + UUID.randomUUID() + "@parkshark.be", "hash", role));
    }

    private Member saveMember(MembershipLevel level) {
        return memberRepository.save(new Member("John", "Doe", ADDRESS, "0258748582",
                new LicensePlate("1-ABC-123", "BE"), LocalDate.of(2026, 1, 1), level, saveCredentials(Role.MEMBER)));
    }

    /** Stopping an allocation is story 08 (other team): put it in STOPPED directly in the database. */
    private UUID saveStoppedAllocation(Member member, LocalDateTime start, LocalDateTime end) {
        UUID id = saveOngoingAllocation(member, start);
        jdbcTemplate.update("update allocation set end_time = ?, status = 'STOPPED' where id = ?", end, id);
        return id;
    }

    private UUID saveOngoingAllocation(Member member, LocalDateTime start) {
        return allocationRepository.save(new Allocation(start, member, "1-ABC-123", parkingLot)).getId();
    }

    private String statusOf(UUID allocationId) {
        return jdbcTemplate.queryForObject("select status from allocation where id = ?", String.class, allocationId);
    }

    private ResultActions requestMonthlyInvoice(Member member) throws Exception {
        return mockMvc.perform(post("/invoices")
                .with(user(AuthenticatedUser.from(member.getCredentials())))
                .with(csrf()));
    }

    private Long createInvoice(Member member) throws Exception {
        String response = requestMonthlyInvoice(member)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.reference")).longValue();
    }

    @Nested
    @DisplayName("INV1 · Member gets their monthly invoice")
    class MonthlyInvoice {

        @Test
        void givenStoppedAllocations_whenRequestingInvoice_thenReturns201WithItemsAndTotal() throws Exception {
            Member member = saveMember(MembershipLevel.SILVER);
            UUID shortStay = saveStoppedAllocation(member, START, START.plusHours(2).plusMinutes(20));   // 3h
            UUID longStay = saveStoppedAllocation(member, START.plusDays(1), START.plusDays(1).plusHours(7)); // 7h, max 6h

            requestMonthlyInvoice(member)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reference").isNumber())
                    .andExpect(jsonPath("$.memberId").value(member.getId().toString()))
                    .andExpect(jsonPath("$.creationDate").value("2026-03-17"))
                    .andExpect(jsonPath("$.expirationDate").value("2026-04-16"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.paymentDate").isEmpty())
                    .andExpect(jsonPath("$.membershipMonthlyCost").value(10.0))
                    .andExpect(jsonPath("$.items.length()").value(2))
                    .andExpect(jsonPath("$.items[0].allocationId").value(shortStay.toString()))
                    .andExpect(jsonPath("$.items[0].hoursParked").value(3))
                    .andExpect(jsonPath("$.items[0].price").value(6.0))      // 3 × 2.50 − 20%
                    .andExpect(jsonPath("$.items[0].fine").value(0.0))
                    .andExpect(jsonPath("$.items[1].allocationId").value(longStay.toString()))
                    .andExpect(jsonPath("$.items[1].hoursParked").value(7))
                    .andExpect(jsonPath("$.items[1].price").value(14.0))     // 7 × 2.50 − 20%
                    .andExpect(jsonPath("$.items[1].fine").value(2.5))       // 1h above SILVER's 6h
                    .andExpect(jsonPath("$.total").value(32.5));             // 6 + 14 + 2.50 + 10

            assertThat(statusOf(shortStay)).isEqualTo("INVOICED");
            assertThat(statusOf(longStay)).isEqualTo("INVOICED");
        }

        @Test
        void givenOngoingAllocation_whenRequestingInvoice_thenItIsNotInvoiced() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            UUID ongoing = saveOngoingAllocation(member, START);

            requestMonthlyInvoice(member)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items.length()").value(0));

            assertThat(statusOf(ongoing)).isEqualTo("ONGOING");
        }

        @Test
        void givenNoStoppedAllocation_whenRequestingInvoice_thenInvoiceHasOnlyTheMonthlyCost() throws Exception {
            Member member = saveMember(MembershipLevel.GOLD);

            requestMonthlyInvoice(member)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items.length()").value(0))
                    .andExpect(jsonPath("$.total").value(40.0));
        }

        @Test
        void givenInvoiceAlreadyGeneratedThisMonth_whenRequestingAgain_thenReturns200WithTheSameInvoice() throws Exception {
            Member member = saveMember(MembershipLevel.SILVER);
            saveStoppedAllocation(member, START, START.plusHours(1));
            Long reference = createInvoice(member);
            UUID stoppedAfterwards = saveStoppedAllocation(member, START.plusDays(2), START.plusDays(2).plusHours(1));

            requestMonthlyInvoice(member)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reference").value(reference))
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.total").value(12.0));     // monthly cost charged once

            assertThat(statusOf(stoppedAfterwards)).isEqualTo("STOPPED");   // goes on next month's invoice
            assertThat(jdbcTemplate.queryForObject("select count(*) from invoice where member_id = ?",
                    Integer.class, member.getId())).isEqualTo(1);
        }

        @Test
        void givenNextMonth_whenRequestingInvoice_thenANewInvoiceIsGeneratedWithTheRemainingAllocations() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            saveStoppedAllocation(member, START, START.plusHours(1));
            Long marchReference = createInvoice(member);
            UUID stoppedAfterwards = saveStoppedAllocation(member, START.plusDays(5), START.plusDays(5).plusHours(2));

            clock.setTo(NOW.plusMonths(1));

            String response = requestMonthlyInvoice(member)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].allocationId").value(stoppedAfterwards.toString()))
                    .andReturn().getResponse().getContentAsString();

            assertThat(((Number) JsonPath.read(response, "$.reference")).longValue()).isNotEqualTo(marchReference);
            assertThat(statusOf(stoppedAfterwards)).isEqualTo("INVOICED");
        }

        @Test
        void givenAnotherMembersAllocation_whenRequestingInvoice_thenItIsNotOnMyInvoice() throws Exception {
            Member me = saveMember(MembershipLevel.BRONZE);
            Member other = saveMember(MembershipLevel.BRONZE);
            UUID othersAllocation = saveStoppedAllocation(other, START, START.plusHours(1));

            requestMonthlyInvoice(me)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.items.length()").value(0));

            assertThat(statusOf(othersAllocation)).isEqualTo("STOPPED");
        }

        @Test
        void givenManager_whenRequestingMonthlyInvoice_thenReturns403() throws Exception {
            mockMvc.perform(post("/invoices")
                            .with(user(AuthenticatedUser.from(saveCredentials(Role.MANAGER))))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void givenNoCredentials_whenRequestingMonthlyInvoice_thenReturns401() throws Exception {
            mockMvc.perform(post("/invoices").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("INV2 · Manager gets all invoices")
    class AllInvoices {

        @Test
        void givenInvoices_whenManagerGetsAll_thenReturns200WithThem() throws Exception {
            Member member = saveMember(MembershipLevel.SILVER);
            saveStoppedAllocation(member, START, START.plusHours(1));
            Long reference = createInvoice(member);

            String response = mockMvc.perform(get("/invoices")
                            .with(user(AuthenticatedUser.from(saveCredentials(Role.MANAGER)))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].reference", hasItem(reference.intValue())))
                    .andReturn().getResponse().getContentAsString();

            List<Map<String, Object>> mine = JsonPath.read(response, "$[?(@.reference == %d)]".formatted(reference));
            assertThat(mine).singleElement().satisfies(invoice -> {
                assertThat(invoice).containsEntry("status", "OPEN");
                assertThat(invoice).containsEntry("memberName", "John Doe");
                assertThat((List<?>) invoice.get("items")).hasSize(1);
            });
        }

        @Test
        void givenMember_whenGettingAllInvoices_thenReturns403() throws Exception {
            mockMvc.perform(get("/invoices")
                            .with(user(AuthenticatedUser.from(saveMember(MembershipLevel.BRONZE).getCredentials()))))
                    .andExpect(status().isForbidden());
        }

        @Test
        void givenNoCredentials_whenGettingAllInvoices_thenReturns401() throws Exception {
            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("INV3 · Manager closes an invoice")
    class CloseInvoice {

        private ResultActions close(Object reference, Credentials credentials) throws Exception {
            return mockMvc.perform(post("/invoices/{reference}/close", reference)
                    .with(user(AuthenticatedUser.from(credentials)))
                    .with(csrf()));
        }

        @Test
        void givenOpenInvoice_whenManagerClosesIt_thenReturns200WithStatusClosedAndPaymentDate() throws Exception {
            Long reference = createInvoice(saveMember(MembershipLevel.BRONZE));
            clock.setTo(NOW.plusDays(4));

            close(reference, saveCredentials(Role.MANAGER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reference").value(reference))
                    .andExpect(jsonPath("$.status").value("CLOSED"))
                    .andExpect(jsonPath("$.paymentDate").value("2026-03-21"));

            assertThat(jdbcTemplate.queryForObject("select status from invoice where reference = ?",
                    String.class, reference)).isEqualTo("CLOSED");
        }

        @Test
        void givenClosedInvoice_whenClosingAgain_thenReturns409() throws Exception {
            Long reference = createInvoice(saveMember(MembershipLevel.BRONZE));
            Credentials manager = saveCredentials(Role.MANAGER);
            close(reference, manager).andExpect(status().isOk());

            close(reference, manager).andExpect(status().isConflict());
        }

        @Test
        void givenUnknownReference_whenClosing_thenReturns404() throws Exception {
            close(Long.MAX_VALUE, saveCredentials(Role.MANAGER)).andExpect(status().isNotFound());
        }

        @Test
        void givenReferenceThatIsNotANumber_whenClosing_thenReturns400() throws Exception {
            close("abc", saveCredentials(Role.MANAGER)).andExpect(status().isBadRequest());
        }

        @Test
        void givenMember_whenClosingInvoice_thenReturns403() throws Exception {
            Member member = saveMember(MembershipLevel.BRONZE);
            Long reference = createInvoice(member);

            close(reference, member.getCredentials()).andExpect(status().isForbidden());
        }
    }
}
