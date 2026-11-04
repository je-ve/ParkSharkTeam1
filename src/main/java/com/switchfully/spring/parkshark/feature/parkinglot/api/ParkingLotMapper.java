package com.switchfully.spring.parkshark.feature.parkinglot.api;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateParkingLotRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import org.springframework.stereotype.Component;

@Component
public class ParkingLotMapper {

    private final AddressMapper addressMapper;
    private final ContactPersonMapper contactPersonMapper;

    public ParkingLotMapper(AddressMapper addressMapper, ContactPersonMapper contactPersonMapper) {
        this.addressMapper = addressMapper;
        this.contactPersonMapper = contactPersonMapper;
    }

    public ParkingLot toEntity(CreateParkingLotRequest request, ContactPerson contactPerson, Division division) {
        return new ParkingLot(
                request.name(),
                request.category(),
                request.capacity(),
                contactPerson,
                addressMapper.toEntity(request.address()),
                division,
                request.pricePerHour());
    }

    public ParkingLotDto toDto(ParkingLot parkingLot) {
        return new ParkingLotDto(
                parkingLot.getId(),
                parkingLot.getName(),
                parkingLot.getCategory(),
                parkingLot.getCapacity(),
                addressMapper.toDto(parkingLot.getAddress()),
                parkingLot.getPricePerHour(),
                contactPersonMapper.toDto(parkingLot.getContactPerson()),
                parkingLot.getDivision().getId());
    }
}
