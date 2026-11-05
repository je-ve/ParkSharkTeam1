package com.switchfully.spring.parkshark.feature.allocation.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class AllocationNotFoundException extends NotFoundException {
    public AllocationNotFoundException(String message) {
        super(message);
    }

    public static AllocationNotFoundException allocationNotFoundException(UUID allocationId) {
        return new AllocationNotFoundException("Allocation with id " + allocationId + " not found");
    }
}
