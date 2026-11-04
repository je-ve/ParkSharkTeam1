package com.switchfully.spring.parkshark.feature.parkinglot.api.request;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;

import java.math.BigDecimal;

public class CreateParkingLotRequest {

    private String name;

    private Category category;
    private int capacity;
    private AddressDto address;
    private BigDecimal pricePerHour;
    private ContactPersonDto contactPerson;
    private Division division;
}
