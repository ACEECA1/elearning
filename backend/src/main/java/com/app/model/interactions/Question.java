package com.app.model.interactions;

public class Question {
    private int id;
    private int quizId;
    private String text;
    private String materialPath;
    private int score;
    private Quiz quiz;
    public Question(int id, String text, String materialPath, int score, int quizId) {
        this.id = id;
        this.text = text;
        this.materialPath = materialPath;
        this.score = score;
        this.quizId = quizId;
    }

    public Question(String text, String materialPath, int score, int quizId) {
        this.id = 0; // Default ID, to be set later
        this.text = text;
        this.materialPath = materialPath;
        this.score = score;
        this.quizId = quizId;
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

    public String getMaterialPath() {
        return materialPath;
    }

    public void setMaterialPath(String materialPath) {
        this.materialPath = materialPath;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }
    public Quiz getQuiz() {
        return quiz;
    }
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
