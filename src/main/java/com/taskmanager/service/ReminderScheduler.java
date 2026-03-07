package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderScheduler {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public ReminderScheduler(UserRepository userRepository,
                              TaskRepository taskRepository,
                              NotificationService notificationService,
                              EmailService emailService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    // ── Every hour: check for tasks due in next 24h ───────
    @Scheduled(cron = "0 0 * * * *")
    public void checkDueSoon() {
        System.out.println("🔔 Checking due-soon tasks...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);

        for (User user : userRepository.findAll()) {
            try {
                List<Task> tasks = taskRepository.findByUserId(user.getId());
                for (Task task : tasks) {
                    if (task.getStatus() == TaskStatus.DONE) continue;
                    if (task.getDueDate() == null) continue;
                    // Due in next 24h but not already overdue
                    if (task.getDueDate().isAfter(now) && task.getDueDate().isBefore(in24h)) {
                        notificationService.taskDueSoon(user, task);
                        sendDueSoonEmail(user, task);
                    }
                }
            } catch (Exception e) {
                System.err.println("Due-soon check failed for " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    // ── Every hour: check for overdue tasks ───────────────
    @Scheduled(cron = "0 30 * * * *")
    public void checkOverdue() {
        System.out.println("🚨 Checking overdue tasks...");
        LocalDateTime now = LocalDateTime.now();

        for (User user : userRepository.findAll()) {
            try {
                List<Task> tasks = taskRepository.findByUserId(user.getId());
                long overdueCount = tasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                    .count();

                if (overdueCount > 0) {
                    tasks.stream()
                        .filter(t -> t.getStatus() != TaskStatus.DONE)
                        .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                        .forEach(t -> notificationService.taskOverdue(user, t));
                }
            } catch (Exception e) {
                System.err.println("Overdue check failed for " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    // ── 9 PM daily: streak at risk check ─────────────────
    @Scheduled(cron = "0 0 21 * * *")
    public void checkStreakAtRisk() {
        System.out.println("🔥 Checking streak-at-risk...");
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        for (User user : userRepository.findAll()) {
            try {
                if (user.getStreak() == null || user.getStreak() == 0) continue;
                List<Task> tasks = taskRepository.findByUserId(user.getId());
                boolean completedToday = tasks.stream()
                    .anyMatch(t -> t.getCompletedAt() != null && t.getCompletedAt().isAfter(startOfDay));
                if (!completedToday) {
                    notificationService.streakAtRisk(user);
                    sendStreakRiskEmail(user);
                }
            } catch (Exception e) {
                System.err.println("Streak check failed for " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    // ── Email helpers ─────────────────────────────────────
    private void sendDueSoonEmail(User user, Task task) {
        try {
            String subject = "⏰ Task Due Soon: " + task.getTitle();
            String html = """
                <div style="font-family:sans-serif;max-width:520px;margin:0 auto;background:#0a0a0f;color:#f0f0f8;padding:32px;border-radius:16px">
                  <div style="text-align:center;margin-bottom:24px">
                    <span style="font-size:40px">⏰</span>
                    <h2 style="color:#ffd93d;font-size:22px;margin:12px 0">Task Due Soon!</h2>
                  </div>
                  <div style="background:#1a1a24;border:1px solid rgba(255,255,255,.08);border-radius:12px;padding:20px;margin-bottom:20px">
                    <p style="color:#a855f7;font-size:12px;text-transform:uppercase;letter-spacing:2px;margin:0 0 8px">TASK</p>
                    <p style="font-size:18px;font-weight:700;margin:0 0 8px">" + task.getTitle() + "</p>
                    <p style="color:#ffd93d;font-size:13px;margin:0">Due: " + task.getDueDate().toString().replace("T", " at ").substring(0, 19) + "</p>
                  </div>
                  <p style="color:#6b6b8a;font-size:12px;text-align:center">This task is due in less than 24 hours. Head to TaskFlow to complete it!</p>
                  <div style="text-align:center;margin-top:20px">
                    <a href="https://www.todoperks.online/dashboard" style="display:inline-block;padding:12px 28px;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;text-decoration:none;border-radius:10px;font-weight:700;font-size:14px">Open TaskFlow →</a>
                  </div>
                </div>
                """;
            emailService.sendEmail(user.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("Due-soon email failed: " + e.getMessage());
        }
    }

    private void sendStreakRiskEmail(User user) {
        try {
            String subject = "🔥 Your " + user.getStreak() + "-day streak is at risk!";
            String html = """
                <div style="font-family:sans-serif;max-width:520px;margin:0 auto;background:#0a0a0f;color:#f0f0f8;padding:32px;border-radius:16px">
                  <div style="text-align:center;margin-bottom:24px">
                    <span style="font-size:48px">🔥</span>
                    <h2 style="color:#ff6b6b;font-size:22px;margin:12px 0">Streak at Risk!</h2>
                  </div>
                  <p style="text-align:center;font-size:15px;color:#f0f0f8;margin-bottom:8px">
                    Hey <strong>" + user.getName() + "</strong>,
                  </p>
                  <p style="text-align:center;font-size:14px;color:#6b6b8a;margin-bottom:24px">
                    You have a <strong style="color:#ffd93d">" + user.getStreak() + "-day streak</strong> — but you haven't completed any tasks today. Complete at least one task before midnight to keep it alive!
                  </p>
                  <div style="text-align:center">
                    <a href="https://www.todoperks.online/dashboard" style="display:inline-block;padding:12px 28px;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;text-decoration:none;border-radius:10px;font-weight:700;font-size:14px">Complete a Task Now →</a>
                  </div>
                </div>
                """;
            emailService.sendEmail(user.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("Streak risk email failed: " + e.getMessage());
        }
    }
}