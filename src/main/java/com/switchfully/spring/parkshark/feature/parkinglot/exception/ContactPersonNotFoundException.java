package com.switchfully.spring.parkshark.feature.parkinglot.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class ContactPersonNotFoundException extends NotFoundException {
    public ContactPersonNotFoundException(UUID id) {
        super("Could not find contact person with id " + id);
    }
}
