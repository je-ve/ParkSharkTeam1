package com.switchfully.spring.parkshark.feature.division.api.dto;

import java.util.UUID;

public record DivisionDto(
        UUID id,
        String name,
        String originalName,
        String director
) {
}
