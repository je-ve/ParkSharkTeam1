package com.switchfully.spring.parkshark.feature.security.domain;

import java.util.Set;

public enum Role {
    MEMBER,
    MANAGER(
            Feature.CREATE_MEMBER,
            Feature.CREATE_DIVISION)
    ;

    private final Set<Feature> features;

    Role(Feature... features) {
        this.features = Set.of(features);
    }

    public Set<Feature> getFeatures() {
        return features;
    }
}
