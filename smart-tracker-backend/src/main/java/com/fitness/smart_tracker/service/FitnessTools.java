package com.fitness.smart_tracker.service;

import com.fitness.smart_tracker.model.User;
import com.fitness.smart_tracker.repository.UserRepository;
import dev.langchain4j.agent.tool.P; // Parameter annotation
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FitnessTools {

    @Autowired
    private UserRepository userRepository;
    @Tool("Updates the user's fitness goal in the database profile.")
    public String updateFitnessGoal(
            @P("The numeric ID of the user") Long userId,
            @P("The new fitness goal (e.g. Muscle Gain, Weight Loss, Stamina)") String newGoal
    ) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String oldGoal = user.getFitnessGoal();
            user.setFitnessGoal(newGoal);
            userRepository.save(user);

            return "Success: Goal updated from '" + oldGoal + "' to '" + newGoal + "'.";
        } catch (Exception e) {
            return "Error updating goal: " + e.getMessage();
        }
    }
}