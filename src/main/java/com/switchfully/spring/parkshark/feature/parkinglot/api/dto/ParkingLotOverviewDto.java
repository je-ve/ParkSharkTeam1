package com.switchfully.spring.parkshark.feature.parkinglot.api.dto;

import java.util.UUID;

public record ParkingLotOverviewDto(
        // id, name, capacity and the contact person's email + telephone (nothing else)
        UUID id,
        String name,
        int capacity,
        String contactEmail,
        String contactMobilePhone,
        String contactTelephoneNumber
) {
}
