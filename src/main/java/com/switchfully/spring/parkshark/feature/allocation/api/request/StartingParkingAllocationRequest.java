package com.switchfully.spring.parkshark.feature.allocation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StartingParkingAllocationRequest(
        @NotBlank(message = "License plate is required")
        @Size(max = 20, message = "License plate must be at most 20 characters")
        String licencePlate,

        @NotNull(message = "Parking lot ID is required")
        UUID parkingLotId
) {}
