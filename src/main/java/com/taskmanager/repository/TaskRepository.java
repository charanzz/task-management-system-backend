package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // For pagination
    Page<Task> findByUserId(Long userId, Pageable pageable);

    // For stats (non-paginated)
    List<Task> findByUserId(Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end AND t.status != 'DONE'")
    List<Task> findTasksDueAround(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    List<Task> findByTeamId(Long teamId);
    long countByUserId(Long userId);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
