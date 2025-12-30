package com.app.model.course;

public class Module {
    private int id;
    private int courseId;
    private String title;
    private String description;
    private Course course;
    public Module(int id, int courseId, String title, String description) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
    }
    public Module(int courseId, String title, String description) {
        this.id = 0; 
        this.courseId = courseId;
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
    public int getCourseId() {
        return courseId;
    }
    public void setCourseId(int courseId) {
        this.courseId = courseId;
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
    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
}
