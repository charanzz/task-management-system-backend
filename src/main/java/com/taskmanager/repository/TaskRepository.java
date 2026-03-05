package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // User tasks (paginated + list)
    Page<Task> findByUserId(Long userId, Pageable pageable);
    List<Task> findByUserId(Long userId);

    // Filters
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);

    // Due date reminders
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end AND t.status != 'DONE'")
    List<Task> findTasksDueAround(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // Team tasks
    List<Task> findByTeamId(Long teamId);

    // Admin stats
    long countByUserId(Long userId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}