package com.switchfully.spring.parkshark.feature.division.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class DivisionNotFoundException extends NotFoundException {
    public DivisionNotFoundException(UUID id) {
        super("Could not find division with id " + id);
    }
}
