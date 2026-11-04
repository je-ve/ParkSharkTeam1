package com.switchfully.spring.parkshark.feature.parkinglot.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, UUID> {
}