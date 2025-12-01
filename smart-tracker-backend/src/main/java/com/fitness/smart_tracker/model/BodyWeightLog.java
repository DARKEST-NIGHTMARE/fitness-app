package com.fitness.smart_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "body_weight_logs")
public class BodyWeightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double weight;
    private LocalDate dateRecorded = LocalDate.now();
}
