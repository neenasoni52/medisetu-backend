package com.medisetu.medisetu_backend.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GeminiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public GeminiClient(@Value("${gemini.base.url:https://generativelanguage.googleapis.com}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode mapSpeechToSlot(String questionPrompt, String optionsJson, String spokenText) {
        if (apiKey == null || apiKey.isBlank()) {
            return objectMapper.createObjectNode()
                    .putNull("matchedOptionId")
                    .put("isNegated", false)
                    .putNull("targetModule");
        }

        String systemInstruction = """
            You are a medical triage slot extractor. 
            Map the patient's spoken words to ONE of the valid option IDs, respecting negations.
            Respond ONLY with JSON matching: {"matchedOptionId": "STRING or null", "isNegated": boolean, "targetModule": "STRING or null"}
            """;

        String prompt = "Question: " + questionPrompt + "\nOptions: " + optionsJson + "\nPatient said: \"" + spokenText + "\"";

        String requestBody = """
        {
          "system_instruction": { "parts": [{ "text": %s }] },
          "contents": [{ "parts": [{ "text": %s }] }],
          "generationConfig": { "response_mime_type": "application/json" }
        }
        """.formatted(objectMapper.valueToTree(systemInstruction).toString(), objectMapper.valueToTree(prompt).toString());

        try {
            String rawResponse = restClient.post()
                    .uri("/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            String jsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            return objectMapper.readTree(jsonText);
        } catch (Exception e) {
            return objectMapper.createObjectNode()
                    .putNull("matchedOptionId")
                    .put("isNegated", false)
                    .putNull("targetModule");
        }
    }
}