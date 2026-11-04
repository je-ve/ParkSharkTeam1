package com.switchfully.spring.parkshark.feature.parkinglot.api.request;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateContactPersonRequest(
        @NotBlank(message = "Contact person name is required")
        @Size(max = 255, message = "Contact person name must be at most 255 characters")
        @Schema(description = "Name of the contact person", example = "Jane Doe")
        String name,

        @Size(max = 50, message = "Mobile phone must be at most 50 characters")
        @Schema(description = "Mobile phone; required when no telephone number is given", example = "+32 470 12 34 56")
        String mobilePhone,

        @Size(max = 50, message = "Telephone number must be at most 50 characters")
        @Schema(description = "Telephone number; required when no mobile phone is given", example = "+32 3 123 45 67")
        String telephoneNumber,

        @NotBlank(message = "Contact person email is required")
        @Email(message = "Contact person email must be valid")
        @Size(max = 255, message = "Contact person email must be at most 255 characters")
        @Schema(description = "Email of the contact person", example = "jane.doe@company.be")
        String email,

        @NotNull(message = "Contact person address is required")
        @Valid
        AddressDto address) {

    @Schema(hidden = true)
    @AssertTrue(message = "Mobile phone or telephone number is required")
    public boolean isAPhoneNumberGiven() {
        return !isBlank(mobilePhone) || !isBlank(telephoneNumber);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
