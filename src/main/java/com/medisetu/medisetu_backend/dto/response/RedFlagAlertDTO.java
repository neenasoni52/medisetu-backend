package com.medisetu.medisetu_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class RedFlagAlertDTO {
    private boolean raised;
    private String severity; // "NONE", "YELLOW", "CRITICAL"
    private String message;
    private String ruleId;
}