package com.app.controller.Test.users;

import com.app.dao.implementation.users.StudentDAO;
import com.app.model.users.Student;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/test/api/student/*")
public class TestStudentDAO extends HttpServlet {
    private StudentDAO studentDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            // 1. Process Write Operations
            if ("/insert".equals(pathInfo)) {
                Student student = gson.fromJson(req.getReader(), Student.class);
                studentDAO.insert(student);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student inserted with ID: " + student.getId());
                jsonResponse.add("student", gson.toJsonTree(student));

            } else if ("/update".equals(pathInfo)) {
                Student student = gson.fromJson(req.getReader(), Student.class);
                studentDAO.update(student);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student " + student.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                // Expecting JSON: {"id": 10}
                JsonObject jobj = gson.fromJson(req.getReader(), JsonObject.class);
                int studentId = jobj.get("id").getAsInt();
                
                studentDAO.delete(studentId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student " + studentId + " deleted.");

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unknown endpoint: " + pathInfo);
            }

            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Operation failed: " + e.getMessage());
                out.print(jsonResponse.toString());
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo != null && pathInfo.startsWith("/find")) {
                String idStr = req.getParameter("id");

                if (idStr != null && !idStr.isEmpty()) {
                    // Find Single Student: /api/student/find?id=5
                    int id = Integer.parseInt(idStr);
                    Student student = studentDAO.findById(id);
                    
                    if (student != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("student", gson.toJsonTree(student));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Student not found with ID: " + id);
                    }
                } else {
                    // Find All Students: /api/student/find
                    List<Student> students = studentDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("students", gson.toJsonTree(students));
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Endpoint not found");
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}