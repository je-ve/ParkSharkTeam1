package com.switchfully.spring.parkshark.feature.allocation.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 1, 1, 9, 0);

    private final Member member = new Member("John", "Doe", ADDRESS, "0258748582",
            new LicensePlate("1-ABC-123", "BE"), LocalDate.of(2026, 1, 1), MembershipLevel.BRONZE,
            new Credentials("john@doe.com", "hash", Role.MEMBER));

    private final ParkingLot parkingLot = new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, 250,
            new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "jane@company.be", ADDRESS),
            ADDRESS, new Division("MyCompany", "MyOldCompany", "MyBoss"), new BigDecimal("2.50"));

    @Test
    void givenValidData_whenCreatingAllocation_thenFieldsAreSet() {
        Allocation allocation = new Allocation(START_TIME, member, "1-ABC-123", parkingLot);

        assertThat(allocation.getStartTime()).isEqualTo(START_TIME);
        assertThat(allocation.getMember()).isSameAs(member);
        assertThat(allocation.getLicensePlateNumber()).isEqualTo("1-ABC-123");
        assertThat(allocation.getParkingLot()).isSameAs(parkingLot);
    }

    @Test
    void givenNewAllocation_whenCreatingAllocation_thenStatusIsOngoing() {
        Allocation allocation = new Allocation(START_TIME, member, "1-ABC-123", parkingLot);

        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.ONGOING);
    }

    @Test
    void givenNewAllocation_whenCreatingAllocation_thenItHasNoEndTime() {
        Allocation allocation = new Allocation(START_TIME, member, "1-ABC-123", parkingLot);

        assertThat(allocation.getEndTime()).isNull();
    }

    @Test
    void givenAnotherPlateThanTheMembers_whenCreatingAllocation_thenTheGivenPlateIsKept() {
        Allocation allocation = new Allocation(START_TIME, member, "2-XYZ-999", parkingLot);

        assertThat(allocation.getLicensePlateNumber()).isEqualTo("2-XYZ-999");
        assertThat(member.getLicensePlate().getNumber()).isEqualTo("1-ABC-123");
    }
}