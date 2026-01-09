package com.app.model.course;

public class Video extends Material {
    private int duration; // duration in seconds

    public Video(int id,String title, String path, int chapterId, int duration) {
        super(id, title, path, "VIDEO", chapterId);
        this.duration = duration;
    }

    public Video(String title, String path, int chapterId, int duration) {
        super(title, path, "VIDEO", chapterId);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
}
