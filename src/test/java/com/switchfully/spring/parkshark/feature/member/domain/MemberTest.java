package com.switchfully.spring.parkshark.feature.member.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    void givenNoMembershipLevel_whenCreatingMember_thenMembershipLevelIsBronze() {
        Member member = new Member("John", "Doe",
                new Address("Meir", "1", "2000", "Antwerp"),
                "0258748582",
                new LicensePlate("1-AAA-111", "BE"),
                LocalDate.of(2026, 10, 29),
                null,
                new Credentials("john@doe.com", "hash", Role.MEMBER));

        assertThat(member.getMembershipLevel()).isEqualTo(MembershipLevel.BRONZE);
    }
}
