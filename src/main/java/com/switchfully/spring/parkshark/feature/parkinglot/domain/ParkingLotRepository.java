package com.switchfully.spring.parkshark.feature.parkinglot.domain;

import com.switchfully.spring.parkshark.feature.member.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, UUID> {
    @Query("select p from ParkingLot p join fetch p.contactPerson")
    List<ParkingLot> findAllWithContactPerson();

    @EntityGraph(attributePaths = {"contactPerson, division"})
    ParkingLot findWithContactAndDivisionById(@Param("id") UUID id);
}