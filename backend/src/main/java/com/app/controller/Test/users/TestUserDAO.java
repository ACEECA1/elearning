package com.app.controller.Test.users;

import com.app.dao.implementation.users.UserDAO;
import com.app.model.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/user/*")
public class TestUserDAO extends HttpServlet {
    private UserDAO userDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            // Use req.getReader() directly with Gson for efficiency
            if ("/insert".equals(pathInfo)) {
                User user = gson.fromJson(req.getReader(), User.class);
                userDAO.insert(user);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "User inserted with ID: " + user.getId());
                jsonResponse.add("user", gson.toJsonTree(user));

            } else if ("/update".equals(pathInfo)) {
                User user = gson.fromJson(req.getReader(), User.class);
                userDAO.update(user);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "User " + user.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                // Expecting JSON: {"id": 1}
                JsonObject jobj = gson.fromJson(req.getReader(), JsonObject.class);
                int userId = jobj.get("id").getAsInt();
                
                userDAO.delete(userId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "User " + userId + " deleted successfully.");

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

                if (idStr != null && !idStr.isEmpty()) {
                    // Find by ID: /api/user/find?id=1
                    int id = Integer.parseInt(idStr);
                    User user = userDAO.findById(id);
                    
                    if (user != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("user", gson.toJsonTree(user));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "User not found with ID: " + id);
                    }
                } else {
                    // Find All: /api/user/find
                    List<User> users = userDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("users", gson.toJsonTree(users));
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Use /api/user/find to fetch data.");
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}