package com.taskmanager.dto;

public class ExamPathSummaryDTO {
    private Long id;
    private String title;
    private String slug;
    private String category;
    private String description;
    private String icon;
    private String audience;
    private String language;
    private int totalWeeks;
    private int totalTasks;
    private boolean comingSoon;
    private boolean enrolled;
    private int completedTopics;
    private double progressPercent;
    private int totalXpEarned;
    private int currentStreak;

    public ExamPathSummaryDTO() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String getAudience() { return audience; }
    public String getLanguage() { return language; }
    public int getTotalWeeks() { return totalWeeks; }
    public int getTotalTasks() { return totalTasks; }
    public boolean isComingSoon() { return comingSoon; }
    public boolean isEnrolled() { return enrolled; }
    public int getCompletedTopics() { return completedTopics; }
    public double getProgressPercent() { return progressPercent; }
    public int getTotalXpEarned() { return totalXpEarned; }
    public int getCurrentStreak() { return currentStreak; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setAudience(String audience) { this.audience = audience; }
    public void setLanguage(String language) { this.language = language; }
    public void setTotalWeeks(int totalWeeks) { this.totalWeeks = totalWeeks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    public void setComingSoon(boolean comingSoon) { this.comingSoon = comingSoon; }
    public void setEnrolled(boolean enrolled) { this.enrolled = enrolled; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
    public void setTotalXpEarned(int totalXpEarned) { this.totalXpEarned = totalXpEarned; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ExamPathSummaryDTO obj = new ExamPathSummaryDTO();
        public Builder id(Long id) { obj.id = id; return this; }
        public Builder title(String v) { obj.title = v; return this; }
        public Builder slug(String v) { obj.slug = v; return this; }
        public Builder category(String v) { obj.category = v; return this; }
        public Builder description(String v) { obj.description = v; return this; }
        public Builder icon(String v) { obj.icon = v; return this; }
        public Builder audience(String v) { obj.audience = v; return this; }
        public Builder language(String v) { obj.language = v; return this; }
        public Builder totalWeeks(int v) { obj.totalWeeks = v; return this; }
        public Builder totalTasks(int v) { obj.totalTasks = v; return this; }
        public Builder comingSoon(boolean v) { obj.comingSoon = v; return this; }
        public Builder enrolled(boolean v) { obj.enrolled = v; return this; }
        public Builder completedTopics(int v) { obj.completedTopics = v; return this; }
        public Builder progressPercent(double v) { obj.progressPercent = v; return this; }
        public Builder totalXpEarned(int v) { obj.totalXpEarned = v; return this; }
        public Builder currentStreak(int v) { obj.currentStreak = v; return this; }
        public ExamPathSummaryDTO build() { return obj; }
    }
}