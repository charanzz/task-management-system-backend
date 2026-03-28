package com.taskmanager.dto;

import java.util.List;

public class QuizDTO {
    private Long topicId;
    private String topicTitle;
    private List<QuestionDTO> questions;

    public QuizDTO() {}

    public Long getTopicId() { return topicId; }
    public String getTopicTitle() { return topicTitle; }
    public List<QuestionDTO> getQuestions() { return questions; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final QuizDTO o = new QuizDTO();
        public Builder topicId(Long v) { o.topicId = v; return this; }
        public Builder topicTitle(String v) { o.topicTitle = v; return this; }
        public Builder questions(List<QuestionDTO> v) { o.questions = v; return this; }
        public QuizDTO build() { return o; }
    }

    public static class QuestionDTO {
        private Long id;
        private String question;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;

        public QuestionDTO() {}

        public Long getId() { return id; }
        public String getQuestion() { return question; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public void setId(Long id) { this.id = id; }
        public void setQuestion(String question) { this.question = question; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public void setOptionD(String optionD) { this.optionD = optionD; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final QuestionDTO o = new QuestionDTO();
            public Builder id(Long v) { o.id = v; return this; }
            public Builder question(String v) { o.question = v; return this; }
            public Builder optionA(String v) { o.optionA = v; return this; }
            public Builder optionB(String v) { o.optionB = v; return this; }
            public Builder optionC(String v) { o.optionC = v; return this; }
            public Builder optionD(String v) { o.optionD = v; return this; }
            public QuestionDTO build() { return o; }
        }
    }
}