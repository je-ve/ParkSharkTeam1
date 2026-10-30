package com.switchfully.spring.parkshark.feature.member.service;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.member.api.LicensePlateMapper;
import com.switchfully.spring.parkshark.feature.member.api.MemberMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.LicensePlateDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.security.exception.EmailAlreadyUsedException;
import com.switchfully.spring.parkshark.feature.security.service.CredentialsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-30T09:00:00Z"), ZoneId.of("Europe/Brussels"));

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CredentialsService credentialsService;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MemberMapper memberMapper = new MemberMapper(new AddressMapper(), new LicensePlateMapper());
        memberService = new MemberService(memberRepository, memberMapper, credentialsService, CLOCK);
    }

    @Test
    void givenAValidRequest_whenRegisteringAMember_thenSavesTheMemberWithTodayAsRegistrationDate() {
        RegisterMemberRequest request = request(MembershipLevel.GOLD);
        when(credentialsService.create("john@doe.be", "secret123", Role.MEMBER))
                .thenReturn(new Credentials("john@doe.be", "hash", Role.MEMBER));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberDto member = memberService.registerMember(request);

        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.lastName()).isEqualTo("Doe");
        assertThat(member.address().city()).isEqualTo("Antwerp");
        assertThat(member.licensePlate().number()).isEqualTo("1-ABC-123");
        assertThat(member.registrationDate()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(member.membershipLevel()).isEqualTo(MembershipLevel.GOLD);
        assertThat(member.email()).isEqualTo("john@doe.be");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void givenNoMembershipLevel_whenRegisteringAMember_thenTheMemberIsBronze() {
        RegisterMemberRequest request = request(null);
        when(credentialsService.create("john@doe.be", "secret123", Role.MEMBER))
                .thenReturn(new Credentials("john@doe.be", "hash", Role.MEMBER));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberDto member = memberService.registerMember(request);

        assertThat(member.membershipLevel()).isEqualTo(MembershipLevel.BRONZE);
    }

    @Test
    void givenAnEmailAlreadyInUse_whenRegisteringAMember_thenThrowsAndSavesNoMember() {
        RegisterMemberRequest request = request(MembershipLevel.GOLD);
        when(credentialsService.create("john@doe.be", "secret123", Role.MEMBER))
                .thenThrow(new EmailAlreadyUsedException("john@doe.be"));

        assertThatThrownBy(() -> memberService.registerMember(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessage("Email john@doe.be is already in use");

        verify(memberRepository, never()).save(any());
    }

    private static RegisterMemberRequest request(MembershipLevel membershipLevel) {
        return new RegisterMemberRequest(
                "John",
                "Doe",
                new AddressDto("Meir", "10a", "2000", "Antwerp"),
                "+32 470 12 34 56",
                new LicensePlateDto("1-ABC-123", "BE"),
                membershipLevel,
                "john@doe.be",
                "secret123");
    }
}
