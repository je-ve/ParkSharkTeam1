package com.switchfully.spring.parkshark.feature.allocation.exception;

public class AllocationBadRequestException extends IllegalArgumentException {
    public AllocationBadRequestException(String message) {
        super(message);
    }

    public static AllocationBadRequestException wrongMembershipLevelException(String membership) {
        return new AllocationBadRequestException(messageFor(membership));
    }

    public static AllocationBadRequestException allocationMemberIdMismatchException() {
        return new AllocationBadRequestException("Bad request.");
    }

    public static AllocationBadRequestException wrongAllocationStatusException() {
        return new AllocationBadRequestException("Bad request.");
    }

    private static String messageFor(String membershipLevel) {
        return "Membership level %s doesn't allow you to do that.".formatted(membershipLevel);
    }
}