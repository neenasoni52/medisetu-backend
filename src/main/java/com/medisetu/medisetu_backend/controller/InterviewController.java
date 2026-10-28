package com.medisetu.medisetu_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import com.medisetu.medisetu_backend.dto.request.AnswerSubmissionRequest;
import com.medisetu.medisetu_backend.dto.response.AnswerSubmissionResponse;
import com.medisetu.medisetu_backend.dto.response.InterviewReviewDTO;
import com.medisetu.medisetu_backend.entity.IntakeSession;
import com.medisetu.medisetu_backend.entity.InterviewAnswer;
import com.medisetu.medisetu_backend.exception.ResourceNotFoundException;
import com.medisetu.medisetu_backend.repository.IntakeSessionRepository;
import com.medisetu.medisetu_backend.repository.InterviewAnswerRepository;
import com.medisetu.medisetu_backend.service.InterviewService;
import com.medisetu.medisetu_backend.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/medisetu/api/v1/sessions/{sessionId}/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final QuestionService questionService;
    private final IntakeSessionRepository sessionRepository;
    private final InterviewAnswerRepository answerRepository;

    @Value("${medisetu.ayush.enabled:false}")
    private boolean defaultAyushEnabled;

    @GetMapping("/next")
    public ResponseEntity<JsonNode> getNextQuestion(@PathVariable Long sessionId) {
        IntakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);
        JsonNode next = questionService.getNextQuestion(session, answers, defaultAyushEnabled);

        return ResponseEntity.ok(next);
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerSubmissionResponse> submitAnswer(
            @PathVariable Long sessionId,
            @RequestBody AnswerSubmissionRequest request) {
        return ResponseEntity.ok(interviewService.submitAnswer(sessionId, request));
    }

    @GetMapping("/review")
    public ResponseEntity<List<InterviewReviewDTO>> getInterviewReview(@PathVariable Long sessionId) {
        return ResponseEntity.ok(interviewService.getInterviewReview(sessionId));
    }

    @PostMapping("/acknowledge-redflag")
    public ResponseEntity<Void> acknowledgeRedFlag(@PathVariable Long sessionId) {
        interviewService.acknowledgeRedFlag(sessionId);
        return ResponseEntity.ok().build();
    }
}