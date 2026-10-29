package com.switchfully.spring.parkshark.feature.member.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemberTest {

    @Test
    void GivenNoMembershipLevel_WhenCreatingMember_ThenMembershipLevelIsBronze() {
        //Given
        Member member = new Member("John", "Doe",
                new Address("Meir", "1", "2000", "Antwerp"),
                "0258748582",
                new LicensePlate("1-AAA-111", "BE"),
                LocalDate.now(),
                null,
                new Credentials("john@doe.com", "azerty", Role.MEMBER)
        );
        ///Then
        assertEquals(MembershipLevel.BRONZE, member.getMembershipLevel());
    }
}

