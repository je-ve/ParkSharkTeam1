package com.switchfully.spring.parkshark.feature.parkinglot.api;

import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotOverviewDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotWithDivisionDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateParkingLotRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @PostMapping
    @Secured("CREATE_PARKING_LOT")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a parking lot, with a new or an existing contact person")
    public ParkingLotDto createParkingLot(@RequestBody @Valid CreateParkingLotRequest request) {
        return parkingLotService.createParkingLot(request);
    }

    @GetMapping
    @Secured("VIEW_PARKING_LOTS")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "View all parking lots with their contact person")
    public List<ParkingLotOverviewDto> viewParkingLots() {
        return parkingLotService.getParkingLotsOverview();
    }

    @GetMapping(path="/{id}")
    @Secured("VIEW_PARKING_LOT")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "View a parking lot with its division by id")
    public ParkingLotWithDivisionDto viewParkingLotWithDivisionById(@PathVariable UUID id) {
        return parkingLotService.getParkingLotWithContactAndDivisionById(id);
    }
}
