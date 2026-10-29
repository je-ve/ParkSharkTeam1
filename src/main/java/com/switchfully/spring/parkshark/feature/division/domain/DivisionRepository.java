package com.switchfully.spring.parkshark.feature.division.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DivisionRepository extends CrudRepository<Division, UUID>{
    boolean existsByName(String name);
}
