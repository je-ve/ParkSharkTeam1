package com.switchfully.spring.parkshark.feature.member.api.dto;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterMemberRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must be at most 255 characters")
        @Schema(description = "First name", example = "John")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must be at most 255 characters")
        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @NotNull(message = "Address is required")
        @Valid
        AddressDto address,

        @NotBlank(message = "Telephone is required")
        @Size(max = 50, message = "Telephone must be at most 50 characters")
        @Schema(description = "Telephone number", example = "+32 470 12 34 56")
        String telephone,

        @NotNull(message = "License plate is required")
        @Valid
        LicensePlateDto licensePlate,

        @Schema(description = "Membership level: BRONZE, SILVER or GOLD; BRONZE when omitted", example = "GOLD")
        MembershipLevel membershipLevel,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        @Schema(description = "Email, also used to log in; spaces and capitals are ignored", example = "john.doe@gmail.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Schema(description = "Password used to log in, 8 to 72 characters", example = "secret123")
        String password) {
}
