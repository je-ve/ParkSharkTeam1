package com.switchfully.spring.parkshark.feature.member.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(UUID id) {
        super("Member with id " + id + " was not found");
    }

    private MemberNotFoundException(String message) {
        super(message);
    }

    public static MemberNotFoundException forLoggedInUser(UUID credentialsId) {
        return new MemberNotFoundException("No member profile found for logged-in user " + credentialsId);
    }
}
