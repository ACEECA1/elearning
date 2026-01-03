package com.app.controller.Test.users;

import com.app.dao.implementation.users.TeacherDAO;
import com.app.model.users.Teacher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/teacher/*")
public class TestTeacherDAO extends HttpServlet {
    private TeacherDAO teacherDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        teacherDAO = new TeacherDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            // 1. Read JSON body from request
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonInput = sb.toString();

            // 2. Logic for /insert
            if ("/insert".equals(pathInfo)) {
                // Parse JSON to Teacher object
                // Note: Ensure Teacher class has a constructor or setters 
                // that match the JSON structure
                Teacher teacher = gson.fromJson(jsonInput, Teacher.class);
                
                teacherDAO.insert(teacher);

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Teacher inserted successfully.");
                jsonResponse.add("teacher", gson.toJsonTree(teacher));

            } else if ("/update".equals(pathInfo)) {
                Teacher teacher = gson.fromJson(jsonInput, Teacher.class);
                teacherDAO.update(teacher);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Teacher ID " + teacher.getId() + " updated.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int id = jobj.get("id").getAsInt();
                teacherDAO.delete(id);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Teacher deleted.");

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Endpoint not found.");
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        try (PrintWriter out = resp.getWriter()) {
            String pathInfo = req.getPathInfo();
            if("/find".equals(pathInfo)){
                // Example: /api/teacher/find?id=1
                String idParam = req.getParameter("id");
                if (idParam != null) {
                    int id = Integer.parseInt(idParam);
                    Teacher teacher = teacherDAO.findById(id);
                    if (teacher != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("teacher", gson.toJsonTree(teacher));
                    } else {
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Teacher not found.");
                    }
                } else {
                    //Find all
                    List<Teacher> teachers = teacherDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("teachers", gson.toJsonTree(teachers));
                }
                out.print(jsonResponse.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unknown endpoint: " + pathInfo);
                out.print(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "GET operation failed: " + e.getMessage());
                out.print(jsonResponse.toString());
            } catch (Exception ignored) {}
        }
    }
}