package com.app.model.interactions; // or com.app.model.student

import java.util.Date;

public class Note {
    private int studentId;
    private int quizId;
    private double grade;
    private Date dateRecorded;

    public Note(int studentId, int quizId, double grade, Date dateRecorded) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.grade = grade;
        this.dateRecorded = dateRecorded;
    }

    public Note(int studentId, int quizId, double grade) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.grade = grade;
    }


    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public Date getDateRecorded() { return dateRecorded; }
    public void setDateRecorded(Date dateRecorded) { this.dateRecorded = dateRecorded; }
}