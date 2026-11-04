package com.switchfully.spring.parkshark.feature.parkinglot.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class ParkingLotNotFoundException extends NotFoundException {
    public ParkingLotNotFoundException(UUID id) {
        super("Could not find parking lot with id " + id);
    }
}
