package com.switchfully.spring.parkshark.feature.allocation.api.request;

import java.util.UUID;

public record StopParkingAllocationRequest(
        UUID allocationId
) {
}
