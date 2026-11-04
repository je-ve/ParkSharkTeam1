package com.switchfully.spring.parkshark.feature.parkinglot.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParkingLotTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final Division DIVISION = new Division("MyCompany", "MyOldCompany", "MyBoss");

    private static ContactPerson contact(String mobilePhone, String telephoneNumber, String email) {
        return new ContactPerson("Jane Doe", mobilePhone, telephoneNumber, email, ADDRESS);
    }

    private static ParkingLot parkingLot(int capacity, BigDecimal pricePerHour) {
        return new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, capacity,
                contact("+32 470 12 34 56", null, "jane@company.be"), ADDRESS, DIVISION, pricePerHour);
    }

    @Test
    void givenValidData_whenCreatingParkingLot_thenFieldsAreSet() {
        ParkingLot parkingLot = parkingLot(250, new BigDecimal("2.50"));

        assertThat(parkingLot.getName()).isEqualTo("Meir Underground");
        assertThat(parkingLot.getCategory()).isEqualTo(Category.UNDERGROUND_BUILDING);
        assertThat(parkingLot.getCapacity()).isEqualTo(250);
        assertThat(parkingLot.getPricePerHour()).isEqualByComparingTo("2.50");
        assertThat(parkingLot.getDivision()).isSameAs(DIVISION);
    }

    @Test
    void givenZeroCapacity_whenCreatingParkingLot_thenThrows() {
        assertThatThrownBy(() -> parkingLot(0, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenNegativePrice_whenCreatingParkingLot_thenThrows() {
        assertThatThrownBy(() -> parkingLot(10, new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenFreeParking_whenCreatingParkingLot_thenItIsAllowed() {
        assertThat(parkingLot(10, BigDecimal.ZERO).getPricePerHour()).isZero();
    }

    @Test
    void givenNoPhoneNumber_whenCreatingContactPerson_thenThrows() {
        assertThatThrownBy(() -> contact(" ", null, "jane@company.be"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenOnlyTelephoneNumber_whenCreatingContactPerson_thenBlankMobileBecomesNull() {
        ContactPerson contactPerson = contact("", "+32 3 123 45 67", "jane@company.be");

        assertThat(contactPerson.getMobilePhone()).isNull();
        assertThat(contactPerson.getTelephoneNumber()).isEqualTo("+32 3 123 45 67");
    }

    @Test
    void givenEmailWithCapitalsAndSpaces_whenCreatingContactPerson_thenEmailIsNormalized() {
        assertThat(contact("+32 470 12 34 56", null, "  Jane@Company.BE ").getEmail())
                .isEqualTo("jane@company.be");
    }
}
