package com.fitness.smart_tracker.service;

import com.fitness.smart_tracker.model.User;
import com.fitness.smart_tracker.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {

    @Autowired private FitnessAssistant assistant;
    @Autowired private UserRepository userRepository;

    public String chatWithUser(Long userId, String userMessage) {
        User user = userRepository.findById(userId).orElseThrow();

        String profile = String.format("Goal: %s, Weight: %.1fkg, Activity: %s",
                user.getFitnessGoal(), user.getWeight(), user.getActivityLevel());

        String summary = user.getAiSummary();
        if (summary == null) summary = "No prior history.";
        String response = assistant.chat(userId, userMessage, profile, summary, userId);

        updateLongTermMemory(user, userMessage, response);

        return response;
    }

    private void updateLongTermMemory(User user, String userMsg, String aiMsg) {
        String oldSummary = user.getAiSummary();
        if (oldSummary == null) oldSummary = "";
        String interaction = "User: " + userMsg + "\nAI: " + aiMsg;
        String newSummary = assistant.updateSummary("Update memory", oldSummary, interaction);
        user.setAiSummary(newSummary);
        userRepository.save(user);
    }

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final WebClient webClient;

    public GeminiService() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        this.webClient = WebClient.builder().uriBuilderFactory(factory).build();
    }

    public String getWorkoutPlan(String prompt, String userDetails) {
        if (apiKey == null || apiKey.length() < 10) return "[]";

        String modelName = "gemini-2.5-flash";
        String finalUrl = BASE_URL + modelName + ":generateContent?key=" + apiKey.trim();

        String fullPrompt =
                "You are a fitness API. Generate a workout based on this request: '" + prompt + "'. " +
                        "User Details: " + userDetails + ". " +
                        "RULES: 1. If time mentioned, duration must match. 2. If not fitness related, return JSON name='Error'. " +
                        "CRITICAL: Return JSON Array: [{ \"name\": \"...\", \"sets\": \"...\", \"reps\": \"...\", \"duration\": \"...\", \"description\": \"...\" }]";

        return callGemini(finalUrl, fullPrompt);
    }

    public String generateProgressInsights(String userProfile, String weightHistory, String recentWorkouts) {
        if (apiKey == null || apiKey.length() < 10) return "{}";

        String modelName = "gemini-2.5-flash";
        String finalUrl = BASE_URL + modelName + ":generateContent?key=" + apiKey.trim();

        String fullPrompt =
                "Act as an expert Fitness & Nutrition Coach. Analyze this user data:\n" +
                        "PROFILE: " + userProfile + "\n" +
                        "WEIGHT HISTORY (Oldest to Newest): " + weightHistory + "\n" +
                        "RECENT WORKOUTS: " + recentWorkouts + "\n\n" +

                        "TASK: Generate a daily progress report.\n" +
                        "1. STATUS: Analyze if they are moving towards their goal (e.g., if goal is weight loss, is weight trending down?).\n" +
                        "2. ADVICE: Give specific feedback based on the trends.\n" +
                        "3. NUTRITION: Provide a sample daily meal plan (calories/macros) tailored to their GOAL and current PROGRESS.\n\n" +

                        "OUTPUT FORMAT: Return STRICT JSON (No Markdown):\n" +
                        "{\n" +
                        "  \"status\": \"On Track / Needs Attention\",\n" +
                        "  \"summary\": \"Your weight is down 1kg this week...\",\n" +
                        "  \"advice\": \"Focus on increasing protein...\",\n" +
                        "  \"nutrition\": {\n" +
                        "     \"calories\": \"2400\",\n" +
                        "     \"protein\": \"160g\",\n" +
                        "     \"plan\": [\"Breakfast: Oatmeal...\", \"Lunch: Chicken Salad...\"]\n" +
                        "  }\n" +
                        "}";

        return callGemini(finalUrl, fullPrompt);
    }

    private String callGemini(String url, String prompt) {
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
            return "{}";
        }
    }
}