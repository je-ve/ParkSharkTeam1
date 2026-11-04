package com.switchfully.spring.parkshark.feature.allocation.api;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.AllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StopParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.service.AllocationService;
import com.switchfully.spring.parkshark.feature.security.authentication.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartingParkingAllocationRequest request) {
        return allocationService.createAllocation(request, user.id());
    }

    @PutMapping
    @Secured("STOP_ALLOCATION")
    @ResponseStatus(HttpStatus.OK)
    public AllocationDto stopAllocation(
            @AuthenticationPrincipal AuthenticatedUser user,
            StopParkingAllocationRequest request) {
        return allocationService.stopAllocation(request, user.id());
    }
}
