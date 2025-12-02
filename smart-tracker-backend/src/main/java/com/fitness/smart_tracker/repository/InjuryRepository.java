package com.fitness.smart_tracker.repository;

import com.fitness.smart_tracker.model.Injury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InjuryRepository extends JpaRepository<Injury, Long> {
    List<Injury> findByUserIdAndActiveTrue(Long userId);
}