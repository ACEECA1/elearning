package com.app.controller.api.course;

import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.service.course.ChapterService;
import com.app.service.course.CourseService;
import com.app.service.course.ModuleService;
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
    POST : Create Chapter
        (Format: application/json)
        Body: { "moduleId": 1, "title": "Variables", "content": "...", "orderIndex": 1 }

    DELETE : Delete Chapter
        (Format: application/json)
        URL: /api/chapter
        Body: { "chapterId": ... } or { "id": ... }

    PUT : Update Chapter
        (Format: application/json)
        URL: /api/chapter
        Body: { "chapterId": ..., ...updatedDetails }

    GET : Get Chapter Details
        (Format: application/json)
        URL: /api/chapter?chapterId=...
        Response: { ...chapterDetails }

    GET : List All Chapters in a Module
        (Format: application/json)
        URL: /api/chapter?moduleId=...
        Response: [ { ...chapter1 }, { ...chapter2 }, ... ]
    
    Student:
    GET : List All Chapters in a Module
        (Format: application/json)
        URL: /api/chapter?moduleId=...
        Response: [ { ...chapter1 }, { ...chapter2 }, ... ]
*/
@WebServlet("/api/chapter")
public class ChapterServlet extends HttpServlet {

    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService(); // Needed to find courseId from moduleId
    private final CourseService courseService = new CourseService(); // Needed for permission checks
    private final Gson gson = new Gson();

    // List chapters or get chapter details
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String moduleIdParam = req.getParameter("moduleId");
        String chapterIdParam = req.getParameter("chapterId");
        
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            out.println("{\"error\": \"You have to be logged in\"}");
            return;
        }
        int userId = userIdObj;

        try {
            if (moduleIdParam != null) {
                int moduleId = Integer.parseInt(moduleIdParam);
                
                Module module = moduleService.getModuleById(moduleId);
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its chapters.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must own the course to view its chapters.\"}");
                        return;
                    }
                }

                List<Chapter> chapters = chapterService.getChaptersByModule(moduleId);
                out.print(gson.toJson(chapters));

            } 
            else if (chapterIdParam != null) {
                int chapterId = Integer.parseInt(chapterIdParam);
                
                Chapter chapter = chapterService.getChapterById(chapterId);
                
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its chapters.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view chapters of this course.\"}");
                        return;
                    }
                }

                out.print(gson.toJson(chapter));

            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing moduleId or chapterId parameter\"}");
            }

        } catch (Exception e) {
            resp.setStatus(400);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // POST: Create Chapter (Teacher Only)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "CREATE");
    }

    // PUT: Update Chapter (Teacher Only)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "UPDATE");
    }

    // DELETE: Delete Chapter (Teacher Only)
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
        boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isTeacher && !isAdmin) {
            resp.setStatus(403);
            out.print("{\"error\": \"Only teachers can modify chapters.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Chapter chapter = gson.fromJson(body, Chapter.class);
                // Service handles ownership verification
                
                chapterService.createChapter(chapter, userId);
                responseJson.addProperty("message", "Chapter created successfully");

            } else if ("UPDATE".equals(action)) {
                Chapter chapter = gson.fromJson(body, Chapter.class);
                
                // Normalize ID from 'chapterId' or 'id'
                if (!body.has("chapterId") && !body.has("id")) {
                    throw new Exception("chapterId is required");
                }
                int id = body.has("chapterId") ? body.get("chapterId").getAsInt() : body.get("id").getAsInt();
                chapter.setId(id);
                
                chapterService.updateChapter(chapter, userId);
                responseJson.addProperty("message", "Chapter updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("chapterId") && !body.has("id")) {
                    throw new Exception("chapterId is required");
                }
                int chapterId = body.has("chapterId") ? body.get("chapterId").getAsInt() : body.get("id").getAsInt();
                
                chapterService.deleteChapter(chapterId, userId);
                responseJson.addProperty("message", "Chapter deleted successfully");
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