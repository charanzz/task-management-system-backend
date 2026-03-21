package com.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_task_responses")
public class UserTaskResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_task_id")
    private PathTask pathTask;

    // Result
    private Integer scorePercent;       // 0-100
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer timeTakenMinutes;
    private Boolean needsRevision;      // true if score < 60%
    private Boolean completed;          // task marked complete
    private Integer xpEarned;

    // Per question responses stored as JSON string
    // Format: {"1":"A","2":"C","3":"B"} — questionNumber:selectedOption
    @Column(columnDefinition = "TEXT")
    private String questionResponses;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (completed == null) completed = false;
        if (needsRevision == null) needsRevision = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public PathTask getPathTask() { return pathTask; }
    public void setPathTask(PathTask pathTask) { this.pathTask = pathTask; }
    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
    public void setTimeTakenMinutes(Integer timeTakenMinutes) { this.timeTakenMinutes = timeTakenMinutes; }
    public Boolean getNeedsRevision() { return needsRevision; }
    public void setNeedsRevision(Boolean needsRevision) { this.needsRevision = needsRevision; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public Integer getXpEarned() { return xpEarned; }
    public void setXpEarned(Integer xpEarned) { this.xpEarned = xpEarned; }
    public String getQuestionResponses() { return questionResponses; }
    public void setQuestionResponses(String questionResponses) { this.questionResponses = questionResponses; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}