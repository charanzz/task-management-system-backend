package com.taskmanager.repository;

import com.taskmanager.entity.UserPathEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPathEnrollmentRepository extends JpaRepository<UserPathEnrollment, Long> {
    Optional<UserPathEnrollment> findByUserIdAndExamPathId(Long userId, Long pathId);
    List<UserPathEnrollment> findByUserIdOrderByLastActivityAtDesc(Long userId);
    boolean existsByUserIdAndExamPathId(Long userId, Long pathId);
}