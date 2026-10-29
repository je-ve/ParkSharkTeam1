package com.switchfully.spring.parkshark.feature.member.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MemberTest {

    @Test
    void GivenNoMembershipLevel_WhenCreatingMember_ThenMembershipLevelIsGold() {
        //Given
        Member member = new Member("Doe", "John",
                new Address("Meir", "1", "2000", "Antwerp"),
                "0258748582",
                new LicensePlate("1-AAA-111", "BE"),
                LocalDate.now(),
                null,
                new Credentials("john@doe.com", "azerty", Role.MEMBER)
        );
        ///Then
        assertEquals(member.getMembershipLevel(), MembershipLevel.BRONZE);

    }
}

