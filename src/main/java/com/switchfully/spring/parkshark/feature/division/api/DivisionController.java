package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionOverviewDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping(value ="/{id}")
    @Secured("CREATE_SUBDIVISION")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a subdivision")
    public DivisionOverviewDto createSubdivision(
            @RequestBody @Valid CreateDivisionRequest request,
            @PathVariable UUID id
    ) {
        return divisionService.createSubdivision(request, id);
    }

    @GetMapping(value ="/{id}")
    @Secured("VIEW_DIVISION")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a division")
    public DivisionOverviewDto createSubdivision(@PathVariable UUID id ) {
        return divisionService.getDivisionOverview(id);
    }

    @GetMapping
    @Secured("VIEW_DIVISIONS")
    @Operation(summary = "Get all divisions, sorted by name")
    public List<DivisionDto> getDivisions() {
        return divisionService.listAllDivisions();
    }
}
