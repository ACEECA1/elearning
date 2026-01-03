package com.app.controller.Test.course;

import com.app.dao.implementation.course.MaterialDAO;
import com.app.model.course.Material;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/material/*")
public class TestMaterialDAO extends HttpServlet {
    private MaterialDAO materialDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        materialDAO = new MaterialDAO();
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
                Material material = gson.fromJson(jsonInput, Material.class);
                materialDAO.insert(material);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Material inserted with ID: " + material.getId());
                jsonResponse.add("material", gson.toJsonTree(material));

            } else if ("/update".equals(pathInfo)) {
                Material material = gson.fromJson(jsonInput, Material.class);
                materialDAO.update(material);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Material " + material.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int materialId = jobj.get("id").getAsInt();
                
                materialDAO.delete(materialId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Material " + materialId + " deleted.");

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
                String chapterIdStr = req.getParameter("chapterId");

                if (idStr != null && !idStr.isEmpty()) {
                    // Find One by ID
                    int id = Integer.parseInt(idStr);
                    Material material = materialDAO.findById(id);
                    if (material != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("material", gson.toJsonTree(material));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Material not found with ID: " + id);
                    }
                } else if (chapterIdStr != null && !chapterIdStr.isEmpty()) {
                    // Find All for a specific Chapter
                    int chapterId = Integer.parseInt(chapterIdStr);
                    List<Material> materials = materialDAO.findByChapterId(chapterId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("materials", gson.toJsonTree(materials));
                } else {
                    // Find All in the system
                    List<Material> materials = materialDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("materials", gson.toJsonTree(materials));
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