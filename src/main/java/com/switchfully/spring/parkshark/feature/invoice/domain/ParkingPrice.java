package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;

/**
 * Price of one parking allocation (INV1).
 * <ul>
 *     <li>hours parked: started hours, rounded up (2h20 → 3h)</li>
 *     <li>price: hours × price per hour, minus the membership reduction</li>
 *     <li>fine: €2.50 for every hour above the membership's maximum allocation time, without reduction</li>
 * </ul>
 * Assumptions to confirm with the analyst: the fine only counts the hours above the maximum,
 * and the reduction does not apply to the fine.
 */
public record ParkingPrice(int hoursParked, BigDecimal price, BigDecimal fine) {

    public static final BigDecimal FINE_PER_HOUR = new BigDecimal("2.50");
    private static final long SECONDS_PER_HOUR = 3600;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static ParkingPrice calculate(Duration parked, BigDecimal pricePerHour, MembershipLevel level) {
        Objects.requireNonNull(parked);
        Objects.requireNonNull(pricePerHour);
        Objects.requireNonNull(level);
        if (parked.isNegative()) {
            throw new IllegalArgumentException("Parked duration cannot be negative");
        }

        int hours = Math.toIntExact((parked.getSeconds() + SECONDS_PER_HOUR - 1) / SECONDS_PER_HOUR);

        BigDecimal reductionFactor = HUNDRED.subtract(BigDecimal.valueOf(level.getReductionPercentage()))
                .divide(HUNDRED);
        BigDecimal price = pricePerHour.multiply(BigDecimal.valueOf(hours))
                .multiply(reductionFactor)
                .setScale(2, RoundingMode.HALF_UP);

        long hoursAboveMaximum = Math.max(0, hours - level.getMaxAllocationTime().toHours());
        BigDecimal fine = FINE_PER_HOUR.multiply(BigDecimal.valueOf(hoursAboveMaximum))
                .setScale(2, RoundingMode.HALF_UP);

        return new ParkingPrice(hours, price, fine);
    }
}
