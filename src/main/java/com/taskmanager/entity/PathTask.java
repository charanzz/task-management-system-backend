package com.taskmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "path_tasks")
public class PathTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_path_id")
    private ExamPath examPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private PathPhase phase;

    // Position
    private Integer taskNumber;     // 1 to 132 — sequential overall
    private Integer weekNumber;     // 1 to 24
    private Integer dayNumber;      // 1 to 7 — suggested day within week
    private Integer phaseNumber;    // 1 to 5

    // Content
    private String subject;         // "General Science"
    private String topic;           // "Physics"
    private String subtopic;        // "Motion and Force"
    private String taskType;        // READ, PRACTICE, REVISE, MOCK_TEST, CURRENT_AFFAIRS

    @Column(columnDefinition = "TEXT")
    private String instruction;     // Exactly what to do

    private String resource;        // "Samacheer Class 9 Science - Chapter 2, Pages 45-67"
    private String resourceUrl;     // Optional link
    private Integer estimatedMinutes; // 45
    private String difficulty;      // EASY, MEDIUM, HARD
    private Integer weightagePercent; // how much this topic appears in exam (%)
    private Integer totalQuestions; // number of MCQs attached
    private String icon;            // emoji for subject

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamPath getExamPath() { return examPath; }
    public void setExamPath(ExamPath examPath) { this.examPath = examPath; }
    public PathPhase getPhase() { return phase; }
    public void setPhase(PathPhase phase) { this.phase = phase; }
    public Integer getTaskNumber() { return taskNumber; }
    public void setTaskNumber(Integer taskNumber) { this.taskNumber = taskNumber; }
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Integer getPhaseNumber() { return phaseNumber; }
    public void setPhaseNumber(Integer phaseNumber) { this.phaseNumber = phaseNumber; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getSubtopic() { return subtopic; }
    public void setSubtopic(String subtopic) { this.subtopic = subtopic; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getWeightagePercent() { return weightagePercent; }
    public void setWeightagePercent(Integer weightagePercent) { this.weightagePercent = weightagePercent; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}