package com.switchfully.spring.parkshark.feature.division.exception;

import java.util.UUID;

public class DivisionNotFoundException extends IllegalArgumentException {
    public DivisionNotFoundException(UUID id) {
        super("Could not find division with id " + id);
    }
}
