package com.switchfully.spring.parkshark.feature.member.domain;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String firstName;
    private String lastName;
    private Address address;
    private String telephone;
    private LicensePlate licensePlate;
    private LocalDate registrationDate;
    private MembershipLevel membershipLevel;
    @OneToOne
    @JoinColumn(name ="credentials_id")
    private Credentials credentials;

    public Member(String firstName, String lastName, Address address, String telephone, LicensePlate licensePlate, LocalDate registrationDate, MembershipLevel membershipLevel, Credentials credentials) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.telephone = telephone;
        this.licensePlate = licensePlate;
        this.registrationDate = registrationDate;
        this.membershipLevel = membershipLevel;
        this.credentials = credentials;
    }

    protected Member() {
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }

    public String getTelephone() {
        return telephone;
    }

    public LicensePlate getLicensePlate() {
        return licensePlate;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public MembershipLevel getMembershipLevel() {
        return membershipLevel;
    }
}

