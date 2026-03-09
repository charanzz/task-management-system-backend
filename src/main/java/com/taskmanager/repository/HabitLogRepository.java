package com.taskmanager.repository;

import com.taskmanager.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabitId(Long habitId);
    Optional<HabitLog> findByHabitIdAndLogDate(Long habitId, LocalDate date);
    List<HabitLog> findByHabitIdAndLogDateBetween(Long habitId, LocalDate from, LocalDate to);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId AND hl.logDate >= :from")
    List<HabitLog> findByUserIdSince(@Param("userId") Long userId, @Param("from") LocalDate from);
}