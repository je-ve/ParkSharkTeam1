package com.switchfully.spring.parkshark.feature.parkingLot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name="contact_person")
public class ContactPerson {

    @Id
    UUID id;
    private String name;
//obile phone number, a telephone number, an email, and an address
    private String mobilePhone;
    private String telephoneNumber;
    private String email;
    private Address address;
}
