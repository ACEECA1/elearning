package com.app.model.course;

public class Material {
    private int id;
    private String title;
    private String path;
    private String type;
    private int chapterId;
    private Chapter chapter;

    public Material(int id,String title, String path, String type, int chapterId) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.type = type;
        this.chapterId = chapterId;
    }

    public Material(String title, String path, String type, int chapterId) {
        this.id = 0; 
        this.title = title;
        this.path = path;
        this.type = type;
        this.chapterId = chapterId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
