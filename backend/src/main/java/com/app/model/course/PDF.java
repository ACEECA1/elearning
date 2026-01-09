package com.app.model.course;

public class PDF extends Material {
    private int numberOfPages;
    public PDF(int id, String title, String path, int chapterId , int numberOfPages) {
        super(id, title, path, "PDF", chapterId);
        this.numberOfPages = numberOfPages;
    }

    public PDF(String title , String path, int chapterId, int numberOfPages) {
        super(title, path, "PDF", chapterId);
        this.numberOfPages = numberOfPages;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}
