package com.taskmanager.dto;

import java.util.List;

public class QuizSubmitRequest {
    private List<AnswerDTO> answers;

    public QuizSubmitRequest() {}

    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }

    public static class AnswerDTO {
        private Long questionId;
        private String selectedOption;

        public AnswerDTO() {}

        public Long getQuestionId() { return questionId; }
        public String getSelectedOption() { return selectedOption; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    }
}