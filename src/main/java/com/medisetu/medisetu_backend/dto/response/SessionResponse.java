package com.medisetu.medisetu_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.medisetu.medisetu_backend.entity.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SessionResponse {
    private Long sessionId;
    private Long patientId;
    private String tokenNumber;
    @JsonProperty("isTemporary")
    private boolean isTemporary;
    private String maskedName;
    private String language;
    private SessionStatus sessionStatus;
    private LocalDateTime createdAt;
}