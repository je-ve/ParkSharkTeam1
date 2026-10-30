package com.switchfully.spring.parkshark.feature.division.api.dto;

import java.util.List;

public record DivisionOverviewDto(
        String name,
        String originalName,
        String director,
        DivisionDto parentDivision,
        List<DivisionDto> childrenDivisions
) {
}
