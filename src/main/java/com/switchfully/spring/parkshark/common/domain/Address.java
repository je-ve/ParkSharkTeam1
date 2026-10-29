package com.switchfully.spring.parkshark.common.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String streetName;
    private String streetNumber;
    private String postalCode;
    private String city;

    public Address(String streetName, String streetNumber, String postalCode, String city) {

        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.postalCode = postalCode;
        this.city = city;
    }

    protected Address() {
    }

    public String getStreetName() {
        return streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }
}
