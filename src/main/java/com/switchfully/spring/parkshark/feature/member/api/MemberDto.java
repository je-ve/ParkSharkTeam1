package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;

import java.time.LocalDate;
import java.util.UUID;


public record MemberDto(
        UUID id,
        String firstName,
        String lastName,
        AddressDto address,
        String telephone,
        LicensePlateDto licensePlate,
        LocalDate registrationDate,
        MembershipLevel membershipLevel,
        String email) {
}
