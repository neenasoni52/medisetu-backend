package com.medisetu.medisetu_backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.medisetu.medisetu_backend.entity.IntakeSession;
import com.medisetu.medisetu_backend.entity.InterviewAnswer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private JsonNode modules;
    private final Map<String, JsonNode> questionIndex = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new ClassPathResource("questions.json").getInputStream());
        this.modules = root.get("modules");

        // Index all questions for fast lookup by questionId
        if (modules != null) {
            List<String> moduleNames = List.of(
                    "CHIEF_COMPLAINT",
                    "CARDIO_RESPIRATORY",
                    "GI_ABDOMINAL",
                    "FEVER_GENERAL",
                    "SYSTEMIC_REVIEW",
                    "AYUSH_ASSESSMENT"
            );
            for (String moduleName : moduleNames) {
                JsonNode qArray = modules.get(moduleName);
                if (qArray != null && qArray.isArray()) {
                    for (JsonNode q : qArray) {
                        if (q.hasNonNull("id")) {
                            questionIndex.put(q.get("id").asText(), q);
                        }
                    }
                }
            }
        }
    }

    public JsonNode getNextQuestion(IntakeSession session, List<InterviewAnswer> answers, boolean isAyushEnabled) {
        String currentModule = session.getActiveModule();
        if (currentModule == null || currentModule.isBlank()) {
            currentModule = "CHIEF_COMPLAINT";
            session.setActiveModule(currentModule);
        }

        Set<String> answeredIds = answers.stream()
                .map(InterviewAnswer::getQuestionId)
                .collect(Collectors.toSet());

        // 1. Check if current module has remaining unanswered questions
        JsonNode currentModuleQuestions = modules != null ? modules.get(currentModule) : null;
        if (currentModuleQuestions != null && currentModuleQuestions.isArray()) {
            for (JsonNode q : currentModuleQuestions) {
                if (!answeredIds.contains(q.get("id").asText())) {
                    return q;
                }
            }
        }

        // 2. All questions in currentModule are answered, determine module transition:
        if ("CHIEF_COMPLAINT".equals(currentModule)) {
            // Check CC_PRIMARY answer to route to condition-specific module
            String routedModule = "CARDIO_RESPIRATORY";
            for (InterviewAnswer a : answers) {
                if ("CC_PRIMARY".equals(a.getQuestionId()) && a.getSelectedOptionId() != null) {
                    if ("OPT_ABDOMEN_PAIN".equals(a.getSelectedOptionId())) {
                        routedModule = "GI_ABDOMINAL";
                    } else if ("OPT_FEVER".equals(a.getSelectedOptionId())) {
                        routedModule = "FEVER_GENERAL";
                    } else {
                        routedModule = "CARDIO_RESPIRATORY";
                    }
                    break;
                }
            }
            session.setActiveModule(routedModule);
            return getNextQuestion(session, answers, isAyushEnabled);
        }

        if ("CARDIO_RESPIRATORY".equals(currentModule) 
                || "GI_ABDOMINAL".equals(currentModule) 
                || "FEVER_GENERAL".equals(currentModule)) {
            // After condition-specific module, proceed to systemic review
            session.setActiveModule("SYSTEMIC_REVIEW");
            return getNextQuestion(session, answers, isAyushEnabled);
        }

        if ("SYSTEMIC_REVIEW".equals(currentModule)) {
            // If AYUSH is enabled in config, proceed to AYUSH assessment; otherwise done
            if (isAyushEnabled) {
                session.setActiveModule("AYUSH_ASSESSMENT");
                return getNextQuestion(session, answers, true);
            }
            return null;
        }

        if ("AYUSH_ASSESSMENT".equals(currentModule)) {
            // All AYUSH questions complete
            return null;
        }

        return null;
    }

    public JsonNode getQuestionById(String questionId) {
        return questionIndex.get(questionId);
    }

    public String getQuestionPrompt(String questionId, String language) {
        JsonNode q = questionIndex.get(questionId);
        if (q == null || !q.has("prompt")) {
            return questionId;
        }
        JsonNode prompt = q.get("prompt");
        if (prompt.hasNonNull(language)) {
            return prompt.get(language).asText();
        }
        return prompt.hasNonNull("en") ? prompt.get("en").asText() : questionId;
    }

    public String getOptionLabel(String questionId, String optionId, String language) {
        if (optionId == null) return null;
        JsonNode q = questionIndex.get(questionId);
        if (q == null || !q.has("options")) return optionId;

        for (JsonNode opt : q.get("options")) {
            if (opt.hasNonNull("id") && optionId.equals(opt.get("id").asText())) {
                if (opt.has("label")) {
                    JsonNode label = opt.get("label");
                    if (label.hasNonNull(language)) {
                        return label.get(language).asText();
                    }
                    if (label.hasNonNull("en")) {
                        return label.get("en").asText();
                    }
                }
                return optionId;
            }
        }
        return optionId;
    }
}