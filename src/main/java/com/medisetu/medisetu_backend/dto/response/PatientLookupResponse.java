package com.medisetu.medisetu_backend.dto.response;



import com.medisetu.medisetu_backend.entity.Gender;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientLookupResponse {
    private Long patientId;
    // DPDP minimization: only first name is sent to public kiosk screen
    private String firstName;
    private Integer age;
    private Gender gender;
}