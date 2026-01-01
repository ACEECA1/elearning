package com.app.controller.Test.course;

import com.app.dao.implementation.course.CourseDAO;
import com.app.model.course.Course;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/course/*")
public class TestCourseDAO extends HttpServlet {
    private CourseDAO courseDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        courseDAO = new CourseDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo(); // This will be /insert, /update, or /delete

        try (PrintWriter out = resp.getWriter()) {
            // 1. Read JSON body from the request
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonInput = sb.toString();

            // 2. Route logic based on path
            if ("/insert".equals(pathInfo)) {
                Course course = gson.fromJson(jsonInput, Course.class);
                courseDAO.insert(course);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Course inserted with ID: " + course.getId());
                // Return the full object so the frontend gets the new ID
                jsonResponse.add("course", gson.toJsonTree(course));

            } else if ("/update".equals(pathInfo)) {
                Course course = gson.fromJson(jsonInput, Course.class);
                courseDAO.update(course);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Course " + course.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                // Expecting JSON: {"id": 5}
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int courseId = jobj.get("id").getAsInt();
                
                courseDAO.delete(courseId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Course " + courseId + " deleted.");

            }
            else {
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
            // Check if the path starts with /find
            if (pathInfo != null && pathInfo.startsWith("/find")) {
                
                String idStr = req.getParameter("id");

                if (idStr == null || idStr.isEmpty()) {
                    // CASE: /api/course/find (Find All)
                    var courses = courseDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("courses", gson.toJsonTree(courses));
                } else {
                    // CASE: /api/course/find?id=5 (Find One)
                    int id = Integer.parseInt(idStr);
                    Course course = courseDAO.findById(id);
                    
                    if (course != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("course", gson.toJsonTree(course));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Course not found with ID: " + id);
                    }
                }
                out.println(jsonResponse.toString());
            } else {
                // If the path isn't /find, return 404
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Endpoint not found");
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // Add error handling logic here
        }
    }
}