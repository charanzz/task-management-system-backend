package com.taskmanager.service.impl;

import com.taskmanager.dto.UserStats;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.TaskService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ==============================
    // CREATE TASK
    // ==============================
    @Override
    public Task createTask(Task task) {

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // ==============================
    // UPDATE TASK
    // ==============================
    @Override
    public Task updateTask(Long taskId, Task updatedTask) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setPriority(updatedTask.getPriority());
        task.setStatus(updatedTask.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        // ✅ If task marked as DONE, store completion time
        if (updatedTask.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    // ==============================
    // DELETE TASK
    // ==============================
    @Override
    public void deleteTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }

    // ==============================
    // GET TASKS BY USER (Paginated)
    // ==============================
    @Override
    public Page<Task> getTasksByUser(Long userId, Pageable pageable) {
        return taskRepository.findByUserId(userId, pageable);
    }

    // ==============================
    // GET TASKS BY STATUS
    // ==============================
    @Override
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    // ==============================
    // GET TASKS BY PRIORITY
    // ==============================
    @Override
    public List<Task> getTasksByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }

    // ==============================
    // USER STATS (Focus Score + Streak)
    // ==============================
    @Override
    public UserStats getUserStats(Long userId) {

        List<Task> tasks = taskRepository.findByUserId(userId);

        int total = tasks.size();
        int completed = 0;
        int score = 0;

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.DONE) {
                completed++;

                switch (task.getPriority()) {
                    case HIGH -> score += 30;
                    case MEDIUM -> score += 20;
                    case LOW -> score += 10;
                }
            }
        }

        int streak = calculateStreak(tasks);

        return new UserStats(total, completed, score, streak);
    }

    // ==============================
    // STREAK CALCULATION
    // ==============================
    private int calculateStreak(List<Task> tasks) {

        return (int) tasks.stream()
                .filter(t -> t.getCompletedAt() != null)
                .map(t -> t.getCompletedAt().toLocalDate())
                .distinct()
                .count();
    }
}
