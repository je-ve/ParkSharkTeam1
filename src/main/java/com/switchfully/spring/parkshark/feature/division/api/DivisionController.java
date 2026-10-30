package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/divisions")
public class DivisionController {

    private final DivisionService divisionService;

    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    @PostMapping
    @Secured("CREATE_DIVISION")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a division")
    public DivisionDto createDivision(@RequestBody @Valid CreateDivisionRequest request) {
        return divisionService.createDivision(request);
    }

    @GetMapping
    @Secured("VIEW_DIVISIONS")
    @Operation(summary = "Get all divisions, sorted by name")
    public List<DivisionDto> getDivisions() {
        return divisionService.listAllDivisions();
    }
}
