package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.member.domain.Member;
import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reference;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private LocalDate paymentDate;
    private InvoiceStatus status;
    private double membershipMonthlyCost;

    public Invoice(Long reference, UUID memberId, LocalDate creationDate, LocalDate expirationDate,
                   LocalDate paymentDate, InvoiceStatus status, double membershipMonthlyCost) {
        this.reference = reference;
        this.memberId = memberId;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.paymentDate = paymentDate;
        this.status = status;
        this.membershipMonthlyCost = membershipMonthlyCost;
    }

    protected Invoice() {
    }

    public UUID getId() {
        return id;
    }

    public Long getReference() {
        return reference;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public double getMembershipMonthlyCost() {
        return membershipMonthlyCost;
    }
}
