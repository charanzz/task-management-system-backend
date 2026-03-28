package com.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exam_questions")
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private ExamTopic topic;

    @Column(columnDefinition = "TEXT")
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;
    private int orderIndex;

    public ExamQuestion() {}

    // Getters
    public Long getId() { return id; }
    public ExamTopic getTopic() { return topic; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public String getExplanation() { return explanation; }
    public int getOrderIndex() { return orderIndex; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTopic(ExamTopic topic) { this.topic = topic; }
    public void setQuestion(String question) { this.question = question; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ExamQuestion o = new ExamQuestion();
        public Builder topic(ExamTopic v)          { o.topic = v; return this; }
        public Builder question(String v)          { o.question = v; return this; }
        public Builder optionA(String v)           { o.optionA = v; return this; }
        public Builder optionB(String v)           { o.optionB = v; return this; }
        public Builder optionC(String v)           { o.optionC = v; return this; }
        public Builder optionD(String v)           { o.optionD = v; return this; }
        public Builder correctOption(String v)     { o.correctOption = v; return this; }
        public Builder explanation(String v)       { o.explanation = v; return this; }
        public Builder orderIndex(int v)           { o.orderIndex = v; return this; }
        public ExamQuestion build()                { return o; }
    }
}