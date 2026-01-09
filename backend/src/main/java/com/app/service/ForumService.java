package com.app.service;

import com.app.dao.implementation.interactions.ForumDAO;
import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.interactions.Forum;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class ForumService {

    private final ForumDAO forumDAO = new ForumDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Forum> getForumsByChapter(int chapterId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return forumDAO.findByChapterId(conn, chapterId);
        }
    }

    public Forum getForumById(int forumId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Forum forum = forumDAO.findById(conn, forumId);
            if (forum == null) throw new Exception("Forum not found");
            return forum;
        }
    }

    public void createForum(Forum forum, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            verifyChapterOwnership(conn, forum.getChapterId(), teacherId);
            forumDAO.insert(conn, forum);
        }
    }

    public void updateForum(Forum forumUpdates, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Forum existing = forumDAO.findById(conn, forumUpdates.getId());
            if (existing == null) throw new Exception("Forum not found");

            verifyChapterOwnership(conn, existing.getChapterId(), teacherId);

            existing.setTitle(forumUpdates.getTitle());
            
            forumDAO.update(conn, existing);
        }
    }

    public void deleteForum(int forumId, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Forum existing = forumDAO.findById(conn, forumId);
            if (existing == null) throw new Exception("Forum not found");

            verifyChapterOwnership(conn, existing.getChapterId(), teacherId);

            forumDAO.delete(conn, forumId);
        }
    }

    // Helper: Traces Chapter -> Module -> Course -> Teacher to ensure permission
    private void verifyChapterOwnership(Connection conn, int chapterId, int teacherId) throws Exception {
        Chapter chapter = chapterDAO.findById(conn, chapterId);
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this forum belongs to.");
        }
    }
}