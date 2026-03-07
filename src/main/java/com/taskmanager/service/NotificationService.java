package com.taskmanager.service;

import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Notification.NotifType;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public Notification create(User user, NotifType type, String title, String body, String link) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link != null ? link : "/dashboard");
        return repo.save(n);
    }

    public void taskDueSoon(User user, Task task) {
        // Avoid duplicate — check if one already exists for this task today
        create(user, NotifType.DUE_SOON,
            "⏰ Due Soon: " + task.getTitle(),
            "This task is due in less than 24 hours.",
            "/dashboard");
    }

    public void taskOverdue(User user, Task task) {
        create(user, NotifType.OVERDUE,
            "🚨 Overdue: " + task.getTitle(),
            "This task is past its due date. Take action now!",
            "/dashboard");
    }

    public void taskCompleted(User user, Task task, int pts) {
        create(user, NotifType.TASK_DONE,
            "✅ Task Completed!",
            "\"" + task.getTitle() + "\" done! +" + pts + " focus points earned.",
            "/dashboard");
    }

    public void badgeEarned(User user, String badgeName, String badgeEmoji) {
        create(user, NotifType.BADGE_EARNED,
            badgeEmoji + " New Badge: " + badgeName,
            "You unlocked the \"" + badgeName + "\" badge. Keep it up!",
            "/dashboard");
    }

    public void levelUp(User user, int newLevel) {
        create(user, NotifType.LEVEL_UP,
            "⚡ Level Up! You're now Level " + newLevel,
            "Amazing progress! You've reached Level " + newLevel + ". Keep crushing it!",
            "/dashboard");
    }

    public void streakAtRisk(User user) {
        create(user, NotifType.STREAK_AT_RISK,
            "🔥 Streak at Risk!",
            "You haven't completed any tasks today. Complete one to keep your streak alive!",
            "/dashboard");
    }

    public void teamInvite(User user, String teamName, String inviterName) {
        create(user, NotifType.TEAM_INVITE,
            "👥 Team Invite: " + teamName,
            inviterName + " invited you to join \"" + teamName + "\".",
            "/dashboard");
    }

    public void newTeamMessage(User user, String teamName, String senderName) {
        create(user, NotifType.TEAM_MESSAGE,
            "💬 " + teamName,
            senderName + " sent a message in your team.",
            "/dashboard");
    }
}