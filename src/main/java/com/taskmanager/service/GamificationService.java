package com.taskmanager.service;

import com.taskmanager.entity.Badge;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.BadgeRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamificationService {

    private final BadgeRepository badgeRepository;
    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public GamificationService(BadgeRepository badgeRepository,
                                TaskRepository taskRepository,
                                EmailService emailService) {
        this.badgeRepository = badgeRepository;
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    public void checkAndAwardBadges(User user) {
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long highPriDone = tasks.stream()
            .filter(t -> t.getStatus() == TaskStatus.DONE && t.getPriority().name().equals("HIGH"))
            .count();

        awardIfNew(user, "First Step",     "🌱", "Complete your first task",          completed >= 1);
        awardIfNew(user, "Getting Started","⚡", "Complete 5 tasks",                   completed >= 5);
        awardIfNew(user, "On Fire",        "🔥", "Complete 10 tasks",                  completed >= 10);
        awardIfNew(user, "Unstoppable",    "💪", "Complete 25 tasks",                  completed >= 25);
        awardIfNew(user, "Legend",         "👑", "Complete 50 tasks",                  completed >= 50);
        awardIfNew(user, "High Achiever",  "🎯", "Complete 5 high priority tasks",     highPriDone >= 5);
        awardIfNew(user, "Priority Master","🏆", "Complete 20 high priority tasks",    highPriDone >= 20);
    }

    private void awardIfNew(User user, String name, String emoji, String desc, boolean condition) {
        if (condition && !badgeRepository.existsByUserIdAndName(user.getId(), name)) {
            Badge badge = new Badge(name, emoji, desc, user);
            badgeRepository.save(badge);
            emailService.sendBadgeEmail(user, name, emoji);
        }
    }

    public List<Badge> getUserBadges(Long userId) {
        return badgeRepository.findByUserId(userId);
    }

    public int getUserLevel(int focusScore) {
        if (focusScore >= 1000) return 10;
        if (focusScore >= 500)  return 8;
        if (focusScore >= 250)  return 6;
        if (focusScore >= 100)  return 4;
        if (focusScore >= 50)   return 2;
        return 1;
    }
}