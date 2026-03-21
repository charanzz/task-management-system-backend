package com.taskmanager.repository;

import com.taskmanager.entity.ExamPath;
import com.taskmanager.entity.PathPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PathPhaseRepository extends JpaRepository<PathPhase, Long> {
    List<PathPhase> findByExamPathOrderByPhaseNumber(ExamPath examPath);
}