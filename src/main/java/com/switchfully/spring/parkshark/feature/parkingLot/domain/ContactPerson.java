package com.switchfully.spring.parkshark.feature.parkingLot.domain;

import com.switchfully.spring.parkshark.common.domain.Address;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "contact_person")
public class ContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    private String name;
    private String mobilePhone;
    private String telephoneNumber;
    private String email;
    @Embedded
    private Address address;

    public ContactPerson(String name, String mobilePhone, String telephoneNumber, String email, Address address) {
        if (ContactPerson.isBlank(mobilePhone) && isBlank(telephoneNumber)) {
            throw new IllegalArgumentException("Mobile phone or telephone number is required");
        }

        this.name = Objects.requireNonNull(name);
        this.mobilePhone = isBlank(mobilePhone) ? null : mobilePhone;
        this.telephoneNumber = isBlank(telephoneNumber) ? null : telephoneNumber;
        this.email = Objects.requireNonNull(email);
        this.address = Objects.requireNonNull(address);
    }

    public ContactPerson() {
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }
}
