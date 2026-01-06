package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.CommentDAO;
import com.app.model.interactions.Comment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/comment/*")
public class TestCommentDAO extends HttpServlet {

    private CommentDAO commentDAO;
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void init() {
        commentDAO = new CommentDAO();
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
                Comment comment = gson.fromJson(jsonInput, Comment.class);
                commentDAO.insert(comment);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Comment inserted with ID: " + comment.getId());
                jsonResponse.add("comment", gson.toJsonTree(comment));

            } else if ("/update".equals(pathInfo)) {
                Comment comment = gson.fromJson(jsonInput, Comment.class);
                commentDAO.update(comment);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Comment " + comment.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int commentId = jobj.get("id").getAsInt();
                
                commentDAO.delete(commentId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Comment " + commentId + " deleted.");

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
                String forumIdStr = req.getParameter("forumId");

                if (idStr != null) {
                    int commentId = Integer.parseInt(idStr);
                    Comment comment = commentDAO.findById(commentId);
                    if (comment != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("comment", gson.toJsonTree(comment));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Comment not found with ID: " + commentId);
                    }
                } else if (forumIdStr != null) {
                    int forumId = Integer.parseInt(forumIdStr);
                    List<Comment> comments = commentDAO.findByForumId(forumId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("comments", gson.toJsonTree(comments));
                } else {
                    List<Comment> comments = commentDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("comments", gson.toJsonTree(comments));
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