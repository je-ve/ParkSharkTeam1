package com.switchfully.spring.parkshark.feature.member.service;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.member.api.LicensePlateMapper;
import com.switchfully.spring.parkshark.feature.member.api.MemberMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.LicensePlateDto;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDetailsDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.member.exception.MemberNotFoundException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        MemberDetailsDto member = memberService.registerMember(request);

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

        MemberDetailsDto member = memberService.registerMember(request);

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

    @Test
    void givenSavedMembers_whenGettingAllMembers_thenReturnsTheOverviewOfEachMember() {
        when(memberRepository.findAllWithCredentials()).thenReturn(List.of(
                member("John", "john@doe.be", "1-ABC-123"),
                member("Jane", "jane@doe.be", "2-DEF-456")));

        List<MemberDto> members = memberService.getAllMembers();

        assertThat(members).hasSize(2);
        assertThat(members.get(0).firstName()).isEqualTo("John");
        assertThat(members.get(0).lastName()).isEqualTo("Doe");
        assertThat(members.get(0).licensePlateNumber()).isEqualTo("1-ABC-123");
        assertThat(members.get(0).telephone()).isEqualTo("+32 470 12 34 56");
        assertThat(members.get(0).email()).isEqualTo("john@doe.be");
        assertThat(members.get(0).registrationDate()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(members).extracting(MemberDto::email).containsExactly("john@doe.be", "jane@doe.be");
    }

    @Test
    void givenNoMembers_whenGettingAllMembers_thenReturnsAnEmptyList() {
        when(memberRepository.findAllWithCredentials()).thenReturn(List.of());

        List<MemberDto> members = memberService.getAllMembers();

        assertThat(members).isEmpty();
    }

    @Test
    void givenAnExistingId_whenGettingOneMember_thenReturnsAllTheirData() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(id)).thenReturn(Optional.of(member("John", "john@doe.be", "1-ABC-123")));

        MemberDetailsDto member = memberService.getMember(id);

        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.address().city()).isEqualTo("Antwerp");
        assertThat(member.licensePlate().number()).isEqualTo("1-ABC-123");
        assertThat(member.licensePlate().issuingCountry()).isEqualTo("BE");
        assertThat(member.membershipLevel()).isEqualTo(MembershipLevel.SILVER);
        assertThat(member.email()).isEqualTo("john@doe.be");
    }

    @Test
    void givenAnUnknownId_whenGettingOneMember_thenThrowsMemberNotFound() {
        UUID id = UUID.randomUUID();
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(id))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessage("Member with id " + id + " was not found");
    }

    private static Member member(String firstName, String email, String licensePlateNumber) {
        return new Member(
                firstName,
                "Doe",
                new Address("Meir", "10a", "2000", "Antwerp"),
                "+32 470 12 34 56",
                new LicensePlate(licensePlateNumber, "BE"),
                LocalDate.of(2026, 10, 30),
                MembershipLevel.SILVER,
                new Credentials(email, "hash", Role.MEMBER));
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
