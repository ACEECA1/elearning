package com.app.model.course;

import com.app.model.users.Teacher;

public class Course {
    private int id;
    private int teacherId;
    private String title;
    private String targetAudience;
    private String description;
    private String enrollmentKey;
    private String thumbnailPath;
    private Teacher teacher;
    public Course(int id, int teacherId, String title, String targetAudience, String description,
                  String enrollmentKey, String thumbnailPath) {
        this.id = id;
        this.teacherId = teacherId;
        this.title = title;
        this.targetAudience = targetAudience;
        this.description = description;
        this.enrollmentKey = enrollmentKey;
        this.thumbnailPath = thumbnailPath;
    }
    public Course(int teacherId, String title, String targetAudience, String description,
                  String enrollmentKey, String thumbnailPath) {
        this.id = 0; 
        this.teacherId = teacherId;
        this.title = title;
        this.targetAudience = targetAudience;
        this.description = description;
        this.enrollmentKey = enrollmentKey;
        this.thumbnailPath = thumbnailPath;
    }
    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getTeacherId() {
        return teacherId;
    }
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTargetAudience() {
        return targetAudience;
    }
    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getEnrollmentKey() {
        return enrollmentKey;
    }
    public void setEnrollmentKey(String enrollmentKey) {
        this.enrollmentKey = enrollmentKey;
    }
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    public Teacher getTeacher() {
        return teacher;
    }
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
