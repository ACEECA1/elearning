package com.app.controller.Api.interactions;

import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.interactions.Forum;
import com.app.service.course.ChapterService;
import com.app.service.course.CourseService;
import com.app.service.course.ModuleService;
import com.app.service.interactions.ForumService;
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
    POST : Create Forum
        (Format: application/json)
        Body: { "chapterId": 1, "title": "Chapter Discussion" }

    DELETE : Delete Forum
        (Format: application/json)
        URL: /api/forum
        Body: { "forumId": ... } or { "id": ... }

    PUT : Update Forum
        (Format: application/json)
        URL: /api/forum
        Body: { "forumId": ..., "title": "Updated Title" }

    GET : Get Forum Details
        (Format: application/json)
        URL: /api/forum?forumId=...
        Response: { ...forumDetails }

    GET : List All Forums in a Chapter
        (Format: application/json)
        URL: /api/forum?chapterId=...
        Response: [ { ...forum1 }, { ...forum2 }, ... ]
    
    Student:
    GET : List All Forums in a Chapter
        (Format: application/json)
        URL: /api/forum?chapterId=...
        Response: [ { ...forum1 }, { ...forum2 }, ... ]
*/
@WebServlet("/api/forum")
public class ForumServlet extends HttpServlet {

    private final ForumService forumService = new ForumService();
    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String chapterIdParam = req.getParameter("chapterId");
        String forumIdParam = req.getParameter("forumId");
        
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            out.println("{\"error\": \"You have to be logged in\"}");
            return;
        }
        int userId = userIdObj;

        try {
            if (chapterIdParam != null) {
                int chapterId = Integer.parseInt(chapterIdParam);
                
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its forums.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view forums of this course.\"}");
                        return;
                    }
                }

                List<Forum> forums = forumService.getForumsByChapter(chapterId);
                out.print(gson.toJson(forums));

            } 
            else if (forumIdParam != null) {
                int forumId = Integer.parseInt(forumIdParam);
                
                Forum forum = forumService.getForumById(forumId);
                
                Chapter chapter = chapterService.getChapterById(forum.getChapterId());
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view this forum.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view this forum.\"}");
                        return;
                    }
                }

                out.print(gson.toJson(forum));

            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing chapterId or forumId parameter\"}");
            }

        } catch (Exception e) {
            resp.setStatus(400);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "CREATE");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "UPDATE");
    }

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
            out.print("{\"error\": \"Only teachers or admins can modify forums.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Forum forum = gson.fromJson(body, Forum.class);
                forumService.createForum(forum, userId);
                responseJson.addProperty("message", "Forum created successfully");

            } else if ("UPDATE".equals(action)) {
                Forum forum = gson.fromJson(body, Forum.class);
                
                if (!body.has("forumId") && !body.has("id")) {
                    throw new Exception("forumId is required");
                }
                int id = body.has("forumId") ? body.get("forumId").getAsInt() : body.get("id").getAsInt();
                forum.setId(id);
                
                forumService.updateForum(forum, userId);
                responseJson.addProperty("message", "Forum updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("forumId") && !body.has("id")) {
                    throw new Exception("forumId is required");
                }
                int forumId = body.has("forumId") ? body.get("forumId").getAsInt() : body.get("id").getAsInt();
                
                forumService.deleteForum(forumId, userId);
                responseJson.addProperty("message", "Forum deleted successfully");
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