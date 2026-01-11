package com.app.controller.Auth;

import com.app.model.users.Student;
import com.app.model.users.Teacher;
import com.app.service.auth.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    /*
    POST : (Format: application/json)
        /send-code
            Body: { "email": email }
        /register/student
            Body: { "email": email, "password": password, "verificationCode": code, ...studentDetails }
        /register/teacher
            Body: { "email": email, "password": password, "verificationCode": code, ...teacherDetails }
        /login
            Body: { "email": email, "password": password }
    */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            if (sb.length() == 0){
                resp.setStatus(400);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Empty request body");
                out.print(jsonResponse.toString());
                System.out.println("Empty request body");
                return;
            } 
            
            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String pathInfo = req.getPathInfo();

            
            // send verification code to email
            if ("/send-code".equals(pathInfo)) {
                if (!jsonObject.has("email")) {
                    resp.setStatus(400);
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Email is required");
                    out.print(jsonResponse.toString());
                    System.out.println("Email is required");
                    return;
                }
                
                String email = jsonObject.get("email").getAsString();
                authService.sendVerificationCode(email);

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Verification code sent to email.");

            // register student
            } else if ("/register/student".equals(pathInfo)) {
                validateRegistrationRequest(jsonObject);
                
                String code = jsonObject.get("verificationCode").getAsString();
                String pass = jsonObject.get("password").getAsString();
                
                Student student = gson.fromJson(jsonObject, Student.class);

                authService.registerStudent(student, pass, code);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student registered successfully.");

            // register teacher
            } else if ("/register/teacher".equals(pathInfo)) {
                validateRegistrationRequest(jsonObject);

                String code = jsonObject.get("verificationCode").getAsString();
                String pass = jsonObject.get("password").getAsString();
                
                Teacher teacher = gson.fromJson(jsonObject, Teacher.class);
                authService.registerTeacher(teacher, pass, code);
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Teacher registered successfully.");

            // login
            } else if ("/login".equals(pathInfo)) {
                if (!jsonObject.has("email") || !jsonObject.has("password")) {
                    throw new Exception("Email and password are required");
                }

                String email = jsonObject.get("email").getAsString();
                String password = jsonObject.get("password").getAsString();

                String token = authService.login(email, password);
                Cookie tokenCookie = new Cookie("authToken", token);
                tokenCookie.setHttpOnly(true);
                tokenCookie.setSecure(false);
                tokenCookie.setPath("/");
                tokenCookie.setMaxAge(60 * 60 * 10); 
                resp.addCookie(tokenCookie);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Login successful.");
                JsonObject userJson = authService.getUserInfoAsJson(email);
                jsonResponse.add("user", userJson);
            }
            else if("/logout".equals(pathInfo)){
                Cookie tokenCookie = new Cookie("authToken", "");
                tokenCookie.setHttpOnly(true);
                tokenCookie.setSecure(false);
                tokenCookie.setPath("/");
                tokenCookie.setMaxAge(0); 
                resp.addCookie(tokenCookie);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Logout successful.");
            }
            else {
                resp.setStatus(404);
                System.out.println("Invalid endpoint: " + pathInfo);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid endpoint: " + pathInfo);
                return;
            }

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Invalid password") || errorMsg.contains("User not found")) {
                resp.setStatus(401);
                System.out.println("Authentication failed: " + errorMsg);
            } else if (errorMsg.contains("already registered")) {
                System.out.println("Error processing request: " + errorMsg);
                resp.setStatus(409);
            } else {
                System.out.println("Error processing request: " + errorMsg);
                resp.setStatus(400);
            }
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", errorMsg);
        }

        out.print(jsonResponse.toString());
    }
    
    private void validateRegistrationRequest(JsonObject json) throws Exception {
        if (!json.has("email")) {
            throw new Exception("Email is required");
        }
        if (!json.has("verificationCode") || json.get("verificationCode").getAsString().isEmpty()){
            throw new Exception("Missing or empty verification code");
        }
        if (!json.has("password") || json.get("password").getAsString().isEmpty()){
            throw new Exception("Missing or empty password");
        }
    }
}