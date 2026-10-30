package com.switchfully.spring.parkshark.feature.member.domain;

import java.math.BigDecimal;
import java.time.Duration;

public enum MembershipLevel {
    BRONZE(new BigDecimal("0"), 0, Duration.ofHours(4)),
    SILVER(new BigDecimal("10"), 20, Duration.ofHours(6)),
    GOLD(new BigDecimal("40"), 30, Duration.ofHours(24));

    private final BigDecimal monthlyCost;
    private final int reductionPercentage;
    private final Duration maxAllocationTime;

    MembershipLevel(BigDecimal monthlyCost, int reductionPercentage, Duration maxAllocationTime) {
        this.monthlyCost = monthlyCost;
        this.reductionPercentage = reductionPercentage;
        this.maxAllocationTime = maxAllocationTime;
    }

    public BigDecimal getMonthlyCost() {
        return monthlyCost;
    }

    public int getReductionPercentage() {
        return reductionPercentage;
    }

    public Duration getMaxAllocationTime() {
        return maxAllocationTime;
    }
}
