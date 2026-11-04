package com.switchfully.spring.parkshark.feature.parkinglot.api;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotOverviewDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotWithDivisionDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateParkingLotRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParkingLotMapper {

    private final AddressMapper addressMapper;
    private final ContactPersonMapper contactPersonMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final DivisionMapper divisionMapper;

    public ParkingLotMapper(AddressMapper addressMapper, ContactPersonMapper contactPersonMapper, ParkingLotMapper parkingLotMapper, DivisionMapper divisionMapper) {
        this.addressMapper = addressMapper;
        this.contactPersonMapper = contactPersonMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.divisionMapper = divisionMapper;
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

    public ParkingLotWithDivisionDto toWithDivisionDto(ParkingLot parkingLot) {
        return new ParkingLotWithDivisionDto(
                parkingLot.getId(),
                parkingLot.getName(),
                parkingLot.getCategory(),
                parkingLot.getCapacity(),
                addressMapper.toDto(parkingLot.getAddress()),
                parkingLot.getPricePerHour(),
                contactPersonMapper.toDto(parkingLot.getContactPerson()),
                divisionMapper.mapToDto(parkingLot.getDivision()));
    }

    public ParkingLotOverviewDto toOverviewDto(ParkingLot parkingLot) {
        return new ParkingLotOverviewDto(
                parkingLot.getId(),
                parkingLot.getName(),
                parkingLot.getCapacity(),
                parkingLot.getContactPerson().getMobilePhone(),
                parkingLot.getContactPerson().getTelephoneNumber()
        );
    }

    public List<ParkingLotOverviewDto> toOverviewDto(List<ParkingLot> parkingLots) {
        return parkingLots.stream().map(this::toOverviewDto).toList();
    }
}
