package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotifType {
        DUE_SOON,       // task due in <24h
        OVERDUE,        // task past due date
        TASK_DONE,      // you completed a task
        BADGE_EARNED,   // new badge unlocked
        TEAM_INVITE,    // invited to a team
        TEAM_MESSAGE,   // new message in your team
        STREAK_AT_RISK, // haven't completed anything today
        LEVEL_UP        // levelled up
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotifType type;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String link;          // optional deep-link e.g. /dashboard
    private boolean read = false;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"tasks","badges","notifications"})
    private User user;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters & setters
    public Long getId() { return id; }
    public NotifType getType() { return type; }
    public void setType(NotifType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}