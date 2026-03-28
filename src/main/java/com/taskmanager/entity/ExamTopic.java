package com.taskmanager.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "exam_topics")
public class ExamTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private ExamPhase phase;

    private String title;
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String resourceUrl;
    private String resourceType;
    private int orderIndex;
    private int xpReward;
    private int estimatedMinutes;
    private boolean hasQuiz;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamQuestion> questions;

    public ExamTopic() {}

    // Getters
    public Long getId() { return id; }
    public ExamPhase getPhase() { return phase; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getResourceUrl() { return resourceUrl; }
    public String getResourceType() { return resourceType; }
    public int getOrderIndex() { return orderIndex; }
    public int getXpReward() { return xpReward; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public boolean isHasQuiz() { return hasQuiz; }
    public List<ExamQuestion> getQuestions() { return questions; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPhase(ExamPhase phase) { this.phase = phase; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setContent(String content) { this.content = content; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public void setHasQuiz(boolean hasQuiz) { this.hasQuiz = hasQuiz; }
    public void setQuestions(List<ExamQuestion> questions) { this.questions = questions; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ExamTopic o = new ExamTopic();
        public Builder phase(ExamPhase v)              { o.phase = v; return this; }
        public Builder title(String v)                 { o.title = v; return this; }
        public Builder description(String v)           { o.description = v; return this; }
        public Builder content(String v)               { o.content = v; return this; }
        public Builder resourceUrl(String v)           { o.resourceUrl = v; return this; }
        public Builder resourceType(String v)          { o.resourceType = v; return this; }
        public Builder orderIndex(int v)               { o.orderIndex = v; return this; }
        public Builder xpReward(int v)                 { o.xpReward = v; return this; }
        public Builder estimatedMinutes(int v)         { o.estimatedMinutes = v; return this; }
        public Builder hasQuiz(boolean v)              { o.hasQuiz = v; return this; }
        public ExamTopic build()                       { return o; }
    }
}