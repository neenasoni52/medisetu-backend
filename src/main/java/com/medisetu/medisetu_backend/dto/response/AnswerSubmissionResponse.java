package com.medisetu.medisetu_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter @Setter @AllArgsConstructor
public class AnswerSubmissionResponse {
    private JsonNode nextQuestion;
    private RedFlagAlertDTO redFlag;
    private boolean completed;
}