package com.taskmanager.repository;

import com.taskmanager.entity.ExamPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamPhaseRepository extends JpaRepository<ExamPhase, Long> {
    List<ExamPhase> findByExamPathIdOrderByOrderIndexAsc(Long pathId);
}