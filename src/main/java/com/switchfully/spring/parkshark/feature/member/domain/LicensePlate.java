package com.switchfully.spring.parkshark.feature.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LicensePlate {

    @Column(name="license_plate_number")
    private String number;

    @Column(name="license_plate_country")
    private String issuingCountry;

    public LicensePlate(String number, String issuingCountry) {
        this.number = number;
        this.issuingCountry = issuingCountry;
    }

    protected LicensePlate() {
    }

    public String getNumber() {
        return number;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }
}
