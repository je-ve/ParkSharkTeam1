package com.switchfully.spring.parkshark.feature.parkinglot.api.dto;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;

import java.math.BigDecimal;
import java.util.UUID;

public record ParkingLotDto(
        UUID id,
        String name,
        Category category,
        int capacity,
        AddressDto address,
        BigDecimal pricePerHour,
        ContactPersonDto contactPerson,
        UUID divisionId) {
}
