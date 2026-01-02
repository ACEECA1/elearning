package com.app.controller.Test.course;

import com.app.dao.implementation.course.ModuleDAO;
import com.app.model.course.Module;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/module/*")
public class TestModuleDAO extends HttpServlet {
    private ModuleDAO moduleDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        moduleDAO = new ModuleDAO();
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
                Module module = gson.fromJson(jsonInput, Module.class);
                moduleDAO.insert(module);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Module inserted with ID: " + module.getId());
                jsonResponse.add("module", gson.toJsonTree(module));

            } else if ("/update".equals(pathInfo)) {
                Module module = gson.fromJson(jsonInput, Module.class);
                moduleDAO.update(module);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Module " + module.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int moduleId = jobj.get("id").getAsInt();
                
                moduleDAO.delete(moduleId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Module " + moduleId + " deleted.");

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
                String courseId = req.getParameter("courseId");
                if (idStr == null || idStr.isEmpty()) {
                    if (courseId == null || courseId.isEmpty()) {
                    // Find All
                    List<Module> modules = moduleDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("modules", gson.toJsonTree(modules));
                    }
                    else{
                        int cId = Integer.parseInt(courseId);
                        List<Module> modules = moduleDAO.findByCourseId(cId);
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("modules", gson.toJsonTree(modules));
                    }
                } else {
                    // Find One
                    int id = Integer.parseInt(idStr);
                    Module module = moduleDAO.findById(id);
                    
                    if (module != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("module", gson.toJsonTree(module));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Module not found with ID: " + id);
                    }
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