package com.switchfully.spring.parkshark.feature.division.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DivisionRepository extends JpaRepository<Division, UUID> {
    boolean existsByName(String name);
}
