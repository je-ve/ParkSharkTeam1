package com.switchfully.spring.parkshark.feature.parkinglot.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

import java.util.UUID;

public class ContactPersonConflictException extends ConflictException {

    private ContactPersonConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ContactPersonConflictException emailAlreadyUsed(String email, UUID existingId) {
        return new ContactPersonConflictException(
                "Contact person with email %s already exists (id %s). Use contactPersonId to link it."
                        .formatted(email, existingId), null);
    }

    public static ContactPersonConflictException emailAlreadyUsed(String email, Throwable cause) {
        return new ContactPersonConflictException(
                "Contact person with email %s already exists. Use contactPersonId to link it."
                        .formatted(email), cause);
    }
}
