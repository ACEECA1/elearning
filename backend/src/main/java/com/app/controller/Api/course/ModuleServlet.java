package com.app.controller.Api.course;

import com.app.model.course.Module;
import com.app.service.CourseService;
import com.app.service.ModuleService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
/*
    Teacher:
    POST : Create Module
        (Format: application/json)
        Body: { ...moduleDetails }

    DELETE : Delete Module
        (Format: application/json)
        URL: /api/module
        Body: { "moduleId": ... }
    PUT : Update Module
        (Format: application/json)
        URL: /api/module
        Body: { "moduleId",...updatedModuleDetails }
    GET : Get Module Details
        (Format: application/json)
        URL: /api/module?moduleId=...
        Response: { ...moduleDetails }
    GET : List All Modules in a Course
        (Format: application/json)
        URL: /api/module?courseId=...
        Response: [ { ...moduleDetails1 }, { ...moduleDetails2 }, ... ]
    
    Student:
    GET : List All Modules in a Course
        (Format: application/json)
        URL: /api/module?courseId=...
        Response: [ { ...moduleDetails1 }, { ...moduleDetails2 }, ... ]
*/
@WebServlet("/api/module")
public class ModuleServlet extends HttpServlet {

    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    //list modules or get module details
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String courseIdParam = req.getParameter("courseId");
        String moduleIdParam = req.getParameter("moduleId");
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");
        if(userIdObj == null){
            resp.setStatus(401);
            out.println("You have to be logged in");
            return;
        }
        int userId = userIdObj;
        try {
            if (courseIdParam != null) {
                int courseId = Integer.parseInt(courseIdParam);
                if("STUDENT".equalsIgnoreCase(role)){
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if(!isEnrolled){
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its modules.\"}");
                        return;
                    }
                }
                if("TEACHER".equalsIgnoreCase(role)){
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if(!ownsCourse){
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view modules of this course.\"}");
                        return;
                    }
                }
                List<Module> modules = moduleService.getModulesByCourse(courseId);
                out.print(gson.toJson(modules));

            } else if (moduleIdParam != null) {
                int moduleId = Integer.parseInt(moduleIdParam);
                Module module = moduleService.getModuleById(moduleId);
                int moduleCourseId = module.getCourseId();
                if("STUDENT".equalsIgnoreCase(role)){
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, moduleCourseId);
                    if(!isEnrolled){
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its modules.\"}");
                        return;
                    }
                }
                if("TEACHER".equalsIgnoreCase(role)){
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, moduleCourseId);
                    if(!ownsCourse){
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view modules of this course.\"}");
                        return;
                    }
                }

                out.print(gson.toJson(module));

            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing courseId or moduleId parameter\"}");
            }

        } catch (Exception e) {
            resp.setStatus(400);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // POST: Create Module (Teacher Only)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "CREATE");
    }

    // PUT: Update Module (Teacher Only)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "UPDATE");
    }

    // DELETE: Delete Module (Teacher Only)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "DELETE");
    }

    private void handleWriteRequest(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            return;
        }

        if (!"TEACHER".equalsIgnoreCase(role)) {
            resp.setStatus(403);
            out.print("{\"error\": \"Only teachers can modify modules.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Module module = gson.fromJson(body, Module.class);
                //verified inside service
                moduleService.createModule(module, userId);
                responseJson.addProperty("message", "Module created successfully");

            } else if ("UPDATE".equals(action)) {
                Module module = gson.fromJson(body, Module.class);
                // Ensure ID is present
                if (!body.has("moduleId") && !body.has("id")){
                    throw new Exception("moduleId is required");
                }
                int id = body.has("moduleId") ? body.get("moduleId").getAsInt() : body.get("id").getAsInt();
                module.setId(id);
                
                moduleService.updateModule(module, userId);
                responseJson.addProperty("message", "Module updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("moduleId") && !body.has("id")){
                    throw new Exception("moduleId is required");
                }
                int moduleId = body.has("moduleId") ? body.get("moduleId").getAsInt() : body.get("id").getAsInt();
                
                moduleService.deleteModule(moduleId, userId);
                responseJson.addProperty("message", "Module deleted successfully");
            }
            
            responseJson.addProperty("status", "success");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    private JsonObject parseBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        if (sb.length() == 0) return new JsonObject();
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }
}