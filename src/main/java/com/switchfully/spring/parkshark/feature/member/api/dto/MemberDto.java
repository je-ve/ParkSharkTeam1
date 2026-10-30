package com.switchfully.spring.parkshark.feature.member.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MemberDto(
        UUID id,
        String firstName,
        String lastName,
        String licensePlateNumber,
        String telephone,
        String email,
        LocalDate registrationDate) {
}
