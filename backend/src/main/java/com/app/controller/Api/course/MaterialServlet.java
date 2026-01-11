package com.app.controller.Api.course;

import com.app.model.course.Chapter;
import com.app.model.course.Material;
import com.app.model.course.Module;
import com.app.service.course.ChapterService;
import com.app.service.course.CourseService;
import com.app.service.course.MaterialService;
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
    POST : Create Material
        (Format: application/json)
        Body: { "chapterId": 1, "title": "Lecture Slides", "path": "uploads/materials/slides.pdf", "type": "PDF" }

    DELETE : Delete Material
        (Format: application/json)
        URL: /api/material
        Body: { "materialId": ... } or { "id": ... }

    PUT : Update Material
        (Format: application/json)
        URL: /api/material
        Body: { "materialId": ..., "title": "Updated Title", ... }

    GET : Get Material Details
        (Format: application/json)
        URL: /api/material?materialId=...
        Response: { ...materialDetails }

    GET : List All Materials in a Chapter
        (Format: application/json)
        URL: /api/material?chapterId=...
        Response: [ { ...material1 }, { ...material2 }, ... ]
    
    Student:
    GET : List All Materials in a Chapter
        (Format: application/json)
        URL: /api/material?chapterId=...
        Response: [ { ...material1 }, { ...material2 }, ... ]
*/
@WebServlet("/api/material")
public class MaterialServlet extends HttpServlet {

    private final MaterialService materialService = new MaterialService();
    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    // List materials or get material details
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String chapterIdParam = req.getParameter("chapterId");
        String materialIdParam = req.getParameter("materialId");
        
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
                        out.print("{\"error\": \"You must be enrolled in the course to view its materials.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view materials of this course.\"}");
                        return;
                    }
                }

                List<Material> materials = materialService.getMaterialsByChapter(chapterId);
                out.print(gson.toJson(materials));

            } 
            else if (materialIdParam != null) {
                int materialId = Integer.parseInt(materialIdParam);
                
                Material material = materialService.getMaterialById(materialId);
                
                Chapter chapter = chapterService.getChapterById(material.getChapterId());
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view this material.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view this material.\"}");
                        return;
                    }
                }

                out.print(gson.toJson(material));

            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing chapterId or materialId parameter\"}");
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
            out.print("{\"error\": \"Only teachers can modify materials.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Material material = gson.fromJson(body, Material.class);
                materialService.createMaterial(material, userId);
                responseJson.addProperty("message", "Material created successfully");
            } else if ("UPDATE".equals(action)) {
                Material material = gson.fromJson(body, Material.class);
                
                if (!body.has("materialId") && !body.has("id")) {
                    throw new Exception("materialId is required");
                }
                int id = body.has("materialId") ? body.get("materialId").getAsInt() : body.get("id").getAsInt();
                material.setId(id);
                
                materialService.updateMaterial(material, userId);
                responseJson.addProperty("message", "Material updated successfully");
            } else if ("DELETE".equals(action)) {
                if (!body.has("materialId") && !body.has("id")) {
                    throw new Exception("materialId is required");
                }
                int materialId = body.has("materialId") ? body.get("materialId").getAsInt() : body.get("id").getAsInt();
                
                materialService.deleteMaterial(materialId, userId);
                responseJson.addProperty("message", "Material deleted successfully");
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