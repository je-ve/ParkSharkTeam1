package com.switchfully.spring.parkshark.feature.security.domain;

import java.util.Set;

public enum Role {
    ;

    private final Set<Feature> features;

    Role(Feature... features) {
        this.features = Set.of(features);
    }

    public Set<Feature> getFeatures() {
        return features;
    }
}
