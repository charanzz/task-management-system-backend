package com.taskmanager.repository;

import com.taskmanager.entity.ExamPath;
import com.taskmanager.entity.PathPhase;
import com.taskmanager.entity.PathTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PathTaskRepository extends JpaRepository<PathTask, Long> {
    List<PathTask> findByPhaseOrderByTaskNumber(PathPhase phase);
    Optional<PathTask> findByExamPathAndTaskNumber(ExamPath examPath, Integer taskNumber);
    List<PathTask> findByExamPathOrderByTaskNumber(ExamPath examPath);
}