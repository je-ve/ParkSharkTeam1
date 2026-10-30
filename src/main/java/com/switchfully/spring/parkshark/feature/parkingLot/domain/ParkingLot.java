package com.switchfully.spring.parkshark.feature.parkingLot.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ParkingLot {
// name, a category, a (max) capacity, a contact person, and an address

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Category category;
    private int capacity;
    private Address address;
    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerHour;
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "contact_person_id")
    private ContactPerson contactPerson;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "division_id")
    private Division division;

    public ParkingLot(String name, Category category, int capacity, ContactPerson contactPerson, Address address, Division division, BigDecimal pricePerHour) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity should be positive");
        if (pricePerHour == null || pricePerHour.signum() == -1)
            throw new IllegalArgumentException("Price per hour should not be empty or negative");
        this.name = Objects.requireNonNull(name);
        this.category = Objects.requireNonNull(category);
        this.capacity = capacity;
        this.contactPerson = Objects.requireNonNull(contactPerson);
        this.address = Objects.requireNonNull(address);
        this.division = Objects.requireNonNull(division);
        this.pricePerHour = pricePerHour;
    }

    protected ParkingLot() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getCapacity() {
        return capacity;
    }

    public ContactPerson getContactPerson() {
        return contactPerson;
    }

    public Address getAddress() {
        return address;
    }

    public Division getDivision() {
        return division;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }
}
