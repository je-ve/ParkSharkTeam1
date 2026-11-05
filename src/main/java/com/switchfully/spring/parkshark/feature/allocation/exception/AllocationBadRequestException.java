package com.switchfully.spring.parkshark.feature.allocation.exception;

public class AllocationBadRequestException extends IllegalArgumentException {
    public AllocationBadRequestException(String message) {
        super(message);
    }

    public static AllocationBadRequestException wrongMembershipLevelException(String membership) {
        return new AllocationBadRequestException(messageFor(membership));
    }

    private static String messageFor(String membershipLevel) {
        return "Membership level %s doesn't allow you to do that.".formatted(membershipLevel);
    }
}
