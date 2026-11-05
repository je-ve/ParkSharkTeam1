package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.invoice.exception.InvoiceAlreadyClosedException;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Generated
    @Column(insertable = false, updatable = false)
    private Long reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate billingMonth;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal membershipMonthlyCost;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice(Member member, LocalDate creationDate, LocalDate expirationDate) {
        this.member = Objects.requireNonNull(member);
        this.creationDate = Objects.requireNonNull(creationDate);
        this.expirationDate = Objects.requireNonNull(expirationDate);
        if (expirationDate.isBefore(creationDate)) {
            throw new IllegalArgumentException("Expiration date cannot be before creation date");
        }
        this.billingMonth = billingMonthOf(creationDate);
        this.status = InvoiceStatus.OPEN;
        this.membershipMonthlyCost = member.getMembershipLevel().getMonthlyCost();
    }

    protected Invoice() {
    }

    public static LocalDate billingMonthOf(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public void addItem(Allocation allocation, ParkingPrice parkingPrice) {
        allocation.markAsInvoiced();
        items.add(new InvoiceItem(this, allocation, parkingPrice.hoursParked(), parkingPrice.price(), parkingPrice.fine()));
    }

    public void close(LocalDate paymentDate) {
        if (status == InvoiceStatus.CLOSED) {
            throw new InvoiceAlreadyClosedException(reference);
        }
        this.paymentDate = Objects.requireNonNull(paymentDate);
        this.status = InvoiceStatus.CLOSED;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(InvoiceItem::getTotal)
                .reduce(membershipMonthlyCost, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public Long getReference() {
        return reference;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBillingMonth() {
        return billingMonth;
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

    public BigDecimal getMembershipMonthlyCost() {
        return membershipMonthlyCost;
    }

    public List<InvoiceItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
