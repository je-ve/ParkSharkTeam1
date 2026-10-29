package com.switchfully.spring.parkshark.feature.division.api.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDivisionRequest(
        @NotBlank String name,
        @NotBlank String originalName,
        @NotBlank String director
) {
}
