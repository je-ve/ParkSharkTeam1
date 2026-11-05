package com.switchfully.spring.parkshark.feature.allocation.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

import java.util.UUID;

public class AllocationConflictException extends ConflictException {
    public AllocationConflictException(String message) {
        super(message);
    }

    public static AllocationConflictException parkingLotIsFullException(UUID id) {
        return new AllocationConflictException(messageFor(id.toString()));
    }

    private static String messageFor(String parkingLotId) {
        return "Parking lot %s is full.".formatted(parkingLotId);
    }


}