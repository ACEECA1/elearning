package com.app.model.interactions;

import java.util.Date;

import com.app.model.course.Chapter;

public class Quiz {
    private int id;
    private int chapterId;
    private String title;
    private String description;
    private Date availableFrom;
    private Date availableTo;
    private Chapter chapter;

    public Quiz(int id, int chapterId, String title, String description, Date availableFrom, Date availableTo) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.description = description;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public Quiz(int chapterId, String title, String description, Date availableFrom, Date availableTo) {
        this.id = 0; 
        this.chapterId = chapterId;
        this.title = title;
        this.description = description;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
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
    public Date getAvailableFrom() {
        return availableFrom;
    }
    public void setAvailableFrom(Date availableFrom) {
        this.availableFrom = availableFrom;
    }
    public Date getAvailableTo() {
        return availableTo;
    }
    public void setAvailableTo(Date availableTo) {
        this.availableTo = availableTo;
    }
}
