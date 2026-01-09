package com.app.service;

import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.dao.implementation.course.MaterialDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.model.course.Chapter;
import com.app.model.course.Course;
import com.app.model.course.Material;
import com.app.model.course.Module;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class MaterialService {

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Material> getMaterialsByChapter(int chapterId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return materialDAO.findByChapterId(conn, chapterId);
        }
    }

    public Material getMaterialById(int materialId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Material material = materialDAO.findById(conn, materialId);
            if (material == null) throw new Exception("Material not found");
            return material;
        }
    }

    public void createMaterial(Material material, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            verifyChapterOwnership(conn, material.getChapterId(), teacherId);
            
            materialDAO.insert(conn, material);
        }
    }

    public void updateMaterial(Material materialUpdates, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Material existing = materialDAO.findById(conn, materialUpdates.getId());
            if (existing == null) throw new Exception("Material not found");

            verifyChapterOwnership(conn, existing.getChapterId(), teacherId);

            existing.setTitle(materialUpdates.getTitle());
            existing.setPath(materialUpdates.getPath());
            existing.setType(materialUpdates.getType());
            
            materialDAO.update(conn, existing);
        }
    }

    public void deleteMaterial(int materialId, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Material existing = materialDAO.findById(conn, materialId);
            if (existing == null) throw new Exception("Material not found");

            verifyChapterOwnership(conn, existing.getChapterId(), teacherId);

            materialDAO.delete(conn, materialId);
        }
    }

    private void verifyChapterOwnership(Connection conn, int chapterId, int teacherId) throws Exception {
        Chapter chapter = chapterDAO.findById(conn, chapterId);
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this material belongs to.");
        }
    }
}