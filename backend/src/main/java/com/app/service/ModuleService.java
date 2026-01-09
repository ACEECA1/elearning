package com.app.service;

import com.app.dao.implementation.course.CourseDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.model.course.Course;
import com.app.model.course.Module;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class ModuleService {

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Module> getModulesByCourse(int courseId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return moduleDAO.findByCourseId(conn, courseId);
        }
    }

    public Module getModuleById(int moduleId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Module module = moduleDAO.findById(conn, moduleId);
            if (module == null) throw new Exception("Module not found");
            return module;
        }
    }

    public void createModule(Module module, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            verifyCourseOwnership(conn, module.getCourseId(), teacherId);
            
            moduleDAO.insert(conn, module);
        }
    }

    public void updateModule(Module moduleUpdates, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Module existing = moduleDAO.findById(conn, moduleUpdates.getId());
            if (existing == null) throw new Exception("Module not found");

            verifyCourseOwnership(conn, existing.getCourseId(), teacherId);

            existing.setTitle(moduleUpdates.getTitle());
            existing.setDescription(moduleUpdates.getDescription());
            existing.setOrderIndex(moduleUpdates.getOrderIndex());

            moduleDAO.update(conn, existing);
        }
    }

    public void deleteModule(int moduleId, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Module existing = moduleDAO.findById(conn, moduleId);
            if (existing == null) throw new Exception("Module not found");

            verifyCourseOwnership(conn, existing.getCourseId(), teacherId);

            moduleDAO.delete(conn, moduleId);
        }
    }

    private void verifyCourseOwnership(Connection conn, int courseId, int teacherId) throws Exception {
        Course course = courseDAO.findById(conn, courseId);
        if (course == null) throw new Exception("Course not found");
        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own this course.");
        }
    }
}