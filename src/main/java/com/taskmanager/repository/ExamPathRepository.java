package com.taskmanager.repository;

import com.taskmanager.entity.ExamPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamPathRepository extends JpaRepository<ExamPath, Long> {
    Optional<ExamPath> findBySlug(String slug);
    List<ExamPath> findAllByOrderByComingSoonAscIdAsc();
}