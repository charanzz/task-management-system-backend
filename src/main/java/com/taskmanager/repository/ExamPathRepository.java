package com.taskmanager.repository;

import com.taskmanager.entity.ExamPath;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamPathRepository extends JpaRepository<ExamPath, Long> {
    List<ExamPath> findByIsActiveTrue();
}