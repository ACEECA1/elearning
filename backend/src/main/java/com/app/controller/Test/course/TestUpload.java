package com.app.controller.Test.course;

import com.google.gson.JsonObject;
import com.app.util.FileUtil;
import java.io.*;
import java.nio.file.Paths;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import com.app.dao.implementation.course.MaterialDAO;
import com.app.model.course.Material;

@WebServlet("/uploads/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 50 * 1024 * 1024 // 50 MB
)
public class TestUpload extends HttpServlet {
    private MaterialDAO materialDAO = new MaterialDAO();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        System.out.println("Requested file: " + pathInfo);
        if ("/ping".equals(pathInfo)) {
            resp.setContentType("application/json");
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"success\", \"message\":\"Reachable\"}");
            } catch (IOException e) { System.out.println("Error writing response: " + e.getMessage()); }
            return;
        }

        String fileName = (pathInfo != null && pathInfo.length() > 1) ? pathInfo.substring(1) : null;
        if (fileName != null) {
            File file = new File(FileUtil.getUploadPath(), fileName);

            if (file.exists() && file.isFile()) {
                String contentType = getServletContext().getMimeType(file.getName());
                resp.setContentType(contentType != null ? contentType : "application/octet-stream");
                resp.setContentLength((int) file.length());

                try (FileInputStream in = new FileInputStream(file);
                    OutputStream out = resp.getOutputStream()) {
                    //Send file content to browser
                    in.transferTo(out); 
                } catch (IOException e) {
                    System.out.println("Error streaming file: " + e.getMessage());
                }
            } else {
                try { resp.sendError(HttpServletResponse.SC_NOT_FOUND); } catch (IOException ignored) {}
            }
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        
        try (PrintWriter out = resp.getWriter()) {
            FileUtil.prepareDirectories();

            Part filePart = req.getPart("file"); 
            int materialId = Integer.parseInt(req.getParameter("materialId"));
            System.out.println("Uploading file for material ID: " + materialId);
            if (filePart != null && filePart.getSize() > 0 && materialId > 0) {
                String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                //.mp4, .pdf, .jpg, etc.
                String fileExtension = ""; 
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFileName.substring(dotIndex).toLowerCase();
                }
                String subFolder = "";
                System.out.println(fileExtension);
                if(".mp4".equalsIgnoreCase(fileExtension)) {
                    subFolder = "videos";
                } else if(".pdf".equalsIgnoreCase(fileExtension)) {
                    subFolder = "materials";
                } else {
                    subFolder = "thumbnails";
                }
                String fileName = originalFileName.substring(0, dotIndex);
                String uniqueFileName = fileName + "_" + System.currentTimeMillis() + fileExtension;
                File uploadFile = new File(FileUtil.getPath(subFolder), uniqueFileName);
                filePart.write(uploadFile.getAbsolutePath());
                Material material = materialDAO.findById(materialId);
                if(material != null) {
                    material.setPath(subFolder + "/" + uniqueFileName);
                    materialDAO.update(material);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Material not found with ID: " + materialId);
                    out.print(jsonResponse.toString());
                    return;
                }
                System.out.println("File uploaded to: " + uploadFile.getPath());
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "File uploaded successfully");
                jsonResponse.addProperty("fileName", uniqueFileName); 
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "No file part found in request");
            }
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Upload Error: " + e.getMessage());
        }
    }
}
