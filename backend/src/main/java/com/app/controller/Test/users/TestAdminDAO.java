package com.app.controller.Test.users;

import com.app.dao.implementation.users.AdminDAO;
import com.app.model.users.Admin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/admin/*")
public class TestAdminDAO extends HttpServlet {
    private AdminDAO adminDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        adminDAO = new AdminDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            // 1. Handle Write Operations based on Path
            if ("/insert".equals(pathInfo)) {
                Admin admin = gson.fromJson(req.getReader(), Admin.class);
                adminDAO.insert(admin);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Admin inserted with ID: " + admin.getId());
                jsonResponse.add("admin", gson.toJsonTree(admin));

            } else if ("/update".equals(pathInfo)) {
                Admin admin = gson.fromJson(req.getReader(), Admin.class);
                adminDAO.update(admin);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Admin " + admin.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                // Expecting JSON: {"id": 5}
                JsonObject jobj = gson.fromJson(req.getReader(), JsonObject.class);
                int adminId = jobj.get("id").getAsInt();
                
                adminDAO.delete(adminId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Admin " + adminId + " deleted.");

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
                    // CASE: /api/admin/find?id=1
                    int id = Integer.parseInt(idStr);
                    Admin admin = adminDAO.findById(id);
                    
                    if (admin != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("admin", gson.toJsonTree(admin));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Admin not found with ID: " + id);
                    }
                } else {
                    // CASE: /api/admin/find (Find All)
                    List<Admin> admins = adminDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("admins", gson.toJsonTree(admins));
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