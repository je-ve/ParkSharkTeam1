package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public DivisionDto createDivision(@RequestBody CreateDivisionRequest request) {
        return divisionMapper.mapToDto(divisionService.createDivision(request));
    }
}
