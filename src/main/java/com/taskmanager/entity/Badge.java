package com.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String emoji;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime earnedAt;

    public Badge() {}

    public Badge(String name, String emoji, String description, User user) {
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.user = user;
        this.earnedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public String getDescription() { return description; }
    public User getUser() { return user; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
}