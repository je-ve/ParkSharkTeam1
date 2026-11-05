package com.switchfully.spring.parkshark.feature.invoice.api;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.invoice.api.dto.InvoiceDto;
import com.switchfully.spring.parkshark.feature.invoice.api.dto.InvoiceItemDto;
import com.switchfully.spring.parkshark.feature.invoice.domain.Invoice;
import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceItem;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class InvoiceMapper {

    public InvoiceDto toDto(Invoice invoice) {
        Member member = invoice.getMember();
        return new InvoiceDto(
                invoice.getReference(),
                member.getId(),
                member.getFirstName() + " " + member.getLastName(),
                invoice.getCreationDate(),
                invoice.getExpirationDate(),
                invoice.getStatus(),
                invoice.getPaymentDate(),
                invoice.getMembershipMonthlyCost(),
                invoice.getItems().stream()
                        .map(this::toDto)
                        .sorted(Comparator.comparing(InvoiceItemDto::startTime))
                        .toList(),
                invoice.getTotal());
    }

    public List<InvoiceDto> toDto(List<Invoice> invoices) {
        return invoices.stream().map(this::toDto).toList();
    }

    private InvoiceItemDto toDto(InvoiceItem item) {
        Allocation allocation = item.getAllocation();
        return new InvoiceItemDto(
                allocation.getId(),
                allocation.getStartTime(),
                allocation.getEndTime(),
                item.getHoursParked(),
                item.getPrice(),
                item.getFine(),
                item.getTotal());
    }
}
