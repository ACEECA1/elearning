package com.app.controller.Api.interactions;

import com.app.model.interactions.Notification;
import com.app.service.interactions.NotificationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

@WebServlet("/api/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();
    // Use a specific date format for JSON serialization
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    /**
     * GET /api/notifications
     * Fetches all notifications for the logged-in user.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        // 1. Get User ID from Auth Filter
        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            out.print(gson.toJson(notifications));
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * PUT /api/notifications
     * Marks notifications as read.
     * * Body Options:
     * 1. Mark single as read: { "notificationId": 123 }
     * 2. Mark ALL as read:    { "markAll": true }
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);
            
            if (body.has("markAll") && body.get("markAll").getAsBoolean()) {
                // Case 1: Mark all as read
                notificationService.markAllAsRead(userId);
                responseJson.addProperty("message", "All notifications marked as read");
            } else if (body.has("notificationId")) {
                // Case 2: Mark specific notification as read
                int notificationId = body.get("notificationId").getAsInt();
                notificationService.markAsRead(notificationId, userId);
                responseJson.addProperty("message", "Notification marked as read");
            } else {
                throw new Exception("Missing 'notificationId' or 'markAll' flag in request body");
            }

            responseJson.addProperty("status", "success");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    /**
     * DELETE /api/notifications
     * Deletes a specific notification.
     * Body: { "notificationId": 123 }
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        if (userId == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);
            if (!body.has("notificationId")) {
                throw new Exception("notificationId is required");
            }

            int notificationId = body.get("notificationId").getAsInt();
            notificationService.deleteNotification(notificationId, userId);

            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Notification deleted successfully");

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