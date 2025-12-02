package com.fitness.smart_tracker.repository;

import com.fitness.smart_tracker.model.BodyWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyWeightLogRepository extends JpaRepository<BodyWeightLog, Long> {
    List<BodyWeightLog> findByUserIdOrderByDateRecordedAsc(Long userId);
    Optional<BodyWeightLog> findByUserIdAndDateRecorded(Long userId, LocalDate dateRecorded);
}