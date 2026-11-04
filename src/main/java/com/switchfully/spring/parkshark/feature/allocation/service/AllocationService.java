package com.switchfully.spring.parkshark.feature.allocation.service;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AllocationService {

    private final AllocationRepository allocationRepository;

    public AllocationService(AllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    public CreatedAllocationDto createAllocation(StartingParkingAllocationRequest request) {

    }
}
