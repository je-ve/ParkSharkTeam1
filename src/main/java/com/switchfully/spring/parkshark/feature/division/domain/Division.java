package com.switchfully.spring.parkshark.feature.division.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "division")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ORIGINAL_NAME")
    private String originalName;

    @Column(name = "DIRECTOR")
    private String director;

    @Column(name = "PARENT_DIVISION_ID")
    private UUID parentDivisionId;

    public Division(String name, String originalName, String director) {
        this.originalName = originalName;
        this.director = director;
        this.name = name;
        this.parentDivisionId = null;
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

    public String getDirector() {
        return director;
    }

    public UUID getParentDivisionId() {
        return parentDivisionId;
    }
}
