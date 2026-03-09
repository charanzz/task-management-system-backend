package com.taskmanager.repository;

import com.taskmanager.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdAndArchivedFalse(Long userId);
    List<Habit> findByUserId(Long userId);
}