package com.switchfully.spring.parkshark.feature.security.domain;

import java.util.Set;

public enum Role {
    MEMBER(
            Feature.START_ALLOCATION,
            Feature.STOP_ALLOCATION,
            Feature.GET_MONTHLY_INVOICE),
    MANAGER(
            Feature.CREATE_DIVISION,
            Feature.VIEW_DIVISIONS,
            Feature.CREATE_PARKING_LOT,
            Feature.VIEW_MEMBERS,
            Feature.CREATE_SUBDIVISION,
            Feature.VIEW_DIVISION,
            Feature.VIEW_PARKING_LOTS,
            Feature.VIEW_PARKING_LOT,
            Feature.VIEW_INVOICES,
            Feature.CLOSE_INVOICE);

    private final Set<Feature> features;

    Role(Feature... features) {
        this.features = Set.of(features);
    }

    public Set<Feature> getFeatures() {
        return features;
    }
}
