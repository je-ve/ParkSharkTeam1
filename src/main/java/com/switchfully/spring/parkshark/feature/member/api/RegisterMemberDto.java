package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterMemberDto {

    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must be at most 20 charachters")
    @Schema(description = "First name", example = "John")
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must be at most 255 charachters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    @NotNull(message = "Address is required")
    @Valid
    private AddressDto addressDto;
    @NotBlank(message = "Phone number name is required")
    @Size(max = 255, message = "Phone number must be at most 255 charachters")
    @Schema(description = "Phone number", example = "+32/125.21.15.41")
    private String telephone;
    @NotNull(message = "License plate is required")
    @Valid
    private LicensePlateDto licenseDto;
    @Schema(description = "Membership level (Gold, Silver or Bronze)", example = "+32/125.21.15.41")
    private MembershipLevel membershipLevel;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be at most 255 charachters")
    @Email
    @Schema(description = "Email", example = "John.Doe@Gmail.com")
    private String email;
    @NotBlank(message = "Password can not be null")
    @Size(max = 25, message = "Password must be at most 25 charachters")
    @Size(min = 8, message = "Password must be at most 8 charachters")
    @Schema(description = "Password", example = "1234")
    private String password;
}
