package com.app.controller.Api.files;

import com.google.gson.JsonObject;
import com.app.util.FileUtil;
import java.io.*;
import java.nio.file.Paths;
import java.net.URLDecoder; // Added missing import
import java.nio.charset.StandardCharsets; // Added missing import

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

// FIX 1: Map to /api/uploads to match your AuthenticationFilter and Frontend
@WebServlet("/api/uploads/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 50 * 1024 * 1024 // 50 MB
)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        
        // 1. Handle Ping
        if ("/ping".equals(pathInfo)) {
            resp.setContentType("application/json");
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"success\", \"message\":\"Reachable\"}");
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        // 2. Serve Files
        // pathInfo comes in as "/thumbnails/image.jpg"
        String fileName = (pathInfo != null && pathInfo.length() > 1) ? pathInfo.substring(1) : null;
        
        if (fileName != null) {
            try {
                // Decode filename (handles spaces like "Java%20Course.png")
                fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
                
                // FileUtil.getUploadPath() should return "C:/.../elearning/uploads"
                File file = new File(FileUtil.getUploadPath(), fileName);

                if (file.exists() && file.isFile()) {
                    String contentType = getServletContext().getMimeType(file.getName());
                    resp.setContentType(contentType != null ? contentType : "application/octet-stream");
                    resp.setContentLength((int) file.length());

                    try (FileInputStream in = new FileInputStream(file);
                         OutputStream out = resp.getOutputStream()) {
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (IOException e) {
                System.out.println("Error serving file: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        
        try (PrintWriter out = resp.getWriter()) {
            FileUtil.prepareDirectories();
            Part filePart = req.getPart("file");
            
            if (filePart != null && filePart.getSize() > 0) {
                String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                
                String fileExtension = ""; 
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFileName.substring(dotIndex).toLowerCase();
                }
                
                String subFolder;
                if(".mp4".equalsIgnoreCase(fileExtension)) {
                    subFolder = "videos";
                } else if(".pdf".equalsIgnoreCase(fileExtension)) {
                    subFolder = "materials";
                } else {
                    subFolder = "thumbnails";
                }
                
                String fileName = (dotIndex > 0) ? originalFileName.substring(0, dotIndex) : originalFileName;
                String uniqueFileName = fileName + "_" + System.currentTimeMillis() + fileExtension;
                
                File uploadFile = new File(FileUtil.getPath(subFolder), uniqueFileName);
                filePart.write(uploadFile.getAbsolutePath());
                String cleanDbPath ="uploads/" + subFolder + "/" + uniqueFileName;

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "File uploaded successfully");
                jsonResponse.addProperty("fileName", uniqueFileName);
                jsonResponse.addProperty("filePath", cleanDbPath); // Send this clean path to Frontend/DB
                
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "No file part found");
            }
            out.print(jsonResponse.toString());
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Upload Error: " + e.getMessage());
            // It's good practice to return a JSON error so frontend doesn't hang
            try {
                resp.getWriter().write("{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            } catch (Exception ignored) {}
        }
    }
}