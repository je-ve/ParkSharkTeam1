package com.switchfully.spring.parkshark.feature.member.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;


public class MembershipLevelTest {

    @ParameterizedTest
    @CsvSource({"GOLD, 40,30,PT24H", "SILVER, 10,20,PT6H", "BRONZE, 0,0,PT4H"})
    void givenGoldMembershipLevel_WhenReadingValues_ThenCost40Reduction30Duration24h(
            MembershipLevel membershipLevel,
            BigDecimal montlyCost,
            int reductionPercentage,
            Duration maxAllocationTime
    ) {
        assertThat(membershipLevel.getMonthlyCost().equals(montlyCost));
        assertThat(membershipLevel.getReductionPercentage() == reductionPercentage);
        assertThat(membershipLevel.getMaxAllocationTime().equals(maxAllocationTime));
    }
}
