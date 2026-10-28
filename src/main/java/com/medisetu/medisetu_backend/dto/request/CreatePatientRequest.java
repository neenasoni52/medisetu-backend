package com.medisetu.medisetu_backend.dto.request;

import com.medisetu.medisetu_backend.entity.Gender;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePatientRequest {

    @Pattern(regexp = "^[0-9]{3,6}$", message = "Token must be 3-6 digits")
    private String tokenNumber;

    @Pattern(regexp = "^[0-9]{14}$", message = "ABHA must be exactly 14 digits")
    private String abhaNumber;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be under 120")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;
}