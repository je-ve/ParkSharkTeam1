package com.switchfully.spring.parkshark.feature.member.service;

import com.switchfully.spring.parkshark.feature.member.api.MemberMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDetailsDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.exception.MemberNotFoundException;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.security.service.CredentialsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final CredentialsService credentialsService;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper, CredentialsService credentialsService, Clock clock) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.credentialsService = credentialsService;
        this.clock = clock;
    }

    public MemberDetailsDto registerMember(RegisterMemberRequest request) {
        Credentials credentials = credentialsService.create(
                request.email(),
                request.password(),
                Role.MEMBER);
        Member createdMember = memberRepository.save(memberMapper.toMember(request, credentials, LocalDate.now(clock)));
        return memberMapper.toDetailsDto(createdMember);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> getAllMembers() {
        return memberMapper.toDto(memberRepository.findAllWithCredentials());
    }

    @Transactional(readOnly = true)
    public MemberDetailsDto getMember(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
        return memberMapper.toDetailsDto(member);
    }
}
