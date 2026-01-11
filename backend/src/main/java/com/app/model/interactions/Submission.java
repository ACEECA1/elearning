package com.app.model.interactions;

import java.sql.Timestamp;

public class Submission {
    private int studentId;
    private int quizId;
    private String submissionPath; // Nullable
    private Double grade;          // Nullable (Use Double object, not double primitive, to allow null)
    private String feedback;       // Nullable
    private Timestamp submissionDate;

    public Submission() {}

    public Submission(int studentId, int quizId, String submissionPath, Double grade) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.submissionPath = submissionPath;
        this.grade = grade;
    }

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getSubmissionPath() { return submissionPath; }
    public void setSubmissionPath(String submissionPath) { this.submissionPath = submissionPath; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Timestamp getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Timestamp submissionDate) { this.submissionDate = submissionDate; }
}