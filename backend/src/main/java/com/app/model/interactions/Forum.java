package com.app.model.interactions;

import com.app.model.course.Chapter;

public class Forum {
    private int id;
    private int chapterId;
    private String title;
    private String description;
    private Chapter chapter;

    public Forum(int id, int chapterId, String title, String description) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.description = description;
    }

    public Forum(int chapterId, String title, String description) {
        this.id = 0; 
        this.chapterId = chapterId;
        this.title = title;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}
