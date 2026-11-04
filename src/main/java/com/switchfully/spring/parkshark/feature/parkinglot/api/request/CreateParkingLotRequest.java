package com.switchfully.spring.parkshark.feature.parkinglot.api.request;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateParkingLotRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        @Schema(description = "Name of the parking lot", example = "Meir Underground")
        String name,

        @NotNull(message = "Category is required")
        @Schema(description = "Category: UNDERGROUND_BUILDING or ABOVE_GROUND_BUILDING", example = "UNDERGROUND_BUILDING")
        Category category,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be positive")
        @Schema(description = "Maximum number of cars", example = "250")
        Integer capacity,

        @NotNull(message = "Address is required")
        @Valid
        AddressDto address,

        @NotNull(message = "Price per hour is required")
        @DecimalMin(value = "0.00", message = "Price per hour must not be negative")
        @Digits(integer = 8, fraction = 2, message = "Price per hour must have at most 8 digits and 2 decimals")
        @Schema(description = "Price per hour in euro", example = "2.50")
        BigDecimal pricePerHour,

        @Schema(description = "Id of an existing contact person; give this or contactPerson, not both")
        UUID contactPersonId,

        @Valid
        @Schema(description = "A new contact person; give this or contactPersonId, not both")
        CreateContactPersonRequest contactPerson,

        @NotNull(message = "Division is required")
        @Schema(description = "Id of the division the parking lot belongs to")
        UUID divisionId) {

    @Schema(hidden = true)
    @AssertTrue(message = "Give either an existing contact person id or a new contact person, not both")
    public boolean isExactlyOneContactPersonGiven() {
        return (contactPersonId == null) != (contactPerson == null);
    }
}
