package com.fitness.smart_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "workout_logs")
public class WorkoutLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String exerciseName;
    private int reps;
    private int sets;
    private Double weightLifted;
    private Integer durationMinutes;
    private LocalDate dateCompleted = LocalDate.now();
}
