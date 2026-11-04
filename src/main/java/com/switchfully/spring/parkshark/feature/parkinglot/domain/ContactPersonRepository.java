package com.switchfully.spring.parkshark.feature.parkinglot.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContactPersonRepository extends JpaRepository<ContactPerson, UUID> {
    Optional<ContactPerson> findByEmail(String email);
}
