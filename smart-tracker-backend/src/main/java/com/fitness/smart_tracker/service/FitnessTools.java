package com.fitness.smart_tracker.service;

import com.fitness.smart_tracker.model.Injury;
import com.fitness.smart_tracker.model.User;
import com.fitness.smart_tracker.repository.InjuryRepository;
import com.fitness.smart_tracker.repository.UserRepository;
import dev.langchain4j.agent.tool.P; // Parameter annotation
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FitnessTools {

    @Autowired private UserRepository userRepository;
    @Autowired private InjuryRepository injuryRepository;
    @Tool("Report a medical injury.Call this when user mentions pain or injury")
    public String reportInjury(
            @P("User ID")Long userId,
            @P("Injury description (e.g. 'Shoulder Dislocation','knee Pain','Leg Injury')")String description
    ){
        Injury injury = new Injury();
        injury.setUserId(userId);
        injury.setDescription(description);
        injury.setDateReported(LocalDate.now());
        injury.setActive(true);
        long recoveryDays = 3;
        if (description.toLowerCase().contains("dislocation")) recoveryDays = 14;
        else if (description.toLowerCase().contains("fracture")) recoveryDays = 42;
        else if (description.toLowerCase().contains("sprain")) recoveryDays = 7;
        injury.setEstimatedRecoveryDate(LocalDate.now().plusDays(recoveryDays));
        injuryRepository.save(injury);

        return String.format("Injury logged. SAFETY LOCKOUT ACTIVE until %s (%d days). Workout generation is now BLOCKED.",
                injury.getEstimatedRecoveryDate(), recoveryDays);

    }
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