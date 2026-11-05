package com.switchfully.spring.parkshark.feature.invoice.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceItemDto(
        UUID allocationId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int hoursParked,
        BigDecimal price,
        BigDecimal fine,
        BigDecimal total) {
}
