package com.app.model.course;

public class PDF extends Material {
    private int numberOfPages;
    public PDF(int id, String path, int chapterId , int numberOfPages) {
        super(id, path, "PDF", chapterId);
        this.numberOfPages = numberOfPages;
    }

    public PDF(String path, int chapterId, int numberOfPages) {
        super(path, "PDF", chapterId);
        this.numberOfPages = numberOfPages;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}
