package com.switchfully.spring.parkshark.feature.allocation.service;

import com.switchfully.spring.parkshark.feature.allocation.api.AllocationMapper;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.AllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationBadRequestException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationConflictException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationForbiddenException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationNotFoundException;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.member.exception.MemberNotFoundException;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.exception.ParkingLotNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final MemberRepository memberRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final AllocationMapper allocationMapper;
    private final Clock clock;

    public AllocationService(AllocationRepository allocationRepository, MemberRepository memberRepository, ParkingLotRepository parkingLotRepository, AllocationMapper allocationMapper, Clock clock) {
        this.allocationRepository = allocationRepository;
        this.memberRepository = memberRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.allocationMapper = allocationMapper;
        this.clock = clock;
    }

    public CreatedAllocationDto createAllocation(StartingParkingAllocationRequest request, UUID credentialsId) {
        Member member = getMemberByCredentialsId(credentialsId);
        ParkingLot parkingLot = getParkingLotById(request.parkingLotId());

        verifyLicencePlateMatchesMember(member, request.licencePlate());
        verifyParkingLotCapacity(parkingLot);

        Allocation allocation = allocationRepository.saveAndFlush(
                new Allocation(
                        LocalDateTime.now(clock),
                        member,
                        request.licencePlate(),
                        parkingLot
                )
        );
        return allocationMapper.mapToDto(allocation);
    }

    public AllocationDto stopAllocation(UUID allocationId, UUID credentialsId) {
        Allocation allocation = getAllocationById(allocationId);

        // Ownership first: another member must not learn whether the allocation is still active.
        verifyAllocationBelongsTo(allocation, credentialsId);
        allocation.stop(clock);

        return allocationMapper.toDto(allocation);
    }


    private void verifyLicencePlateMatchesMember(Member member, String licencePlate) {
        if (!member.getLicensePlate().getNumber().equals(licencePlate) && member.getMembershipLevel() != MembershipLevel.GOLD)
            throw AllocationBadRequestException.wrongMembershipLevelException(member.getMembershipLevel().name());
    }


    private void verifyParkingLotCapacity(ParkingLot parkingLot) {
        if (parkingLot.getCapacity() <= allocationRepository.countByParkingLotIdAndStatus(parkingLot.getId(), AllocationStatus.ONGOING))
            throw AllocationConflictException.parkingLotIsFullException(parkingLot.getId());
    }


    private Member getMemberByCredentialsId(UUID credentialsId) {
        return memberRepository.findByCredentialsId(credentialsId)
                .orElseThrow(() -> MemberNotFoundException.forLoggedInUser(credentialsId));
    }


    private ParkingLot getParkingLotById(UUID parkingLotId) {
        return parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ParkingLotNotFoundException(parkingLotId));
    }


    private Allocation getAllocationById(UUID allocationId) {
        return allocationRepository.findById(allocationId).orElseThrow(() -> AllocationNotFoundException.allocationNotFoundException(allocationId));
    }


    private void verifyAllocationBelongsTo(Allocation allocation, UUID credentialsId) {
        if (!allocation.isOwnedBy(credentialsId)) throw AllocationForbiddenException.notOwnerException(allocation.getId());
    }
}
