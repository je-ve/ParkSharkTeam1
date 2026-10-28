package com.switchfully.spring.parkshark.feature.security.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException(String email) {
        super(messageFor(email));
    }

    public EmailAlreadyUsedException(String email, Throwable cause) {
        super(messageFor(email), cause);
    }

    private static String messageFor(String email) {
        return "Email " + email + " is already in use";
    }
}
