package com.switchfully.spring.parkshark.feature.allocation.api.request;

import java.util.UUID;

public record StartingParkingAllocationRequest(
        UUID memberId,
        String licencePlate,
        UUID parkingLotId
) {}
