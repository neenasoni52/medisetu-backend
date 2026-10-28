package com.medisetu.medisetu_backend.dto.request;


import com.medisetu.medisetu_backend.entity.InputSource;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnswerSubmissionRequest {
    private String questionId;
    private String selectedOptionId;
    private String answerText;
    private String rawUtterance;
    private InputSource inputSource;
    private String questionPrompt;
    private String optionsJson;
}