package com.medisetu.medisetu_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartSessionRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    
    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    @Pattern(regexp = "^[a-z]{2}(-[a-z]{2})?$", message = "Invalid language code format (e.g., 'hi', 'en', 'mr', 'ta')")
     private String language;
}