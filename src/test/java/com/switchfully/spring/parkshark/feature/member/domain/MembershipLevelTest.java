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
            BigDecimal monthlyCost,
            int reductionPercentage,
            Duration maxAllocationTime
    ) {
        assertThat(membershipLevel.getMonthlyCost()).isEqualByComparingTo(monthlyCost);
        assertThat(membershipLevel.getReductionPercentage()).isEqualTo(reductionPercentage);
        assertThat(membershipLevel.getMaxAllocationTime()).isEqualTo(maxAllocationTime);
    }
}
