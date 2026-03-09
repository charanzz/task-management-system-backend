package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "habits")
public class Habit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String emoji;   // e.g. 💧 🏃 📖
    private String color;   // hex
    private String frequency; // DAILY | WEEKLY

    @Column(name = "target_days") // e.g. 7 = every day, 5 = weekdays
    private Integer targetDays = 7;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "archived")
    private Boolean archived = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("habits")
    private User user;

    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("habit")
    private List<HabitLog> logs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (archived == null) archived = false;
        if (currentStreak == null) currentStreak = 0;
        if (longestStreak == null) longestStreak = 0;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String e) { this.emoji = e; }
    public String getColor() { return color; }
    public void setColor(String c) { this.color = c; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { this.frequency = f; }
    public Integer getTargetDays() { return targetDays; }
    public void setTargetDays(Integer t) { this.targetDays = t; }
    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer s) { this.currentStreak = s; }
    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer s) { this.longestStreak = s; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean a) { this.archived = a; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public List<HabitLog> getLogs() { return logs; }
    public void setLogs(List<HabitLog> l) { this.logs = l; }
}