package com.switchfully.spring.parkshark.feature.division.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "division")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String originalName;

    private String director;

    private UUID parentId;

    public Division(String name, String originalName, String director) {
        this.name = name;
        this.originalName = originalName;
        this.director = director;
        this.parentId = null;
    }

    public Division(String name, String originalName, String director, UUID parentId) {
        this.name = name;
        this.originalName = originalName;
        this.director = director;
        this.parentId = parentId;
    }

    protected Division() {}

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getDirector() {
        return director;
    }
}
