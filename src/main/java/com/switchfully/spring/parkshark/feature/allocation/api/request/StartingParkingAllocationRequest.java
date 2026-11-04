package com.switchfully.spring.parkshark.feature.allocation.api.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record StartingParkingAllocationRequest(
        @NotBlank(message = "Member ID is required")
        UUID memberId,

        @NotBlank(message = "License plate is required")
        String licencePlate,

        @NotBlank (message = "Parking lot ID is required")
        UUID parkingLotId
) {}
