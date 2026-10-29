package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.domain.Division;

import java.util.List;

public class DivisionMapper {

    public DivisionDto mapToDto(Division division) {
        return new DivisionDto(
                division.getId(),
                division.getName(),
                division.getOriginalName(),
                division.getDirector());
    }

    public List<DivisionDto> mapToDto(List<Division> divisions) {
        return divisions.stream()
                .map(this::mapToDto)
                .toList();
    }
}
