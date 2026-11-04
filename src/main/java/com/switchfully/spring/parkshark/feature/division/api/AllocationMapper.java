package com.switchfully.spring.parkshark.feature.division.api;

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
}
