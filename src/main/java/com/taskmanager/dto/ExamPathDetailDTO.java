package com.taskmanager.dto;

import java.util.List;

public class ExamPathDetailDTO {
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
    private boolean enrolled;
    private int completedTopics;
    private double progressPercent;
    private int totalXpEarned;
    private double overallAccuracy;
    private int currentStreak;
    private List<PhaseDTO> phases;

    public ExamPathDetailDTO() {}

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
    public boolean isEnrolled() { return enrolled; }
    public int getCompletedTopics() { return completedTopics; }
    public double getProgressPercent() { return progressPercent; }
    public int getTotalXpEarned() { return totalXpEarned; }
    public double getOverallAccuracy() { return overallAccuracy; }
    public int getCurrentStreak() { return currentStreak; }
    public List<PhaseDTO> getPhases() { return phases; }

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
    public void setEnrolled(boolean enrolled) { this.enrolled = enrolled; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
    public void setTotalXpEarned(int totalXpEarned) { this.totalXpEarned = totalXpEarned; }
    public void setOverallAccuracy(double overallAccuracy) { this.overallAccuracy = overallAccuracy; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setPhases(List<PhaseDTO> phases) { this.phases = phases; }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ExamPathDetailDTO o = new ExamPathDetailDTO();
        public Builder id(Long v) { o.id = v; return this; }
        public Builder title(String v) { o.title = v; return this; }
        public Builder slug(String v) { o.slug = v; return this; }
        public Builder category(String v) { o.category = v; return this; }
        public Builder description(String v) { o.description = v; return this; }
        public Builder icon(String v) { o.icon = v; return this; }
        public Builder audience(String v) { o.audience = v; return this; }
        public Builder language(String v) { o.language = v; return this; }
        public Builder totalWeeks(int v) { o.totalWeeks = v; return this; }
        public Builder totalTasks(int v) { o.totalTasks = v; return this; }
        public Builder enrolled(boolean v) { o.enrolled = v; return this; }
        public Builder completedTopics(int v) { o.completedTopics = v; return this; }
        public Builder progressPercent(double v) { o.progressPercent = v; return this; }
        public Builder totalXpEarned(int v) { o.totalXpEarned = v; return this; }
        public Builder overallAccuracy(double v) { o.overallAccuracy = v; return this; }
        public Builder currentStreak(int v) { o.currentStreak = v; return this; }
        public Builder phases(List<PhaseDTO> v) { o.phases = v; return this; }
        public ExamPathDetailDTO build() { return o; }
    }

    // ── Nested: PhaseDTO ──────────────────────────────────────────────────
    public static class PhaseDTO {
        private Long id;
        private String title;
        private String description;
        private String icon;
        private int orderIndex;
        private int totalTopics;
        private int completedTopics;
        private double progressPercent;
        private List<TopicDTO> topics;

        public PhaseDTO() {}

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getOrderIndex() { return orderIndex; }
        public int getTotalTopics() { return totalTopics; }
        public int getCompletedTopics() { return completedTopics; }
        public double getProgressPercent() { return progressPercent; }
        public List<TopicDTO> getTopics() { return topics; }

        public void setId(Long id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setIcon(String icon) { this.icon = icon; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }
        public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }
        public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
        public void setTopics(List<TopicDTO> topics) { this.topics = topics; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final PhaseDTO o = new PhaseDTO();
            public Builder id(Long v) { o.id = v; return this; }
            public Builder title(String v) { o.title = v; return this; }
            public Builder description(String v) { o.description = v; return this; }
            public Builder icon(String v) { o.icon = v; return this; }
            public Builder orderIndex(int v) { o.orderIndex = v; return this; }
            public Builder totalTopics(int v) { o.totalTopics = v; return this; }
            public Builder completedTopics(int v) { o.completedTopics = v; return this; }
            public Builder progressPercent(double v) { o.progressPercent = v; return this; }
            public Builder topics(List<TopicDTO> v) { o.topics = v; return this; }
            public PhaseDTO build() { return o; }
        }
    }

    // ── Nested: TopicDTO ──────────────────────────────────────────────────
    public static class TopicDTO {
        private Long id;
        private String title;
        private String description;
        private String content;
        private String resourceUrl;
        private String resourceType;
        private int orderIndex;
        private int xpReward;
        private int estimatedMinutes;
        private boolean hasQuiz;
        private boolean completed;
        private boolean quizAttempted;
        private boolean quizSkipped;
        private double quizAccuracy;

        public TopicDTO() {}

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getContent() { return content; }
        public String getResourceUrl() { return resourceUrl; }
        public String getResourceType() { return resourceType; }
        public int getOrderIndex() { return orderIndex; }
        public int getXpReward() { return xpReward; }
        public int getEstimatedMinutes() { return estimatedMinutes; }
        public boolean isHasQuiz() { return hasQuiz; }
        public boolean isCompleted() { return completed; }
        public boolean isQuizAttempted() { return quizAttempted; }
        public boolean isQuizSkipped() { return quizSkipped; }
        public double getQuizAccuracy() { return quizAccuracy; }

        public void setId(Long id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setContent(String content) { this.content = content; }
        public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
        public void setXpReward(int xpReward) { this.xpReward = xpReward; }
        public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
        public void setHasQuiz(boolean hasQuiz) { this.hasQuiz = hasQuiz; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public void setQuizAttempted(boolean quizAttempted) { this.quizAttempted = quizAttempted; }
        public void setQuizSkipped(boolean quizSkipped) { this.quizSkipped = quizSkipped; }
        public void setQuizAccuracy(double quizAccuracy) { this.quizAccuracy = quizAccuracy; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final TopicDTO o = new TopicDTO();
            public Builder id(Long v) { o.id = v; return this; }
            public Builder title(String v) { o.title = v; return this; }
            public Builder description(String v) { o.description = v; return this; }
            public Builder content(String v) { o.content = v; return this; }
            public Builder resourceUrl(String v) { o.resourceUrl = v; return this; }
            public Builder resourceType(String v) { o.resourceType = v; return this; }
            public Builder orderIndex(int v) { o.orderIndex = v; return this; }
            public Builder xpReward(int v) { o.xpReward = v; return this; }
            public Builder estimatedMinutes(int v) { o.estimatedMinutes = v; return this; }
            public Builder hasQuiz(boolean v) { o.hasQuiz = v; return this; }
            public Builder completed(boolean v) { o.completed = v; return this; }
            public Builder quizAttempted(boolean v) { o.quizAttempted = v; return this; }
            public Builder quizSkipped(boolean v) { o.quizSkipped = v; return this; }
            public Builder quizAccuracy(double v) { o.quizAccuracy = v; return this; }
            public TopicDTO build() { return o; }
        }
    }
}