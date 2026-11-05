package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParkingPriceTest {

    @ParameterizedTest(name = "{0} min at €{1}/h as {2} → {3}h, price €{4}, fine €{5}")
    @CsvSource({
            // minutes, price/h, level,  hours, price,  fine
            "0,         2.50,    BRONZE, 0,     0.00,   0.00",
            "1,         2.50,    BRONZE, 1,     2.50,   0.00",
            "60,        2.50,    BRONZE, 1,     2.50,   0.00",
            "61,        2.50,    BRONZE, 2,     5.00,   0.00",
            "140,       2.50,    BRONZE, 3,     7.50,   0.00",   // 2h20 → 3h
            "140,       2.50,    SILVER, 3,     6.00,   0.00",   // -20%
            "140,       2.50,    GOLD,   3,     5.25,   0.00",   // -30%
            "240,       2.50,    BRONZE, 4,     10.00,  0.00",   // exactly the BRONZE maximum: no fine
            "241,       2.50,    BRONZE, 5,     12.50,  2.50",   // 1h above the maximum
            "330,       2.50,    BRONZE, 6,     15.00,  5.00",   // 5h30 → 6h, 2h above the maximum
            "420,       2.50,    SILVER, 7,     14.00,  2.50",   // SILVER max 6h, fine not reduced
            "1500,      2.50,    GOLD,   25,    43.75,  2.50",   // GOLD max 24h
            "90,        3.33,    SILVER, 2,     5.33,   0.00"    // 5.328 rounded half up
    })
    void calculatesHoursPriceAndFine(long minutes, BigDecimal pricePerHour, MembershipLevel level,
                                     int expectedHours, BigDecimal expectedPrice, BigDecimal expectedFine) {
        ParkingPrice parkingPrice = ParkingPrice.calculate(Duration.ofMinutes(minutes), pricePerHour, level);

        assertThat(parkingPrice.hoursParked()).isEqualTo(expectedHours);
        assertThat(parkingPrice.price()).isEqualByComparingTo(expectedPrice);
        assertThat(parkingPrice.fine()).isEqualByComparingTo(expectedFine);
    }

    @Test
    void givenOneSecondOverAnHour_whenCalculating_thenTheNextHourIsCharged() {
        ParkingPrice parkingPrice = ParkingPrice.calculate(Duration.ofHours(1).plusSeconds(1),
                new BigDecimal("2.50"), MembershipLevel.BRONZE);

        assertThat(parkingPrice.hoursParked()).isEqualTo(2);
    }

    @Test
    void givenAmounts_whenCalculating_thenTheyHaveTwoDecimals() {
        ParkingPrice parkingPrice = ParkingPrice.calculate(Duration.ofHours(1), new BigDecimal("2"), MembershipLevel.BRONZE);

        assertThat(parkingPrice.price().scale()).isEqualTo(2);
        assertThat(parkingPrice.fine().scale()).isEqualTo(2);
    }

    @Test
    void givenNegativeDuration_whenCalculating_thenThrows() {
        assertThatThrownBy(() -> ParkingPrice.calculate(Duration.ofMinutes(-1), BigDecimal.ONE, MembershipLevel.BRONZE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
