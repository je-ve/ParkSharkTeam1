package com.switchfully.spring.parkshark.feature.allocation.api;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    @Secured("START_ALLOCATION")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedAllocationDto createAllocation(
            @Valid @RequestBody StartingParkingAllocationRequest request) {
        return allocationService.createAllocation(request);
    }

}
