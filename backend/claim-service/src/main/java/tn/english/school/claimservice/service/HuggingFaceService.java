package tn.english.school.claimservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.english.school.claimservice.entity.Claim;

import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${groq.api.token}")
    private String apiToken;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String draftAdminResponse(Claim claim) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "You are a professional academic administrator. " +
                        "Write concise, empathetic and formal responses to student claims. " +
                        "Keep responses to 2-3 sentences maximum. Do not add greetings or sign-offs."),
                    Map.of("role", "user", "content",
                        String.format(
                            "Write a professional administrator response to this student claim:\n\n" +
                            "Subject: %s\nType: %s\nDescription: %s",
                            claim.getSubject(), claim.getType(), claim.getDescription()
                        ))
                ),
                "max_tokens", 150,
                "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_API_URL, request, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return ((String) message.get("content")).trim();

        } catch (Exception e) {
            throw new IllegalStateException("AI draft failed: " + e.getMessage());
        }
    }
}
