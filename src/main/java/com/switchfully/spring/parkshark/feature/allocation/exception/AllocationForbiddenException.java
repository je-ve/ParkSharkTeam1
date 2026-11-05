package com.switchfully.spring.parkshark.feature.allocation.exception;

import com.switchfully.spring.parkshark.common.exception.ForbiddenException;

import java.util.UUID;

public class AllocationForbiddenException extends ForbiddenException {
    private AllocationForbiddenException(String message) {
        super(message);
    }

    public static AllocationForbiddenException notOwnerException(UUID allocationId) {
        return new AllocationForbiddenException("Allocation %s belongs to another member.".formatted(allocationId));
    }
}
