package com.switchfully.spring.parkshark.feature.division.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDivisionRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters")
        @Schema(description = "Name of the division", example = "ParkShark North")
        String name,

        @NotBlank(message = "Original name is required")
        @Size(max = 50, message = "Original name must be at most 50 characters")
        @Schema(description = "Name of the company before ParkShark bought it", example = "Parking Antwerp NV")
        String originalName,

        @NotBlank(message = "Director is required")
        @Size(max = 100, message = "Director must be at most 100 characters")
        @Schema(description = "Name of the director", example = "Jane Doe")
        String director
) {
}
