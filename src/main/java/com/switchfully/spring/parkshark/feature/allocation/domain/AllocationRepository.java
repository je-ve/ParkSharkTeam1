package com.switchfully.spring.parkshark.feature.allocation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, UUID> {
    int countByParkingLotIdAndStatus(UUID parkingLotId, AllocationStatus status);
}
