package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public TaskReminderScheduler(TaskRepository taskRepository, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    // Runs every 30 minutes
    @Scheduled(fixedRate = 1800000)
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();

        // 1 hour before
        LocalDateTime oneHourFrom = now.plusHours(1);
        List<Task> oneHourTasks = taskRepository.findTasksDueAround(
            oneHourFrom.minusMinutes(15),
            oneHourFrom.plusMinutes(15)
        );
        for (Task task : oneHourTasks) {
            if (task.getStatus() != TaskStatus.DONE && task.getUser() != null) {
                emailService.sendTaskReminder(task.getUser(), task, "in 1 hour");
            }
        }

        // 1 day before
        LocalDateTime oneDayFrom = now.plusDays(1);
        List<Task> oneDayTasks = taskRepository.findTasksDueAround(
            oneDayFrom.minusMinutes(15),
            oneDayFrom.plusMinutes(15)
        );
        for (Task task : oneDayTasks) {
            if (task.getStatus() != TaskStatus.DONE && task.getUser() != null) {
                emailService.sendTaskReminder(task.getUser(), task, "tomorrow");
            }
        }
    }
}