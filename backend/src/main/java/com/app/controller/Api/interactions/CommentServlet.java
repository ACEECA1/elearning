package com.app.controller.Api.interactions;

import com.app.model.course.Chapter;
import com.app.model.course.Course;
import com.app.model.course.Module;
import com.app.model.interactions.Comment;
import com.app.model.interactions.Forum;
import com.app.service.ChapterService;
import com.app.service.CommentService;
import com.app.service.CourseService;
import com.app.service.ForumService;
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
    POST : Add Comment
        Body: { "forumId": 1, "content": "Hello", "isReply": false }
        Body (Reply): { "forumId": 1, "content": "Reply", "isReply": true, "parentCommentId": 5 }

    DELETE : Delete Comment
        Body: { "commentId": ... }
    PUT : Update Comment
        Body: { "commentId": ..., "content": "Updated Content" }
    GET : List Comments for a Forum
        URL: /api/comment?forumId=...
*/
@WebServlet("/api/comment")
public class CommentServlet extends HttpServlet {

    private final CommentService commentService = new CommentService();
    private final ChapterService chapterService = new ChapterService();
    private final ForumService forumService = new ForumService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");
        String forumIdParam = req.getParameter("forumId");
        if(userIdObj == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized: User not logged in.\"}");
            return;
        }
        int userId = userIdObj;
        try {
            if (forumIdParam != null) {
                int forumId = Integer.parseInt(forumIdParam);
                Forum forum = forumService.getForumById(forumId);
                Chapter chapter = chapterService.getChapterById(forum.getChapterId());
                Module module = moduleService.getModuleById(chapter.getModuleId());
                Course course = courseService.getCourseById(module.getCourseId());
                if("STUDENT".equalsIgnoreCase(role)){
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, course.getId());
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"Forbidden: You are not enrolled in this course.\"}");
                        return;
                    }
                }
                if("TEACHER".equalsIgnoreCase(role)){
                    boolean isTeacher = courseService.teacherOwnsCourse(userId, course.getId());
                    if (!isTeacher) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"Forbidden: You are not the teacher of this course.\"}");
                        return;
                    }
                }
                List<Comment> comments = commentService.getCommentsByForum(forumId);
                out.print(gson.toJson(comments));
            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing forumId parameter\"}");
            }
        } catch (Exception e) {
            resp.setStatus(400);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Unauthorized: User not logged in.");
            out.print(responseJson.toString());
            return;
        }

        try {
            JsonObject body = parseBody(req);
            Comment comment = gson.fromJson(body, Comment.class);
            
            commentService.addComment(comment, userId, role);
            
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Comment added successfully");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Unauthorized: User not logged in.");
            out.print(responseJson.toString());
            return;
        }

        try {
            JsonObject body = parseBody(req);
            if (!body.has("commentId") && !body.has("id")) {
                throw new Exception("commentId is required");
            }
            int commentId = body.has("commentId") ? body.get("commentId").getAsInt() : body.get("id").getAsInt();

            commentService.deleteComment(commentId, userId, role);
            
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Comment deleted successfully");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }


    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Unauthorized: User not authenticated");
            out.print(responseJson.toString());
            return;
        }

        try {
            JsonObject body = parseBody(req);
            if (!body.has("commentId") && !body.has("id")) {
                throw new Exception("commentId is required");
            }
            int commentId = body.has("commentId") ? body.get("commentId").getAsInt() : body.get("id").getAsInt();
            String newContent = body.get("content").getAsString();

            commentService.updateComment(commentId, newContent, userId, role);
            
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Comment updated successfully");

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