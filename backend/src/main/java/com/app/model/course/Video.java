package com.app.model.course;

public class Video extends Material {
    private int duration; // duration in seconds

    public Video(int id, String path, int chapterId, int duration) {
        super(id, path, "VIDEO", chapterId);
        this.duration = duration;
    }

    public Video(String path, int chapterId, int duration) {
        super(path, "VIDEO", chapterId);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
}
