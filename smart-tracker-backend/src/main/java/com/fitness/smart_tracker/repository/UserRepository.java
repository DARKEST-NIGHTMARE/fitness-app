package com.fitness.smart_tracker.repository;

import com.fitness.smart_tracker.model.User;
import com.fitness.smart_tracker.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
