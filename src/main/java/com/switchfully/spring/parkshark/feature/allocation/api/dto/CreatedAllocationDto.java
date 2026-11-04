package com.switchfully.spring.parkshark.feature.allocation.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedAllocationDto(
        UUID allocationId,
        LocalDateTime startTime
) {
}
