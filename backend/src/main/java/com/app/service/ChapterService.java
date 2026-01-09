package com.app.service;

import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class ChapterService {

    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Chapter> getChaptersByModule(int moduleId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return chapterDAO.findByModuleId(conn, moduleId);
        }
    }

    public Chapter getChapterById(int chapterId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Chapter chapter = chapterDAO.findById(conn, chapterId);
            if (chapter == null) throw new Exception("Chapter not found");
            return chapter;
        }
    }

    public void createChapter(Chapter chapter, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            verifyModuleOwnership(conn, chapter.getModuleId(), teacherId);
            
            chapterDAO.insert(conn, chapter);
        }
    }

    public void updateChapter(Chapter chapterUpdates, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Chapter existing = chapterDAO.findById(conn, chapterUpdates.getId());
            if (existing == null) throw new Exception("Chapter not found");

            verifyModuleOwnership(conn, existing.getModuleId(), teacherId);

            existing.setTitle(chapterUpdates.getTitle());
            existing.setContent(chapterUpdates.getContent());
            existing.setOrderIndex(chapterUpdates.getOrderIndex());

            chapterDAO.update(conn, existing);
        }
    }

    public void deleteChapter(int chapterId, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Chapter existing = chapterDAO.findById(conn, chapterId);
            if (existing == null) throw new Exception("Chapter not found");

            verifyModuleOwnership(conn, existing.getModuleId(), teacherId);

            chapterDAO.delete(conn, chapterId);
        }
    }

    private void verifyModuleOwnership(Connection conn, int moduleId, int teacherId) throws Exception {
        Module module = moduleDAO.findById(conn, moduleId);
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this chapter belongs to.");
        }
    }
}