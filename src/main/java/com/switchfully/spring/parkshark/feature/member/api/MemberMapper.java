package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.api.request.RegisterMemberRequest;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MemberMapper {
    private final AddressMapper addressMapper;
    private final LicensePlateMapper licensePlateMapper;

    public MemberMapper(AddressMapper addressMapper, LicensePlateMapper licensePlateMapper) {
        this.addressMapper = addressMapper;
        this.licensePlateMapper = licensePlateMapper;
    }

    public MemberDto toDto(Member member) {
        return new MemberDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                addressMapper.toDto(member.getAddress()),
                member.getTelephone(),
                licensePlateMapper.toDto(member.getLicensePlate()),
                member.getRegistrationDate(),
                member.getMembershipLevel(),
                member.getCredentials().getEmail()
        );
    }

    public Member toMember(RegisterMemberRequest request, Credentials credentials, LocalDate registrationDate) {
        return new Member(
                request.firstName(),
                request.lastName(),
                addressMapper.toEntity(request.address()),
                request.telephone(),
                licensePlateMapper.toEntity(request.licensePlate()),
                registrationDate,
                request.membershipLevel(),
                credentials
        );
    }
}
