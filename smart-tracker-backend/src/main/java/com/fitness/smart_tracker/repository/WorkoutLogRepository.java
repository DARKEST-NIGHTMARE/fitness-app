package com.fitness.smart_tracker.repository;

import com.fitness.smart_tracker.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findByUserId(Long userId);
}
