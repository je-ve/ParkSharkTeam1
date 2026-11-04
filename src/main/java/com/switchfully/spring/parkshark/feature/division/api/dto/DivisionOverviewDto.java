package com.switchfully.spring.parkshark.feature.division.api.dto;
import java.util.List;
import java.util.UUID;

public record DivisionOverviewDto(
        UUID id,
        String name,
        String originalName,
        String director,
        DivisionDto parentDivision,
        List<DivisionDto> childrenDivisions
) {
}
