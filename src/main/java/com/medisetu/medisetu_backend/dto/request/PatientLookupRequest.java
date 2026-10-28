package com.medisetu.medisetu_backend.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientLookupRequest {
    @NotBlank(message = "Identifier cannot be empty")
    @Pattern(regexp = "^[0-9]{4,14}$", message = "Identifier must be between 4 and 14 digits")
    private String identifier;
}