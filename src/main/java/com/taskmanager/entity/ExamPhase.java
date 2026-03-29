package com.taskmanager.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "exam_phases")
public class ExamPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_path_id")
    private ExamPath examPath;

    private String title;
    private String description;
    private String icon;
    private int orderIndex;
    private int totalTopics;

    // ── CHANGED: EAGER so topics load with phases ─────────────────────────
    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<ExamTopic> topics;

    public ExamPhase() {}

    // Getters
    public Long getId() { return id; }
    public ExamPath getExamPath() { return examPath; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getOrderIndex() { return orderIndex; }
    public int getTotalTopics() { return totalTopics; }
    public List<ExamTopic> getTopics() { return topics; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setExamPath(ExamPath examPath) { this.examPath = examPath; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }
    public void setTopics(List<ExamTopic> topics) { this.topics = topics; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ExamPhase o = new ExamPhase();
        public Builder examPath(ExamPath v)    { o.examPath = v; return this; }
        public Builder title(String v)         { o.title = v; return this; }
        public Builder description(String v)   { o.description = v; return this; }
        public Builder icon(String v)          { o.icon = v; return this; }
        public Builder orderIndex(int v)       { o.orderIndex = v; return this; }
        public Builder totalTopics(int v)      { o.totalTopics = v; return this; }
        public ExamPhase build()               { return o; }
    }
}