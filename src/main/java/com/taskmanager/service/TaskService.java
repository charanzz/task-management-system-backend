package com.taskmanager.service;

import com.taskmanager.dto.UserStats;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {

    Task createTask(Task task);

    Task updateTask(Long taskId, Task task);

    void deleteTask(Long taskId);
    
    UserStats getUserStats(Long userId);


    Page<Task> getTasksByUser(Long userId, Pageable pageable);

    List<Task> getTasksByStatus(TaskStatus status);

    List<Task> getTasksByPriority(TaskPriority priority);
}
