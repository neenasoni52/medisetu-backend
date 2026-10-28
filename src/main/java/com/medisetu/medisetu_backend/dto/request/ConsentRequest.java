package com.medisetu.medisetu_backend.dto.request;



import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentRequest {
    @NotNull(message = "Agreed status is required")
    private Boolean agreed;
}