package com.switchfully.spring.parkshark.feature.allocation.api;

import com.switchfully.spring.parkshark.feature.allocation.api.dto.AllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.service.AllocationService;
import com.switchfully.spring.parkshark.feature.security.authentication.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @Operation(summary = "Start a parking allocation for the logged-in member")
    public CreatedAllocationDto createAllocation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartingParkingAllocationRequest request) {
        return allocationService.createAllocation(request, user.id());
    }

    @PatchMapping("/{id}/stop")
    @Secured("STOP_ALLOCATION")
    @Operation(summary = "Stop one of the logged-in member's active allocations (sets its stop time and status)")
    public AllocationDto stopAllocation(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID id) {
        return allocationService.stopAllocation(id, user.id());
    }
}
