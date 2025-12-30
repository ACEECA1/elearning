package com.app.model.course;

public class Chapter {
    private int id;
    private int moduleId;
    private String title;
    private String content;
    private Module module;
    public Chapter(int id, int moduleId, String title, String content) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.content = content;
    }

    public Chapter(int moduleId, String title, String content) {
        this.id = 0;
        this.moduleId = moduleId;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public Module getModule() {
        return module;
    }
    public void setModule(Module module) {
        this.module = module;
    }
}