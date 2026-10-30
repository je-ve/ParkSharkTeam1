package com.switchfully.spring.parkshark.feature.member.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LicensePlateDto(
        @NotBlank(message = "License plate number is required")
        @Size(max = 20, message = "License plate number must be at most 20 characters")
        @Schema(description = "License plate number", example = "1-AAA-123")
        String number,

        @NotBlank(message = "Issuing country is required")
        @Size(max = 50, message = "Issuing country must be at most 50 characters")
        @Schema(description = "Country that issued this license plate", example = "BE")
        String issuingCountry) {
}
