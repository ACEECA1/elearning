package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.ForumDAO;
import com.app.model.interactions.Forum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/forum/*")
public class TestForumDAO extends HttpServlet {

    private ForumDAO forumDAO;
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void init() {
        forumDAO = new ForumDAO();
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
                Forum forum = gson.fromJson(jsonInput, Forum.class);
                forumDAO.insert(forum);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Forum inserted with ID: " + forum.getId());
                jsonResponse.add("forum", gson.toJsonTree(forum));

            } else if ("/update".equals(pathInfo)) {
                Forum forum = gson.fromJson(jsonInput, Forum.class);
                forumDAO.update(forum);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Forum " + forum.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int forumId = jobj.get("id").getAsInt();
                
                forumDAO.delete(forumId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Forum " + forumId + " deleted.");

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unknown endpoint: " + pathInfo);
            }

            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Error processing request: " + e.getMessage());
            try (PrintWriter out = resp.getWriter()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Operation failed: " + e.getMessage());
                out.print(jsonResponse.toString());
            } catch (Exception ignored) {
                System.out.println("Error writing error response: " + ignored.getMessage());
            }
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
                String chapterIdStr = req.getParameter("chapterId");

                if (idStr != null) {
                    int forumId = Integer.parseInt(idStr);
                    Forum forum = forumDAO.findById(forumId);
                    if (forum != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("forum", gson.toJsonTree(forum));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Forum not found with ID: " + forumId);
                    }
                } else if (chapterIdStr != null) {
                    int chapterId = Integer.parseInt(chapterIdStr);
                    List<Forum> forums = forumDAO.findByChapterId(chapterId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("forums", gson.toJsonTree(forums));
                } else {
                    List<Forum> forums = forumDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("forums", gson.toJsonTree(forums));
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