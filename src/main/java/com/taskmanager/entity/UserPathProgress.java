package com.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_path_progress")
public class UserPathProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_path_id")
    private ExamPath examPath;

    // Progress tracking
    private Integer currentTaskNumber;      // which task they are on now
    private Integer tasksCompleted;         // total completed
    private Integer currentPhase;           // 1-5
    private Integer currentWeek;            // 1-24
    private String status;                  // ACTIVE, PAUSED, COMPLETED, EXPIRED

    // Stats
    private Integer totalQuestionsAttempted;
    private Integer totalCorrectAnswers;
    private Integer pathStreakDays;         // consecutive days studied
    private Integer totalXpEarned;
    private Integer totalMinutesStudied;

    // Weak subjects stored as comma separated
    private String weakSubjects;            // "Geography,Economy"

    // Dates
    private LocalDateTime startedAt;
    private LocalDate targetExamDate;
    private LocalDateTime lastActivityAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        startedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
        currentTaskNumber = 1;
        currentPhase = 1;
        currentWeek = 1;
        tasksCompleted = 0;
        totalQuestionsAttempted = 0;
        totalCorrectAnswers = 0;
        pathStreakDays = 0;
        totalXpEarned = 0;
        totalMinutesStudied = 0;
        status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ExamPath getExamPath() { return examPath; }
    public void setExamPath(ExamPath examPath) { this.examPath = examPath; }
    public Integer getCurrentTaskNumber() { return currentTaskNumber; }
    public void setCurrentTaskNumber(Integer currentTaskNumber) { this.currentTaskNumber = currentTaskNumber; }
    public Integer getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(Integer tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    public Integer getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Integer currentPhase) { this.currentPhase = currentPhase; }
    public Integer getCurrentWeek() { return currentWeek; }
    public void setCurrentWeek(Integer currentWeek) { this.currentWeek = currentWeek; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalQuestionsAttempted() { return totalQuestionsAttempted; }
    public void setTotalQuestionsAttempted(Integer totalQuestionsAttempted) { this.totalQuestionsAttempted = totalQuestionsAttempted; }
    public Integer getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public void setTotalCorrectAnswers(Integer totalCorrectAnswers) { this.totalCorrectAnswers = totalCorrectAnswers; }
    public Integer getPathStreakDays() { return pathStreakDays; }
    public void setPathStreakDays(Integer pathStreakDays) { this.pathStreakDays = pathStreakDays; }
    public Integer getTotalXpEarned() { return totalXpEarned; }
    public void setTotalXpEarned(Integer totalXpEarned) { this.totalXpEarned = totalXpEarned; }
    public Integer getTotalMinutesStudied() { return totalMinutesStudied; }
    public void setTotalMinutesStudied(Integer totalMinutesStudied) { this.totalMinutesStudied = totalMinutesStudied; }
    public String getWeakSubjects() { return weakSubjects; }
    public void setWeakSubjects(String weakSubjects) { this.weakSubjects = weakSubjects; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDate getTargetExamDate() { return targetExamDate; }
    public void setTargetExamDate(LocalDate targetExamDate) { this.targetExamDate = targetExamDate; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}