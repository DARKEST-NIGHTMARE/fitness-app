package com.fitness.smart_tracker.service;

import com.fitness.smart_tracker.model.*;
import com.fitness.smart_tracker.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Autowired private FitnessAssistant assistant;
    @Autowired private UserRepository userRepository;
    @Autowired private InjuryRepository injuryRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final WebClient webClient;

    public GeminiService() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        this.webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .build();
    }

    public String chatWithUser(Long userId, String userMessage) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Injury> injuries = injuryRepository.findByUserIdAndActiveTrue(userId);
        String healthStatus = "HEALTHY";

        if (!injuries.isEmpty()) {
            Injury injury = injuries.get(0);
            if (LocalDate.now().isBefore(injury.getEstimatedRecoveryDate())) {
                healthStatus = "CRITICAL: INJURED. TYPE: " + injury.getDescription() +
                        ". RECOVERY UNTIL: " + injury.getEstimatedRecoveryDate();
            } else {
                injury.setActive(false);
                injuryRepository.save(injury);
            }
        }

        String profile = String.format(
                "Age: %d, Gender: %s, Height: %.1fcm, Weight: %.1fkg, Activity: %s, Goal: %s",
                user.getAge() != null ? user.getAge() : 0,
                user.getGender() != null ? user.getGender() : "Unknown",
                user.getHeight() != null ? user.getHeight() : 0.0,
                user.getWeight() != null ? user.getWeight() : 0.0,
                user.getActivityLevel() != null ? user.getActivityLevel() : "Moderate",
                user.getFitnessGoal()
        );

        String summary = user.getAiSummary();
        if (summary == null) summary = "No prior history.";

        String response = assistant.chat(userId, userMessage, profile, summary, userId, healthStatus);

        updateLongTermMemory(user, userMessage, response);

        return response;
    }

    private void updateLongTermMemory(User user, String userMsg, String aiMsg) {
        String oldSummary = user.getAiSummary();
        if (oldSummary == null) oldSummary = "";

        String interaction = "User: " + userMsg + "\nAI: " + aiMsg;
        String newSummary = assistant.updateSummary("Update", oldSummary, interaction);

        user.setAiSummary(newSummary);
        userRepository.save(user);
    }
    public String getWorkoutPlan(String prompt, String userDetails) {
        if (apiKey == null || apiKey.length() < 10) return "[]";

        String modelName = "gemini-2.5-flash";
        String finalUrl = BASE_URL + modelName + ":generateContent?key=" + apiKey.trim();

        String fullPrompt =
                "You are a fitness API. Generate a workout based on this request: '" + prompt + "'. " +
                        "User Details: " + userDetails + ". " +
                        "RULES: 1. If user mentions time, duration must match. 2. If request not fitness related, return JSON name='Error'. " +
                        "CRITICAL: Return a JSON Array matching this EXACT structure: " +
                        "[ { \"name\": \"Squats\", \"sets\": \"3\", \"reps\": \"12\", \"duration\": \"5 mins\", \"description\": \"...\" } ]" +
                        "Provide 5-6 exercises. Ensure 'duration' field exists. No Markdown.";

        return callGeminiDirect(finalUrl, fullPrompt);
    }

    public String generateProgressInsights(String userProfile, String weightHistory, String recentWorkouts) {
        if (apiKey == null || apiKey.length() < 10) return "{}";

        String modelName = "gemini-2.5-flash";
        String finalUrl = BASE_URL + modelName + ":generateContent?key=" + apiKey.trim();

        String fullPrompt =
                "Act as an expert Fitness & Nutrition Coach. Analyze this data:\n" +
                        "PROFILE: " + userProfile + "\n" +
                        "WEIGHT HISTORY: " + weightHistory + "\n" +
                        "RECENT WORKOUTS: " + recentWorkouts + "\n\n" +
                        "TASK: Generate daily progress report.\n" +
                        "OUTPUT FORMAT: Return STRICT JSON (No Markdown):\n" +
                        "{\n" +
                        "  \"status\": \"On Track / Needs Attention\",\n" +
                        "  \"summary\": \"...\",\n" +
                        "  \"advice\": \"...\",\n" +
                        "  \"nutrition\": { \"calories\": \"...\", \"protein\": \"...\", \"plan\": [\"...\"] }\n" +
                        "}";

        return callGeminiDirect(finalUrl, fullPrompt);
    }

    private String callGeminiDirect(String url, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );
        try {
            return webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return "[]";
        }
    }
}