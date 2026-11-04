package com.switchfully.spring.parkshark.feature.parkinglot.api.dto;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;

import java.util.UUID;

public record ContactPersonDto(
        UUID id,
        String name,
        String mobilePhone,
        String telephoneNumber,
        String email,
        AddressDto address) {
}
