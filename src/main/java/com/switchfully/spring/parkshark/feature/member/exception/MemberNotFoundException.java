package com.switchfully.spring.parkshark.feature.member.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

import java.util.UUID;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(UUID id) {
        super("Member with id " + id + " was not found");
    }
}
