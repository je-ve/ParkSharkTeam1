package com.switchfully.spring.parkshark.feature.allocation.api;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.AllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import org.springframework.stereotype.Component;

@Component
public class AllocationMapper {
    public CreatedAllocationDto mapToDto(Allocation allocation) {
        return new CreatedAllocationDto(
                allocation.getId(),
                allocation.getStartTime()
        );
    }

    public AllocationDto toDto(Allocation allocation) {
        return new  AllocationDto(
                allocation.getId(),
                allocation.getStartTime(),
                allocation.getEndTime(),
                allocation.getLicensePlateNumber(),
                allocation.getStatus().name()
        );
    }
}
