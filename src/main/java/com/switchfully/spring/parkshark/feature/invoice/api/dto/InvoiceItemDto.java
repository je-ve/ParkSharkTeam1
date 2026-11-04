package com.switchfully.spring.parkshark.feature.invoice.api.dto;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.invoice.domain.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceItemDto {
    private UUID id;
    private Invoice invoice;
    private Allocation allocation;
    private int hoursParked;
    private BigDecimal price;
    private BigDecimal fine;


}
