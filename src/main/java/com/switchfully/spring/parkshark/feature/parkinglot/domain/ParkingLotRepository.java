package com.switchfully.spring.parkshark.feature.parkinglot.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, UUID> {
    @Query("select p from ParkingLot p join fetch p.contactPerson order by p.name")
    List<ParkingLot> findAllWithContactPerson();

    @EntityGraph(attributePaths = {"contactPerson", "division"})
    Optional<ParkingLot> findWithContactAndDivisionById(UUID id);
}