package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.EnrollmentDAO;
import com.app.model.interactions.Enrollment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/enrollment/*")
public class TestEnrollmentDAO extends HttpServlet {

    private EnrollmentDAO enrollmentDAO;
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void init() {
        enrollmentDAO = new EnrollmentDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            String jsonInput = sb.toString();

            if ("/insert".equals(pathInfo)) {
                // Expects: { "studentId": 1, "courseId": 50 }
                Enrollment enrollment = gson.fromJson(jsonInput, Enrollment.class);
                enrollmentDAO.insert(enrollment);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student enrolled successfully.");
                jsonResponse.add("enrollment", gson.toJsonTree(enrollment));

            } else if ("/delete".equals(pathInfo)) {
                // Expects: { "studentId": 1, "courseId": 50 }
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int studentId = jobj.get("studentId").getAsInt();
                int courseId = jobj.get("courseId").getAsInt();
                
                enrollmentDAO.delete(studentId, courseId);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Enrollment removed.");

            } else {
                resp.setStatus(404);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Endpoint not found (Use /insert or /delete)");
            }
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            System.out.println("Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo != null && pathInfo.startsWith("/find")) {
                String studentIdStr = req.getParameter("studentId");
                String courseIdStr = req.getParameter("courseId");

                if (studentIdStr != null && courseIdStr != null) {
                    // Check specific enrollment status
                    boolean isEnrolled = EnrollmentDAO.isEnrolled(Integer.parseInt(studentIdStr), Integer.parseInt(courseIdStr));
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("isEnrolled", isEnrolled);
                    
                } else if (studentIdStr != null) {
                    // Get all courses for a student
                    List<Enrollment> enrollments = enrollmentDAO.findByStudentId(Integer.parseInt(studentIdStr));
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("enrollments", gson.toJsonTree(enrollments));
                    
                } else if (courseIdStr != null) {
                    // Get class list for a course
                    List<Enrollment> enrollments = enrollmentDAO.findByCourseId(Integer.parseInt(courseIdStr));
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("enrollments", gson.toJsonTree(enrollments));
                } else {
                    resp.setStatus(400);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Missing parameters (studentId or courseId)");
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(404);
            }
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }
}