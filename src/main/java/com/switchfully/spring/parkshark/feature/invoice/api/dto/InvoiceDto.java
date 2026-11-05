package com.switchfully.spring.parkshark.feature.invoice.api.dto;

import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceDto(
        Long reference,
        UUID memberId,
        String memberName,
        LocalDate creationDate,
        LocalDate expirationDate,
        InvoiceStatus status,
        LocalDate paymentDate,
        BigDecimal membershipMonthlyCost,
        List<InvoiceItemDto> items,
        BigDecimal total) {
}
