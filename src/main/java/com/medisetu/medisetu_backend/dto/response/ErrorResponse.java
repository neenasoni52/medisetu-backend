package com.medisetu.medisetu_backend.dto.response;



import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}