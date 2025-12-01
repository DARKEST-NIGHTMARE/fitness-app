package com.fitness.smart_tracker.controller;

import com.fitness.smart_tracker.model.*;
import com.fitness.smart_tracker.repository.*;
import com.fitness.smart_tracker.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AppController {

    @Autowired private UserRepository userRepository;
    @Autowired private WorkoutLogRepository logRepository;
    @Autowired private BodyWeightLogRepository weightLogRepository;
    @Autowired private GeminiService geminiService;

    @PostMapping("/register")
    public User register(@RequestBody User user) { return userRepository.save(user); }

    @PostMapping("/login")
    public User login(@RequestBody User loginData) {
        Optional<User> user = userRepository.findByUsername(loginData.getUsername());
        if(user.isPresent() && user.get().getPassword().equals(loginData.getPassword())) return user.get();
        throw new RuntimeException("Invalid login");
    }

    @GetMapping("/user/{userId}")
    public User getUser(@PathVariable Long userId) { return userRepository.findById(userId).orElseThrow(); }

    @PutMapping("/user/{userId}")
    public User updateUser(@PathVariable Long userId, @RequestBody User u) {
        return userRepository.findById(userId).map(existing -> {
            existing.setWeight(u.getWeight()); existing.setHeight(u.getHeight());
            existing.setAge(u.getAge()); existing.setGender(u.getGender());
            existing.setActivityLevel(u.getActivityLevel()); existing.setFitnessGoal(u.getFitnessGoal());
            return userRepository.save(existing);
        }).orElseThrow();
    }

    @PostMapping("/log-weight")
    public BodyWeightLog logBodyWeight(@RequestBody BodyWeightLog log) {
        User user = userRepository.findById(log.getUserId()).orElseThrow();
        user.setWeight(log.getWeight());
        userRepository.save(user);

        log.setDateRecorded(LocalDate.now());
        return weightLogRepository.save(log);
    }

    @GetMapping("/weight-history/{userId}")
    public List<BodyWeightLog> getWeightHistory(@PathVariable Long userId) {
        return weightLogRepository.findByUserIdOrderByDateRecordedAsc(userId);
    }

    @GetMapping("/analyze-progress/{userId}")
    public String analyzeProgress(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<BodyWeightLog> weightLogs = weightLogRepository.findByUserIdOrderByDateRecordedAsc(userId);
        List<WorkoutLog> workouts = logRepository.findByUserId(userId);

        String userProfile = String.format("Goal: %s, Current Weight: %.1fkg, Height: %.1fcm, Activity: %s",
                user.getFitnessGoal(), user.getWeight(), user.getHeight(), user.getActivityLevel());

        String weightHistory = weightLogs.stream()
                .map(w -> w.getDateRecorded() + ":" + w.getWeight() + "kg")
                .collect(Collectors.joining(", "));

        String recentWorkouts = workouts.stream()
                .sorted((a, b) -> b.getDateCompleted().compareTo(a.getDateCompleted()))
                .limit(10)
                .map(w -> String.format("%s: %s (Weight: %.1f, Time: %d)",
                        w.getDateCompleted(), w.getExerciseName(),
                        w.getWeightLifted() != null ? w.getWeightLifted() : 0.0,
                        w.getDurationMinutes()))
                .collect(Collectors.joining("; "));

        return geminiService.generateProgressInsights(userProfile, weightHistory, recentWorkouts);
    }

    @PostMapping("/generate-workout")
    public String generateWorkout(@RequestParam Long userId, @RequestBody String userPrompt) {
        User user = userRepository.findById(userId).orElseThrow();
        String context = "Goal: " + user.getFitnessGoal();
        return geminiService.getWorkoutPlan(userPrompt, context);
    }

    @PostMapping("/log-workout")
    public WorkoutLog logWorkout(@RequestBody WorkoutLog log) { return logRepository.save(log); }

    @GetMapping("/history/{userId}")
    public List<WorkoutLog> getHistory(@PathVariable Long userId) { return logRepository.findByUserId(userId); }
}