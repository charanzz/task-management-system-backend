package com.taskmanager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_path_enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_path_id"}))
public class UserPathEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_path_id")
    private ExamPath examPath;

    private LocalDateTime enrolledAt;
    private LocalDateTime lastActivityAt;
    private int completedTopics;
    private int totalXpEarned;
    private double overallAccuracy;
    private int currentStreak;
    private boolean completed;

    public UserPathEnrollment() {}

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public ExamPath getExamPath() { return examPath; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public int getCompletedTopics() { return completedTopics; }
    public int getTotalXpEarned() { return totalXpEarned; }
    public double getOverallAccuracy() { return overallAccuracy; }
    public int getCurrentStreak() { return currentStreak; }
    public boolean isCompleted() { return completed; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setExamPath(ExamPath examPath) { this.examPath = examPath; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }
    public void setTotalXpEarned(int totalXpEarned) { this.totalXpEarned = totalXpEarned; }
    public void setOverallAccuracy(double overallAccuracy) { this.overallAccuracy = overallAccuracy; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}