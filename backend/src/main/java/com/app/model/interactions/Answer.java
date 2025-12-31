package com.app.model.interactions;

public class Answer {
    private int id;
    private String text;
    private boolean isCorrect;
    private int questionId;
    private Question question;
    public Answer(int id, String text, boolean isCorrect, int questionId) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
        this.questionId = questionId;
    }

    public Answer(String text, boolean isCorrect, int questionId) {
        this.id = 0; // Default ID, to be set later
        this.text = text;
        this.isCorrect = isCorrect;
        this.questionId = questionId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    public Question getQuestion() {
        return question;
    }
    public void setQuestion(Question question) {
        this.question = question;
    }
}
