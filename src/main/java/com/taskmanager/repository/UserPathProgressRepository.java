package com.taskmanager.repository;

import com.taskmanager.entity.ExamPath;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserPathProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserPathProgressRepository extends JpaRepository<UserPathProgress, Long> {
    Optional<UserPathProgress> findByUserAndExamPath(User user, ExamPath examPath);
    List<UserPathProgress> findByUserAndStatus(User user, String status);
    List<UserPathProgress> findByUser(User user);
}