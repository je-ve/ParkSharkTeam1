package com.switchfully.spring.parkshark.feature.allocation.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationDto(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime stopTime,
        String licensePlateNumber,
        String status
) {
}
