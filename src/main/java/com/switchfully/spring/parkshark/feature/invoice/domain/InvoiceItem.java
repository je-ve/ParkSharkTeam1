package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invoice_item")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "allocation_id")
    private Allocation allocation;

    private int hoursParked;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal fine;

    InvoiceItem(Invoice invoice, Allocation allocation, int hoursParked, BigDecimal price, BigDecimal fine) {
        if (hoursParked < 0) {
            throw new IllegalArgumentException("Hours parked cannot be negative");
        }
        if (price.signum() < 0 || fine.signum() < 0) {
            throw new IllegalArgumentException("Price and fine cannot be negative");
        }
        this.invoice = Objects.requireNonNull(invoice);
        this.allocation = Objects.requireNonNull(allocation);
        this.hoursParked = hoursParked;
        this.price = price;
        this.fine = fine;
    }

    protected InvoiceItem() {
    }

    public BigDecimal getTotal() {
        return price.add(fine);
    }

    public UUID getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public int getHoursParked() {
        return hoursParked;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getFine() {
        return fine;
    }
}
