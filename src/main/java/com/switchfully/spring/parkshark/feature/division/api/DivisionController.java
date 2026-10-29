package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/divisions")
public class DivisionController {

    private final DivisionMapper divisionMapper;
    private final DivisionService divisionService;

    public DivisionController(DivisionMapper divisionMapper, DivisionService divisionService) {
        this.divisionMapper = divisionMapper;
        this.divisionService = divisionService;
    }

    @PostMapping
    @Secured("CREATE_DIVISION")
    @ResponseStatus(HttpStatus.CREATED)
    public DivisionDto createDivision(@RequestBody @Valid CreateDivisionRequest request) {
        return divisionMapper.mapToDto(divisionService.createDivision(request));
    }
}
