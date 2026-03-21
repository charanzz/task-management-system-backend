package com.taskmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "path_phases")
public class PathPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_path_id")
    private ExamPath examPath;

    private Integer phaseNumber;    // 1, 2, 3, 4, 5
    private String title;           // "Foundation"
    private String description;     // "Build base from Samacheer books"
    private String goal;            // "Complete all Science topics"
    private Integer startWeek;      // 1
    private Integer endWeek;        // 6
    private String color;           // "#7c3aed"
    private String icon;            // "🏗️"
    private Integer totalTasks;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamPath getExamPath() { return examPath; }
    public void setExamPath(ExamPath examPath) { this.examPath = examPath; }
    public Integer getPhaseNumber() { return phaseNumber; }
    public void setPhaseNumber(Integer phaseNumber) { this.phaseNumber = phaseNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }
    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }
}