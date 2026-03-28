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
@Table(name = "user_topic_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}))
public class UserTopicProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private ExamTopic topic;

    private boolean completed;
    private LocalDateTime completedAt;
    private boolean quizAttempted;
    private boolean quizSkipped;
    private int quizScore;
    private int quizTotal;
    private double quizAccuracy;
    private LocalDateTime quizAttemptedAt;
    private Long linkedTaskId;

    public UserTopicProgress() {}

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public ExamTopic getTopic() { return topic; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public boolean isQuizAttempted() { return quizAttempted; }
    public boolean isQuizSkipped() { return quizSkipped; }
    public int getQuizScore() { return quizScore; }
    public int getQuizTotal() { return quizTotal; }
    public double getQuizAccuracy() { return quizAccuracy; }
    public LocalDateTime getQuizAttemptedAt() { return quizAttemptedAt; }
    public Long getLinkedTaskId() { return linkedTaskId; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setTopic(ExamTopic topic) { this.topic = topic; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setQuizAttempted(boolean quizAttempted) { this.quizAttempted = quizAttempted; }
    public void setQuizSkipped(boolean quizSkipped) { this.quizSkipped = quizSkipped; }
    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }
    public void setQuizTotal(int quizTotal) { this.quizTotal = quizTotal; }
    public void setQuizAccuracy(double quizAccuracy) { this.quizAccuracy = quizAccuracy; }
    public void setQuizAttemptedAt(LocalDateTime quizAttemptedAt) { this.quizAttemptedAt = quizAttemptedAt; }
    public void setLinkedTaskId(Long linkedTaskId) { this.linkedTaskId = linkedTaskId; }
}