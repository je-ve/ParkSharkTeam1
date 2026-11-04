package com.switchfully.spring.parkshark.feature.allocation.service;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationBadRequestException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationConflictException;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDetailsDto;
import com.switchfully.spring.parkshark.feature.member.api.dto.MemberDto;
import com.switchfully.spring.parkshark.feature.member.service.MemberService;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotDto;
import com.switchfully.spring.parkshark.feature.parkinglot.service.ParkingLotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final MemberService memberService;
    private final ParkingLotService parkingLotService;

    public AllocationService(AllocationRepository allocationRepository, MemberService memberService, ParkingLotService parkingLotService) {
        this.allocationRepository = allocationRepository;
        this.memberService = memberService;
        this.parkingLotService = parkingLotService;
    }

    public CreatedAllocationDto createAllocation(StartingParkingAllocationRequest request) {
        MemberDetailsDto memberDto = memberService.getMember(request.memberId());
        ParkingLotDto parkingLotDto = parkingLotService.getParkingLot(request.parkingLotId());
        if (!memberDto.licensePlate().number().equals(request.licencePlate()) && !memberDto.membershipLevel().name().equals("GOLD")){
            throw AllocationBadRequestException.wrongMembershipLevelException(memberDto.membershipLevel().name());
        }
        if (parkingLotDto.capacity() <= allocationRepository.countByParkingLotIdAndStatus(request.parkingLotId(), AllocationStatus.ONGOING)) {
            throw AllocationConflictException.parkingLotIsFullException(parkingLotDto.id());
        }
        return null;
    }
}
