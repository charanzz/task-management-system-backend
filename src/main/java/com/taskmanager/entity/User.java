package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Task> tasks;

    // ── Gamification ─────────────────────────────────────
    @Column(name = "focus_score")
    private Integer focusScore = 0;

    @Column(name = "streak")
    private Integer streak = 0;

    // ── Pro / Admin features ──────────────────────────────
    @Column(name = "is_pro")
    private Boolean isPro = false;

    @Column(name = "is_banned")
    private Boolean isBanned = false;

    @Column(name = "pro_expires_at")
    private LocalDateTime proExpiresAt;

    // ── Timestamps ────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (role == null) role = Role.USER;
    }

    // ── Constructors ──────────────────────────────────────
    public User() {}

    // ── Getters & Setters ─────────────────────────────────
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public Integer getFocusScore() { return focusScore != null ? focusScore : 0; }
    public void setFocusScore(Integer focusScore) { this.focusScore = focusScore; }

    public Integer getStreak() { return streak != null ? streak : 0; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public Boolean getIsPro() { return isPro != null ? isPro : false; }
    public void setIsPro(Boolean isPro) { this.isPro = isPro; }

    public Boolean getIsBanned() { return isBanned != null ? isBanned : false; }
    public void setIsBanned(Boolean isBanned) { this.isBanned = isBanned; }

    public LocalDateTime getProExpiresAt() { return proExpiresAt; }
    public void setProExpiresAt(LocalDateTime proExpiresAt) { this.proExpiresAt = proExpiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}