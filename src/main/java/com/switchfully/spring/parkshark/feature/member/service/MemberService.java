package com.switchfully.spring.parkshark.feature.member.service;

import com.switchfully.spring.parkshark.feature.member.api.MemberMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.security.service.CredentialsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional
public class MemberService {
    private MemberRepository memberRepository;
    private MemberMapper memberMapper;
    private CredentialsService credentialsService;
    private Clock clock;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper, CredentialsService credentialsService, Clock clock) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.credentialsService = credentialsService;
        this.clock = clock;
    }

    public MemberDto RegisterUser(RegisterMemberRequest registerMemberRequest) {
        Credentials credentials = credentialsService.create(
                registerMemberRequest.email(),
                registerMemberRequest.password(),
                Role.MEMBER);
        Member createdMember = memberRepository.save(memberMapper.toMember(registerMemberRequest, credentials, LocalDate.now(clock)));
        return memberMapper.toDto(createdMember);
    }
}
