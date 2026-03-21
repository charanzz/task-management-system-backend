package com.taskmanager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_paths")
public class ExamPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String examType; // TNPSC_GROUP4, UPSC, CAT etc

    @Column(nullable = false)
    private String title; // "TNPSC Group 4"

    private String description;
    private String bannerColor; // gradient color
    private String icon; // emoji
    private Integer totalTasks;
    private Integer totalWeeks;
    private Integer totalPhases;
    private Boolean isActive; // is this path available to users
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED
    private String targetAudience; // "Class 12 Pass, Graduates"
    private String examBody; // "TNPSC"
    private String language; // "Tamil & English"

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBannerColor() { return bannerColor; }
    public void setBannerColor(String bannerColor) { this.bannerColor = bannerColor; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }
    public Integer getTotalWeeks() { return totalWeeks; }
    public void setTotalWeeks(Integer totalWeeks) { this.totalWeeks = totalWeeks; }
    public Integer getTotalPhases() { return totalPhases; }
    public void setTotalPhases(Integer totalPhases) { this.totalPhases = totalPhases; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getExamBody() { return examBody; }
    public void setExamBody(String examBody) { this.examBody = examBody; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}