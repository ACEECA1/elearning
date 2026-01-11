package com.app.controller.Api.files;

import com.google.gson.JsonObject;
import com.app.util.FileUtil;
import java.io.*;
import java.nio.file.Paths;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/uploads/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 10 * 1024 * 1024,  // 10 MB
    maxRequestSize = 50 * 1024 * 1024 // 50 MB
)
public class UploadServlet extends HttpServlet {

    /**
     * SECURE DOWNLOAD (GET)
     * Handles file retrieval with strict access control based on folder types.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // e.g., "/submissions/submission_u5_123.pdf"

        // 1. Basic Validation
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File path required");
            return;
        }

        // 2. Authentication Check (All downloads require login)
        Integer currentUserId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (currentUserId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // Decode filename (handles spaces like "My%20File.pdf")
            String requestPath = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8.toString());
            
            // Prevent Path Traversal (e.g., "../../../etc/passwd")
            if (requestPath.contains("..")) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path sequence");
                return;
            }

            // Remove leading slash for logic comparison (e.g. "submissions/file.pdf")
            String cleanPath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;

            // --- SECURITY LOGIC START ---

            // CASE A: Submissions (Strict Privacy)
            if (cleanPath.startsWith("submissions/")) {
                // TEACHERS/ADMINS: Can view all submissions
                if ("TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                    // Access Granted: Flow continues to file serving
                } 
                // STUDENTS: Can ONLY view their own submissions
                else if ("STUDENT".equalsIgnoreCase(role)) {
                    // We verify ownership by checking the filename structure we enforced in doPost
                    // Pattern: type_u{userId}_timestamp.ext
                    String userTag = "_u" + currentUserId + "_";
                    if (!cleanPath.contains(userTag)) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("Access Denied: You can only access your own submissions.");
                        return;
                    }
                    // Access Granted: Flow continues
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("Access Denied");
                    return;
                }
            }
            
            // CASE B: Course Materials (Accessible to all logged-in users)
            else if (cleanPath.startsWith("materials/") || cleanPath.startsWith("videos/")) {
                // Do nothing
            }

            // CASE C: Profile Pictures (Accessible to all logged-in users)
            else if (cleanPath.startsWith("profiles/")) {
                // Do nothing
            }
            
            // CASE D: Unknown folders
            else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Folder not recognized");
                return;
            }

            // --- SECURITY LOGIC END ---

            // 3. Locate and Serve the File
            // FileUtil.getUploadPath() returns the base root, e.g., "C:/elearning/uploads"
            File file = new File(FileUtil.getUploadPath(), cleanPath);

            // Double-check file is truly inside upload dir (Canonical Path check)
            String canonicalDestination = file.getCanonicalPath();
            String canonicalRoot = new File(FileUtil.getUploadPath()).getCanonicalPath();
            
            if (!canonicalDestination.startsWith(canonicalRoot)) {
                 resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Path Traversal Detected");
                 return;
            }

            if (file.exists() && file.isFile()) {
                String contentType = getServletContext().getMimeType(file.getName());
                resp.setContentType(contentType != null ? contentType : "application/octet-stream");
                resp.setContentLength((int) file.length());

                // Actual byte streaming (YOUR REQUESTED IMPLEMENTATION)
                try (FileInputStream in = new FileInputStream(file);
                     OutputStream out = resp.getOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found on server");
            }
        } catch (IOException e) {
            // Log the error internally, don't expose stack trace
            System.err.println("Error serving file: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * SECURE UPLOAD (POST)
     * Handles file uploads enforcing "Upload Types", Roles, and Naming Conventions.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = resp.getWriter();
        
        try {
            // Ensure directories exist
            FileUtil.prepareDirectories(); 
            
            Integer userId = (Integer) req.getAttribute("userId");
            String role = (String) req.getAttribute("role");

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unauthorized");
                out.print(jsonResponse.toString());
                return;
            }

            // 1. Get Upload Type (REQUIRED)
            // The frontend MUST send this field: formData.append("type", "submission");
            String uploadType = req.getParameter("type");
            
            if (uploadType == null || uploadType.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Missing 'type' parameter. Valid values: submission, course_content, profile");
                out.print(jsonResponse.toString());
                return;
            }

            // 2. Validate Role Permissions & Determine Folder
            String subFolder;
            
            if ("course_content".equalsIgnoreCase(uploadType)) {
                if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                    throw new SecurityException("Only teachers can upload course content.");
                }
                subFolder = "materials"; 

            } else if ("submission".equalsIgnoreCase(uploadType)) {
                if (!"STUDENT".equalsIgnoreCase(role)) {
                    throw new SecurityException("Only students can upload submissions.");
                }
                subFolder = "submissions";

            } else if ("profile".equalsIgnoreCase(uploadType)) {
                // Anyone logged in can upload a profile picture
                subFolder = "profiles";

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid upload type. Valid values: submission, course_content, profile");
                out.print(jsonResponse.toString());
                return;
            }

            // 3. Process File Part
            Part filePart = req.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                
                // Extract extension
                String fileExtension = ""; 
                int dotIndex = originalFileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileExtension = originalFileName.substring(dotIndex).toLowerCase();
                }

                // Special handling: Move videos to specific folder if uploading course content
                if ("materials".equals(subFolder) && ".mp4".equals(fileExtension)) {
                    subFolder = "videos";
                }

                // Format: {type}_u{userId}_{timestamp}.{ext}
                String fileNameBase = uploadType + "_u" + userId + "_" + System.currentTimeMillis();
                String uniqueFileName = fileNameBase + fileExtension;
                
                // 5. Save File
                File destinationFolder = new File(FileUtil.getPath(subFolder));
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs(); // Ensure specific subfolder exists
                }

                File uploadFile = new File(destinationFolder, uniqueFileName);
                
                // Write the file to disk
                filePart.write(uploadFile.getAbsolutePath());

                // 6. Return the relative path for the Database
                // This path (e.g., "submissions/submission_u5_123.pdf") is what you save in your DB table
                String dbPath = subFolder + "/" + uniqueFileName;

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "File uploaded successfully");
                jsonResponse.addProperty("fileName", uniqueFileName);
                jsonResponse.addProperty("filePath", dbPath);
                
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "No file part found in request");
            }
            
            out.print(jsonResponse.toString());
            
        } catch (SecurityException se) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // We create a new JSON object to ensure we don't send partial data
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", se.getMessage());
            out.print(err.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("status", "error");
            err.addProperty("message", "Upload failed: " + e.getMessage());
            out.print(err.toString());
        }
    }
}