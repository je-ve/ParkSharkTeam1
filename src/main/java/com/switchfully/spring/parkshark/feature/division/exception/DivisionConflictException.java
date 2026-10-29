package com.switchfully.spring.parkshark.feature.division.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

public class DivisionConflictException extends ConflictException {
    public DivisionConflictException(String message) {
        super(message);
    }

    public static DivisionConflictException duplicateDivisionException(String divisionName) {
        throw new  DivisionConflictException("Division %s already exists.".formatted(divisionName));
    }
}
