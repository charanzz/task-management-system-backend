package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "habit_logs")
public class HabitLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "habit_id", nullable = false)
    @JsonIgnoreProperties("logs")
    private Habit habit;

    @PrePersist
    protected void onCreate() { completedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate d) { this.logDate = d; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public Habit getHabit() { return habit; }
    public void setHabit(Habit h) { this.habit = h; }
}