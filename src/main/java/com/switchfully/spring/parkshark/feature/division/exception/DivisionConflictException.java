package com.switchfully.spring.parkshark.feature.division.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

public class DivisionConflictException extends ConflictException {
    public DivisionConflictException(String message) {
        super(message);
    }

    public DivisionConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DivisionConflictException duplicateDivisionException(String divisionName) {
        return new DivisionConflictException(messageFor(divisionName));
    }

    public static DivisionConflictException duplicateDivisionException(String divisionName, Throwable cause) {
        return new DivisionConflictException(messageFor(divisionName), cause);
    }

    private static String messageFor(String divisionName) {
        return "Division %s already exists.".formatted(divisionName);
    }
}
