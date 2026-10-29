package com.switchfully.spring.parkshark.feature.division.domain;

import java.util.UUID;

public class Division {

    private final UUID id;
    private String name;
    private final String originalName;
    private String director;
    private UUID parentDivisionId;

    public Division(UUID id, String originalName, String director, String name) {
        this.id = id;
        this.originalName = originalName;
        this.director = director;
        this.name = name;
        this.parentDivisionId = null;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getDirector() {
        return director;
    }

    public UUID getParentDivisionId() {
        return parentDivisionId;
    }
}
