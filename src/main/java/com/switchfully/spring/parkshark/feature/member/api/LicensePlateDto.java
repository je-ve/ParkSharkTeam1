package com.switchfully.spring.parkshark.feature.member.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LicensePlateDto(

        @NotBlank(message = "Number is required")
        @Size(max = 20, message = "Number must be at most 20 charachters")
        @Schema(description = "Licens plate number", example = "1-AAA-123")
        String number,
        @NotBlank(message = "Street number is required")
        @Size(max = 50, message = "Issuing country must be at most 50 charachters")
        @Schema(description = "Country that issued this license plate", example = "BE")
        String issuingCountry
) {
}
