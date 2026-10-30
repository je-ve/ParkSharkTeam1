package com.switchfully.spring.parkshark.feature.member.service;

import com.switchfully.spring.parkshark.feature.member.api.MemberMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import com.switchfully.spring.parkshark.feature.security.service.CredentialsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {
    private MemberRepository memberRepository;
    private MemberMapper memberMapper;
    private CredentialsService credentialsService;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper, CredentialsService credentialsService) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.credentialsService = credentialsService;
    }

    public MemberDto RegisterUser(RegisterMemberRequest registerMemberDto) {
        Credentials credentials = credentialsService.create(
                registerMemberDto.email(),
                registerMemberDto.password(),
                Role.MEMBER);
        return memberMapper.toDto(memberMapper.toMember(registerMemberDto, credentials));
    }
}
