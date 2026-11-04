package com.switchfully.spring.parkshark.feature.allocation.service;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationBadRequestException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationConflictException;
import com.switchfully.spring.parkshark.feature.division.api.AllocationMapper;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDetailsDto;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

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

    public CreatedAllocationDto createAllocation(StartingParkingAllocationRequest request) {
        Member member = memberRepository.findById(request.memberId()).orElseThrow(() -> new NotFoundException(request.memberId().toString()));
        ParkingLot parkingLot = parkingLotRepository.findById(request.parkingLotId()).orElseThrow(() -> new NotFoundException(request.memberId().toString()));
        if (!member.getLicensePlate().getNumber().equals(request.licencePlate()) && !member.getMembershipLevel().name().equals("GOLD")){
            throw AllocationBadRequestException.wrongMembershipLevelException(member.getMembershipLevel().name());
        }
        if (parkingLot.getCapacity() <= allocationRepository.countByParkingLotIdAndStatus(request.parkingLotId(), AllocationStatus.ONGOING)) {
            throw AllocationConflictException.parkingLotIsFullException(parkingLot.getId());
        }
        try {
            Allocation allocation = allocationRepository.saveAndFlush(
                    new Allocation(
                            LocalDateTime.now(clock),
                            member,
                            request.licencePlate(),
                            parkingLot
                    )
            );
            return allocationMapper.mapToDto(allocation);
        } catch (DataIntegrityViolationException e) {
            throw AllocationConflictException.parkingLotIsFullException(parkingLot.getId());
        }
    }
}
