package com.switchfully.spring.parkshark.feature.allocation.exception;

public class AllocationNotFoundException extends RuntimeException {
    public AllocationNotFoundException(String message) {
        super(message);
    }
}
