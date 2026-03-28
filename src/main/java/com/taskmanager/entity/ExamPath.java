package com.taskmanager.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "exam_paths")
public class ExamPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(mappedBy = "examPath", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<ExamPhase> phases;

    public ExamPath() {}

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
    public List<ExamPhase> getPhases() { return phases; }

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
    public void setPhases(List<ExamPhase> phases) { this.phases = phases; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ExamPath o = new ExamPath();
        public Builder title(String v)           { o.title = v; return this; }
        public Builder slug(String v)            { o.slug = v; return this; }
        public Builder category(String v)        { o.category = v; return this; }
        public Builder description(String v)     { o.description = v; return this; }
        public Builder icon(String v)            { o.icon = v; return this; }
        public Builder audience(String v)        { o.audience = v; return this; }
        public Builder language(String v)        { o.language = v; return this; }
        public Builder totalWeeks(int v)         { o.totalWeeks = v; return this; }
        public Builder totalTasks(int v)         { o.totalTasks = v; return this; }
        public Builder comingSoon(boolean v)     { o.comingSoon = v; return this; }
        public ExamPath build()                  { return o; }
    }
}