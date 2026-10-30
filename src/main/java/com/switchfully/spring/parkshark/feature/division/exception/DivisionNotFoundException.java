package com.switchfully.spring.parkshark.feature.division.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

public class DivisionNotFoundException extends NotFoundException {
    public DivisionNotFoundException(String message) {
        super(message);
    }

    public static DivisionNotFoundException divisionNotFoundException(String divisionName) {
        return new DivisionNotFoundException(messageFor(divisionName));
    }

    private static String messageFor(String divisionName) {
        return "Division %s not found.".formatted(divisionName);
    }
}
