package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AIScheduler {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AIService aiService;
    private final EmailService emailService;

    public AIScheduler(UserRepository userRepository, TaskRepository taskRepository,
                       AIService aiService, EmailService emailService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.aiService = aiService;
        this.emailService = emailService;
    }

    // Every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyDigests() {
        System.out.println("☀️ Sending daily AI digests...");
        for (User user : userRepository.findAll()) {
            try {
                List<Task> tasks = taskRepository.findByUserId(user.getId());
                if (tasks.isEmpty()) continue;
                String digest = aiService.generateDailyDigest(user, tasks);
                emailService.sendAIDigestEmail(user, digest, "Daily");
                System.out.println("✅ Daily digest → " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Digest failed for " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    // Every Monday at 9:00 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyCoach() {
        System.out.println("🧠 Sending weekly coach emails...");
        for (User user : userRepository.findAll()) {
            try {
                List<Task> tasks = taskRepository.findByUserId(user.getId());
                int focusScore = user.getFocusScore() != null ? user.getFocusScore() : 0;
                int streak = user.getStreak() != null ? user.getStreak() : 0;
                String tips = aiService.generateWeeklyCoach(user, tasks, focusScore, streak);
                emailService.sendAIDigestEmail(user, tips, "Weekly Coach");
                System.out.println("✅ Weekly coach → " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Coach failed for " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }
}