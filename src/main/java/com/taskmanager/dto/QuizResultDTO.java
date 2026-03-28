package com.taskmanager.dto;

import java.util.List;

public class QuizResultDTO {
    private int score;
    private int total;
    private double accuracy;
    private int xpEarned;
    private List<AnswerResultDTO> results;

    public QuizResultDTO() {}

    public int getScore() { return score; }
    public int getTotal() { return total; }
    public double getAccuracy() { return accuracy; }
    public int getXpEarned() { return xpEarned; }
    public List<AnswerResultDTO> getResults() { return results; }
    public void setScore(int score) { this.score = score; }
    public void setTotal(int total) { this.total = total; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
    public void setResults(List<AnswerResultDTO> results) { this.results = results; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final QuizResultDTO o = new QuizResultDTO();
        public Builder score(int v) { o.score = v; return this; }
        public Builder total(int v) { o.total = v; return this; }
        public Builder accuracy(double v) { o.accuracy = v; return this; }
        public Builder xpEarned(int v) { o.xpEarned = v; return this; }
        public Builder results(List<AnswerResultDTO> v) { o.results = v; return this; }
        public QuizResultDTO build() { return o; }
    }

    public static class AnswerResultDTO {
        private Long questionId;
        private String question;
        private String selectedOption;
        private String correctOption;
        private String explanation;
        private boolean correct;

        public AnswerResultDTO() {}

        public Long getQuestionId() { return questionId; }
        public String getQuestion() { return question; }
        public String getSelectedOption() { return selectedOption; }
        public String getCorrectOption() { return correctOption; }
        public String getExplanation() { return explanation; }
        public boolean isCorrect() { return correct; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public void setQuestion(String question) { this.question = question; }
        public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public void setCorrect(boolean correct) { this.correct = correct; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AnswerResultDTO o = new AnswerResultDTO();
            public Builder questionId(Long v) { o.questionId = v; return this; }
            public Builder question(String v) { o.question = v; return this; }
            public Builder selectedOption(String v) { o.selectedOption = v; return this; }
            public Builder correctOption(String v) { o.correctOption = v; return this; }
            public Builder explanation(String v) { o.explanation = v; return this; }
            public Builder correct(boolean v) { o.correct = v; return this; }
            public AnswerResultDTO build() { return o; }
        }
    }
}