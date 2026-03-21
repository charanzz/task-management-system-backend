package com.taskmanager.repository;

import com.taskmanager.entity.PathQuestion;
import com.taskmanager.entity.PathTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PathQuestionRepository extends JpaRepository<PathQuestion, Long> {
    List<PathQuestion> findByPathTaskOrderByQuestionNumber(PathTask pathTask);
}