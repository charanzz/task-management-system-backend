package com.taskmanager.repository;

import com.taskmanager.entity.ExamTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamTopicRepository extends JpaRepository<ExamTopic, Long> {
    List<ExamTopic> findByPhaseIdOrderByOrderIndexAsc(Long phaseId);

    @Query("SELECT COUNT(t) FROM ExamTopic t WHERE t.phase.examPath.id = :pathId")
    int countByPathId(@Param("pathId") Long pathId);
}