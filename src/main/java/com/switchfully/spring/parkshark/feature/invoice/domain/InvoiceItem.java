package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "allocation_id", unique = true)
    private Allocation allocation;

    private int hoursParked;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    @Column(precision = 10, scale = 2)
    private BigDecimal fine;

    public InvoiceItem(Invoice invoice, Allocation allocation, int hoursParked, BigDecimal price, BigDecimal fine) {
        if (hoursParked < 0) throw new IllegalArgumentException("Hours parked can not be negative");
        this.invoice = invoice;
        this.allocation = allocation;
        this.hoursParked = hoursParked;
        this.price = price;
        this.fine = fine;
    }

    protected InvoiceItem() {
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

    public double getPrice() {
        return price;
    }

    public double getFine() {
        return fine;
    }
}
