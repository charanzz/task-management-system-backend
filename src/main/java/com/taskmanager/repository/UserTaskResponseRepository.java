package com.taskmanager.repository;

import com.taskmanager.entity.PathTask;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserTaskResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserTaskResponseRepository extends JpaRepository<UserTaskResponse, Long> {
    Optional<UserTaskResponse> findByUserAndPathTask(User user, PathTask pathTask);
    List<UserTaskResponse> findByUserAndCompleted(User user, Boolean completed);
    List<UserTaskResponse> findByUser(User user);
}