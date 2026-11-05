package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.invoice.exception.InvoiceAlreadyClosedException;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 17);
    private static final LocalDateTime START = LocalDateTime.of(2026, 3, 10, 9, 0);

    private final ParkingLot parkingLot = new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, 250,
            new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "jane@company.be", ADDRESS),
            ADDRESS, new Division("MyCompany", "MyOldCompany", "MyBoss"), new BigDecimal("2.50"));

    private static Member member(MembershipLevel level) {
        return new Member("John", "Doe", ADDRESS, "0258748582", new LicensePlate("1-ABC-123", "BE"),
                LocalDate.of(2026, 1, 1), level, new Credentials("john@doe.com", "hash", Role.MEMBER));
    }

    private Allocation allocation(Member member, AllocationStatus status, LocalDateTime endTime) {
        Allocation allocation = new Allocation(START, member, "1-ABC-123", parkingLot);
        // Stopping is story 08; set the state directly so this test does not depend on it.
        ReflectionTestUtils.setField(allocation, "status", status);
        ReflectionTestUtils.setField(allocation, "endTime", endTime);
        return allocation;
    }

    private static ParkingPrice price(String price, String fine) {
        return new ParkingPrice(3, new BigDecimal(price), new BigDecimal(fine));
    }

    @Test
    void givenNewInvoice_thenItIsOpenForTheCurrentMonthWithTheMembershipCost() {
        Invoice invoice = new Invoice(member(MembershipLevel.SILVER), TODAY, TODAY.plusDays(30));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.getBillingMonth()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(invoice.getPaymentDate()).isNull();
        assertThat(invoice.getMembershipMonthlyCost()).isEqualByComparingTo("10");
        assertThat(invoice.getItems()).isEmpty();
        assertThat(invoice.getTotal()).isEqualByComparingTo("10");
    }

    @Test
    void givenExpirationBeforeCreation_whenCreatingInvoice_thenThrows() {
        assertThatThrownBy(() -> new Invoice(member(MembershipLevel.BRONZE), TODAY, TODAY.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenStoppedAllocations_whenAddingItems_thenTotalIsItemsPlusFinesPlusMonthlyCost() {
        Member member = member(MembershipLevel.SILVER);
        Invoice invoice = new Invoice(member, TODAY, TODAY.plusDays(30));

        invoice.addItem(allocation(member, AllocationStatus.STOPPED, START.plusHours(2)), price("6.00", "0.00"));
        invoice.addItem(allocation(member, AllocationStatus.STOPPED, START.plusHours(7)), price("14.00", "2.50"));

        assertThat(invoice.getItems()).hasSize(2);
        assertThat(invoice.getTotal()).isEqualByComparingTo("32.50");   // 6 + 14 + 2.50 + 10
    }

    @Test
    void givenStoppedAllocation_whenAddingItem_thenAllocationIsInvoiced() {
        Member member = member(MembershipLevel.BRONZE);
        Allocation allocation = allocation(member, AllocationStatus.STOPPED, START.plusHours(1));

        new Invoice(member, TODAY, TODAY.plusDays(30)).addItem(allocation, price("2.50", "0.00"));

        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.INVOICED);
    }

    @Test
    void givenOngoingAllocation_whenAddingItem_thenThrowsAndAddsNothing() {
        Member member = member(MembershipLevel.BRONZE);
        Invoice invoice = new Invoice(member, TODAY, TODAY.plusDays(30));
        Allocation ongoing = new Allocation(START, member, "1-ABC-123", parkingLot);

        assertThatThrownBy(() -> invoice.addItem(ongoing, price("2.50", "0.00")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(invoice.getItems()).isEmpty();
        assertThat(ongoing.getStatus()).isEqualTo(AllocationStatus.ONGOING);
    }

    @Test
    void givenAlreadyInvoicedAllocation_whenAddingItem_thenThrows() {
        Member member = member(MembershipLevel.BRONZE);
        Allocation invoiced = allocation(member, AllocationStatus.INVOICED, START.plusHours(1));

        assertThatThrownBy(() -> new Invoice(member, TODAY, TODAY.plusDays(30)).addItem(invoiced, price("2.50", "0.00")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenOpenInvoice_whenClosing_thenItIsClosedWithPaymentDate() {
        Invoice invoice = new Invoice(member(MembershipLevel.BRONZE), TODAY, TODAY.plusDays(30));

        invoice.close(TODAY.plusDays(5));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CLOSED);
        assertThat(invoice.getPaymentDate()).isEqualTo(TODAY.plusDays(5));
    }

    @Test
    void givenClosedInvoice_whenClosingAgain_thenThrowsAndKeepsTheFirstPaymentDate() {
        Invoice invoice = new Invoice(member(MembershipLevel.BRONZE), TODAY, TODAY.plusDays(30));
        invoice.close(TODAY.plusDays(5));

        assertThatThrownBy(() -> invoice.close(TODAY.plusDays(9)))
                .isInstanceOf(InvoiceAlreadyClosedException.class);
        assertThat(invoice.getPaymentDate()).isEqualTo(TODAY.plusDays(5));
    }

    @Test
    void givenNegativeAmounts_whenCreatingItem_thenThrows() {
        Member member = member(MembershipLevel.BRONZE);
        Invoice invoice = new Invoice(member, TODAY, TODAY.plusDays(30));
        Allocation allocation = allocation(member, AllocationStatus.STOPPED, START.plusHours(1));

        assertThatThrownBy(() -> new InvoiceItem(invoice, allocation, 1, new BigDecimal("-1"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InvoiceItem(invoice, allocation, -1, BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNotStoppedAllocation_whenGettingParkedDuration_thenThrows() {
        Allocation ongoing = new Allocation(START, member(MembershipLevel.BRONZE), "1-ABC-123", parkingLot);

        assertThatThrownBy(ongoing::getParkedDuration).isInstanceOf(IllegalStateException.class);
    }
}
