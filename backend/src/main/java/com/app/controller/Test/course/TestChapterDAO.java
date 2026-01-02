package com.app.controller.Test.course;

import com.app.dao.implementation.course.ChapterDAO;
import com.app.model.course.Chapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/chapter/*")
public class TestChapterDAO extends HttpServlet {
    private ChapterDAO chapterDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        chapterDAO = new ChapterDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            // 1. Read JSON body
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonInput = sb.toString();

            // 2. Routing Logic
            if ("/insert".equals(pathInfo)) {
                Chapter chapter = gson.fromJson(jsonInput, Chapter.class);
                chapterDAO.insert(chapter);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Chapter inserted with ID: " + chapter.getId());
                jsonResponse.add("chapter", gson.toJsonTree(chapter));

            } else if ("/update".equals(pathInfo)) {
                Chapter chapter = gson.fromJson(jsonInput, Chapter.class);
                chapterDAO.update(chapter);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Chapter " + chapter.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int chapterId = jobj.get("id").getAsInt();
                
                chapterDAO.delete(chapterId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Chapter " + chapterId + " deleted.");

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
                String moduleIdStr = req.getParameter("moduleId");
                String courseIdStr = req.getParameter("courseId");
                if (idStr != null) {
                    int chapterId = Integer.parseInt(idStr);
                    Chapter chapter = chapterDAO.findById(chapterId);
                    if (chapter != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("chapter", gson.toJsonTree(chapter));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Chapter not found with ID: " + chapterId);
                    }
                } else if (moduleIdStr != null) {
                    int moduleId = Integer.parseInt(moduleIdStr);
                    List<Chapter> chapters = chapterDAO.findByModuleId(moduleId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("chapters", gson.toJsonTree(chapters));
                } else if (courseIdStr != null) {
                    int courseId = Integer.parseInt(courseIdStr);
                    List<Chapter> chapters = chapterDAO.findByCourseId(courseId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("chapters", gson.toJsonTree(chapters));
                } else {
                    List<Chapter> chapters = chapterDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("chapters", gson.toJsonTree(chapters));
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