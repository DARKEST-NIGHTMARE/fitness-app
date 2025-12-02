package com.fitness.smart_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;

    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private String activityLevel;
    private String fitnessGoal;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;
}
