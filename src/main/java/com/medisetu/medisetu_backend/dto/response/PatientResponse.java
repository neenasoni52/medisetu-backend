package com.medisetu.medisetu_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medisetu.medisetu_backend.entity.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatientResponse {
    private Long id;
    private String tokenNumber;
    private String abhaNumber;
    private String phoneNumber;
    private String fullName;
    private Integer age;
    private Gender gender;
    @JsonProperty("isTemporary")
    private boolean isTemporary;
    private LocalDateTime createdAt;
}