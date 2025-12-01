package com.fitness.smart_tracker.repository;

import com.fitness.smart_tracker.model.BodyWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BodyWeightLogRepository extends JpaRepository<BodyWeightLog, Long> {
    List<BodyWeightLog> findByUserIdOrderByDateRecordedAsc(Long userId);
}