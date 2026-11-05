package com.switchfully.spring.parkshark.feature.allocation.api.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StopParkingAllocationRequest(
        @NotNull(message = "Allocation ID is required")
        UUID allocationId
) {
}
