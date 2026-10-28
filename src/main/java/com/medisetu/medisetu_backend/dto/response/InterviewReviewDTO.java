package com.medisetu.medisetu_backend.dto.response;

import com.medisetu.medisetu_backend.entity.InputSource;
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
public class InterviewReviewDTO {
    private String questionId;
    private String moduleName;
    private String prompt;
    private String selectedOptionId;
    private String selectedOptionLabel;
    private String answerText;
    private String rawUtterance;
    private InputSource inputSource;
}
