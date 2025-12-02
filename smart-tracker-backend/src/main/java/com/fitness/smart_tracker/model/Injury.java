package com.fitness.smart_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "injuries")
public class Injury {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String description;

    private LocalDate dateReported;
    private LocalDate estimatedRecoveryDate;

    private boolean active;
}