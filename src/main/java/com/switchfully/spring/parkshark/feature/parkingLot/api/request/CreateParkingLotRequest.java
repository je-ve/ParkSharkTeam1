package com.switchfully.spring.parkshark.feature.parkingLot.api.request;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.parkingLot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkingLot.domain.ContactPerson;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateParkingLotRequest {

    private String name;

    private Category category;
    private int capacity;
    private AddressDto address;
    private BigDecimal pricePerHour;
    private ContactPersonDto contactPerson;
    private Division division;
}
