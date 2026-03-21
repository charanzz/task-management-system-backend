package com.taskmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "path_questions")
public class PathQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_task_id")
    private PathTask pathTask;

    private Integer questionNumber; // position within task

    @Column(columnDefinition = "TEXT")
    private String questionText;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctOption; // "A", "B", "C", "D"

    @Column(columnDefinition = "TEXT")
    private String explanation; // why this is correct

    private String source;      // "TNPSC 2019 Paper"
    private Integer yearAsked;  // 2019
    private String difficulty;  // EASY, MEDIUM, HARD

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PathTask getPathTask() { return pathTask; }
    public void setPathTask(PathTask pathTask) { this.pathTask = pathTask; }
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getYearAsked() { return yearAsked; }
    public void setYearAsked(Integer yearAsked) { this.yearAsked = yearAsked; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}