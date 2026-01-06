package com.app.model.interactions;

import com.app.model.course.Chapter;
import java.sql.Timestamp; // Using Timestamp for DATETIME compatibility

public class Forum {
    private int id;
    private int chapterId;
    private String title;
    private Timestamp createdAt; 
    private Chapter chapter;

    public Forum(int id, int chapterId, String title, Timestamp createdAt) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Forum(int chapterId, String title, Timestamp createdAt) {
        this.id = 0;
        this.chapterId = chapterId;
        this.title = title;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Chapter getChapter() { return chapter; }
    public void setChapter(Chapter chapter) { this.chapter = chapter; }
}