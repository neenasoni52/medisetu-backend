package com.medisetu.medisetu_backend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.medisetu.medisetu_backend.dto.request.AnswerSubmissionRequest;
import com.medisetu.medisetu_backend.dto.response.AnswerSubmissionResponse;
import com.medisetu.medisetu_backend.dto.response.InterviewReviewDTO;
import com.medisetu.medisetu_backend.dto.response.RedFlagAlertDTO;
import com.medisetu.medisetu_backend.entity.InputSource;
import com.medisetu.medisetu_backend.entity.IntakeSession;
import com.medisetu.medisetu_backend.entity.InterviewAnswer;
import com.medisetu.medisetu_backend.entity.RedFlagEvent;
import com.medisetu.medisetu_backend.entity.SessionStatus;
import com.medisetu.medisetu_backend.exception.InvalidStateException;
import com.medisetu.medisetu_backend.exception.ResourceNotFoundException;
import com.medisetu.medisetu_backend.repository.IntakeSessionRepository;
import com.medisetu.medisetu_backend.repository.InterviewAnswerRepository;
import com.medisetu.medisetu_backend.repository.RedFlagEventRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final IntakeSessionRepository sessionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final RedFlagEventRepository redFlagRepository;
    private final QuestionService questionService;
    private final GeminiClient geminiClient;

    @Value("${medisetu.ayush.enabled:false}")
    private boolean defaultAyushEnabled;

    private JsonNode redFlagRules;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new ClassPathResource("redflags.json").getInputStream());
            this.redFlagRules = root.get("rules");
        } catch (IOException e) {
            this.redFlagRules = null;
        }
    }

    @Transactional
    public AnswerSubmissionResponse submitAnswer(Long sessionId, AnswerSubmissionRequest request) {
        IntakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getCompletedAt() != null) {
            throw new InvalidStateException("Interview is already completed.");
        }

        String selectedOptionId = request.getSelectedOptionId();

        // 1. Voice input mapping via Gemini slot-extraction
        if (request.getInputSource() == InputSource.VOICE && request.getRawUtterance() != null && !request.getRawUtterance().isBlank()) {
            JsonNode mapping = geminiClient.mapSpeechToSlot(
                    request.getQuestionPrompt() != null ? request.getQuestionPrompt() : "",
                    request.getOptionsJson() != null ? request.getOptionsJson() : "[]",
                    request.getRawUtterance()
            );
            if (mapping.hasNonNull("matchedOptionId")) {
                selectedOptionId = mapping.get("matchedOptionId").asText();
            }
        }

        // 2. Upsert answer: prevent duplicate records on resubmission
        InterviewAnswer answer = answerRepository
                .findBySessionIdAndQuestionId(sessionId, request.getQuestionId())
                .orElseGet(InterviewAnswer::new);

        answer.setSessionId(sessionId);
        answer.setQuestionId(request.getQuestionId());
        answer.setModuleName(session.getActiveModule() != null ? session.getActiveModule() : "CHIEF_COMPLAINT");
        answer.setSelectedOptionId(selectedOptionId);
        answer.setAnswerText(request.getAnswerText());
        answer.setRawUtterance(request.getRawUtterance());
        answer.setInputSource(request.getInputSource() != null ? request.getInputSource() : InputSource.TAP);
        answerRepository.save(answer);

        // 3. Dynamic routing based on Chief Complaint
        if ("CC_PRIMARY".equals(request.getQuestionId())) {
            routeChiefComplaint(session, selectedOptionId);
        }

        // 4. Fetch all cumulative answers for this session
        List<InterviewAnswer> allAnswers = answerRepository.findBySessionId(sessionId);

        // 5. Evaluate Red-Flag rules against the complete set of answers
        RedFlagAlertDTO alert = evaluateRedFlag(session, allAnswers);

        // 6. Determine next question in the clinical protocol
        JsonNode nextQuestion = questionService.getNextQuestion(session, allAnswers, defaultAyushEnabled);

        if (nextQuestion == null) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
        }

        sessionRepository.save(session);

        return new AnswerSubmissionResponse(nextQuestion, alert, nextQuestion == null);
    }

    public List<InterviewReviewDTO> getInterviewReview(Long sessionId) {
        IntakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);
        List<InterviewReviewDTO> reviews = new ArrayList<>();
        String lang = session.getLanguage() != null ? session.getLanguage() : "hi";

        for (InterviewAnswer a : answers) {
            String prompt = questionService.getQuestionPrompt(a.getQuestionId(), lang);
            String optionLabel = questionService.getOptionLabel(a.getQuestionId(), a.getSelectedOptionId(), lang);

            reviews.add(InterviewReviewDTO.builder()
                    .questionId(a.getQuestionId())
                    .moduleName(a.getModuleName())
                    .prompt(prompt)
                    .selectedOptionId(a.getSelectedOptionId())
                    .selectedOptionLabel(optionLabel)
                    .answerText(a.getAnswerText())
                    .rawUtterance(a.getRawUtterance())
                    .inputSource(a.getInputSource())
                    .build());
        }

        return reviews;
    }

    @Transactional
    public void acknowledgeRedFlag(Long sessionId) {
        IntakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        // Keep redFlagRaised true for doctor's triage view, but allow continuation
        sessionRepository.save(session);
    }

    private void routeChiefComplaint(IntakeSession session, String optionId) {
        if ("OPT_CHEST_PAIN".equals(optionId)) {
            session.setActiveModule("CARDIO_RESPIRATORY");
        } else if ("OPT_ABDOMEN_PAIN".equals(optionId)) {
            session.setActiveModule("GI_ABDOMINAL");
        } else if ("OPT_FEVER".equals(optionId)) {
            session.setActiveModule("FEVER_GENERAL");
        } else {
            session.setActiveModule("CARDIO_RESPIRATORY");
        }
    }

    private RedFlagAlertDTO evaluateRedFlag(IntakeSession session, List<InterviewAnswer> answers) {
        Set<String> selectedOptions = new HashSet<>();
        for (InterviewAnswer a : answers) {
            if (a.getSelectedOptionId() != null) {
                selectedOptions.add(a.getSelectedOptionId());
            }
        }

        String sessionLang = session.getLanguage() != null ? session.getLanguage() : "hi";

        if (redFlagRules != null && redFlagRules.isArray()) {
            // First evaluate CRITICAL rules
            for (JsonNode rule : redFlagRules) {
                String severity = rule.path("severity").asText("NONE");
                if (!"CRITICAL".equalsIgnoreCase(severity)) continue;

                if (ruleMatches(rule, selectedOptions)) {
                    String ruleId = rule.path("id").asText("RF_UNKNOWN");
                    String message = getLocalizedRuleMessage(rule, sessionLang);

                    session.setRedFlagRaised(true);
                    session.setRedFlagSeverity("CRITICAL");

                    RedFlagEvent event = new RedFlagEvent();
                    event.setSessionId(session.getId());
                    event.setRuleId(ruleId);
                    event.setSeverity("CRITICAL");
                    event.setTriggerDetails("Red flag rule matched: " + ruleId + " with options: " + selectedOptions);
                    redFlagRepository.save(event);

                    return RedFlagAlertDTO.builder()
                            .raised(true)
                            .severity("CRITICAL")
                            .ruleId(ruleId)
                            .message(message)
                            .build();
                }
            }

            // Next evaluate YELLOW rules
            for (JsonNode rule : redFlagRules) {
                String severity = rule.path("severity").asText("NONE");
                if (!"YELLOW".equalsIgnoreCase(severity)) continue;

                if (ruleMatches(rule, selectedOptions)) {
                    String ruleId = rule.path("id").asText("RF_YELLOW");
                    String message = getLocalizedRuleMessage(rule, sessionLang);

                    if (!"CRITICAL".equals(session.getRedFlagSeverity())) {
                        session.setRedFlagSeverity("YELLOW");
                    }

                    return RedFlagAlertDTO.builder()
                            .raised(true)
                            .severity("YELLOW")
                            .ruleId(ruleId)
                            .message(message)
                            .build();
                }
            }
        }

        return RedFlagAlertDTO.builder()
                .raised(false)
                .severity("NONE")
                .message(null)
                .build();
    }

    private boolean ruleMatches(JsonNode rule, Set<String> selectedOptions) {
        JsonNode requiredOptions = rule.get("requiredOptions");
        if (requiredOptions != null && requiredOptions.isArray() && !requiredOptions.isEmpty()) {
            for (JsonNode req : requiredOptions) {
                if (!selectedOptions.contains(req.asText())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private String getLocalizedRuleMessage(JsonNode rule, String language) {
        JsonNode messageNode = rule.get("message");
        if (messageNode == null) return "Clinical warning detected.";
        if (messageNode.isTextual()) return messageNode.asText();

        if (messageNode.hasNonNull(language)) {
            return messageNode.get(language).asText();
        }
        if (messageNode.hasNonNull("en")) {
            return messageNode.get("en").asText();
        }
        return "Clinical warning detected.";
    }
}