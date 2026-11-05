package com.switchfully.spring.parkshark.feature.allocation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, UUID> {
    int countByParkingLotIdAndStatus(UUID parkingLotId, AllocationStatus status);

    @Query("""
            select a from Allocation a
            join fetch a.parkingLot
            where a.member.id = :memberId and a.status = :status
            order by a.startTime""")
    List<Allocation> findWithParkingLotByMemberIdAndStatus(UUID memberId, AllocationStatus status);
}
