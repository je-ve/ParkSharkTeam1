package com.switchfully.spring.parkshark.feature.member.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipLevelTest {

    @ParameterizedTest(name = "{0}: monthly cost {1}, reduction {2}%, max allocation {3}")
    @CsvSource({"GOLD, 40, 30, PT24H", "SILVER, 10, 20, PT6H", "BRONZE, 0, 0, PT4H"})
    void givenAMembershipLevel_whenReadingItsValues_thenMatchesTheMembershipRules(
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
