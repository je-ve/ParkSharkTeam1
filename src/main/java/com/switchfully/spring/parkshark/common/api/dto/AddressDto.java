package com.switchfully.spring.parkshark.common.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressDto(
        @NotBlank(message = "Street name is required")
        @Size(max = 255, message = "Street name must be at most 255 charachters")
        @Schema(description = "Streetname", example = "Meir")
        String streetName,
        @NotBlank(message = "Street number is required")
        @Size(max = 20, message = "Street number must be at most 20 charachters")
        @Schema(description = "Street number", example = "10a")
        String streetNumber,
        @NotBlank(message = "Postal coded is required")
        @Size(max = 20, message = "Postal code must be at most 20 charachters")
        @Schema(description = "Postal code", example = "2000")
        String postalCode,
        @NotBlank(message = "City is required")
        @Size(max = 255, message = "City must be at most 255 charachters")
        @Schema(description = "City", example = "Antwerp")
        String city
) {
}
