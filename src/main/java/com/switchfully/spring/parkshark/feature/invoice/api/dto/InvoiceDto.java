package com.switchfully.spring.parkshark.feature.invoice.api.dto;

import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceItem;
import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceStatus;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class InvoiceDto {

    private Long reference;
    private MemberDto member;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private LocalDate paymentDate;
    private InvoiceStatus status;
    private double membershipMonthlyCost;
    private List<InvoiceItemDto> invoiceItems;

    public InvoiceDto(Long reference, MemberDto member, LocalDate creationDate, LocalDate expirationDate,
                      LocalDate paymentDate, InvoiceStatus status, double membershipMonthlyCost,
                      List<InvoiceItemDto> invoiceItems) {
        this.reference = reference;
        this.member = member;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.paymentDate = paymentDate;
        this.status = status;
        this.membershipMonthlyCost = membershipMonthlyCost;
        this.invoiceItems = invoiceItems;
    }
}
