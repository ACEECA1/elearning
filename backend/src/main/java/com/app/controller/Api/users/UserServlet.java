package com.app.controller.api.users;

import com.app.model.users.Student;
import com.app.model.users.Teacher;
import com.app.model.users.User;
import com.app.service.users.UserService;
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

@WebServlet("/api/user/*")
/*
    ADMIN ONLY ENDPOINTS:
    
    GET /api/user
        ?id=1           -> Get specific user
        ?role=STUDENT   -> Get all students
        ?role=TEACHER   -> Get all teachers
        (no params)     -> Get all users
    GET /api/user/profile
        (Authenticated user only) Get own profile
    POST /api/user/student
        Body: { "username": "...", "email": "...", "password": "...", "firstName": "...", "lastName": "...", "studentCardNumber": "...", "academicYear": "..." }

    POST /api/user/teacher
        Body: { "username": "...", "email": "...", "password": "...", "firstName": "...", "lastName": "...", "domain": "...", "grade": "..." }

    PUT /api/user/student
        Body: { "userId": 1, ...fields to update... }

    PUT /api/user/teacher
        Body: { "userId": 1, ...fields to update... }

    DELETE /api/user
        Body: { "userId": 1 }
*/
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private boolean isAdmin(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        
        
        String pathInfo = req.getPathInfo();
        if("/profile".equals(pathInfo)) {
            Integer userId = (Integer) req.getAttribute("userId");
            String role = (String) req.getAttribute("role");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            try {
                if("STUDENT".equalsIgnoreCase(role)) {
                    Student student = (Student) userService.getUserById(userId);
                    out.print(gson.toJson(student));
                    return;
                } else if("TEACHER".equalsIgnoreCase(role)) {
                    Teacher teacher = (Teacher) userService.getUserById(userId);
                    out.print(gson.toJson(teacher));
                    return;
                }else if("ADMIN".equalsIgnoreCase(role)) {
                    User admin = userService.getUserById(userId);
                    out.print(gson.toJson(admin));
                    return;
                } else {
                    resp.setStatus(400);
                    out.print("{\"error\": \"Invalid role\"}");
                    return;
                }
            } catch (Exception e) {
                resp.setStatus(500);
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
            }
            return;
        }
        else if ("/total".equals(pathInfo)) {
            try{
                int count = userService.userCount();
                out.print("{\"count\": " + count + "}");
                return;
            }
            catch(Exception e){
                resp.setStatus(500);
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
                return;
            }
        }
        else{
            if (!isAdmin(req)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Access Denied. Admins only.\"}");
                return;
            }
        }
        try {
            String idParam = req.getParameter("id");
            String roleParam = req.getParameter("role");

            if (idParam != null) {
                User user = userService.getUserById(Integer.parseInt(idParam));
                if (user != null) {
                    out.print(gson.toJson(user));
                } else {
                    resp.setStatus(404);
                    out.print("{\"error\": \"User not found\"}");
                }
            } else if ("STUDENT".equalsIgnoreCase(roleParam)) {
                List<Student> students = userService.getAllStudents();
                out.print(gson.toJson(students));
            } else if ("TEACHER".equalsIgnoreCase(roleParam)) {
                List<Teacher> teachers = userService.getAllTeachers();
                out.print(gson.toJson(teachers));
            } else {
                List<User> users = userService.getAllUsers();
                out.print(gson.toJson(users));
            }

        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Access Denied. Admins only.\"}");
            return;
        }

        String pathInfo = req.getPathInfo();

        try {
            JsonObject body = parseBody(req);

            if ("/student".equals(pathInfo)) {
                Student student = gson.fromJson(body, Student.class);
                String password = body.get("password").getAsString();
                userService.addStudent(student, password);
                responseJson.addProperty("message", "Student added successfully");

            } else if ("/teacher".equals(pathInfo)) {
                Teacher teacher = gson.fromJson(body, Teacher.class);
                String password = body.get("password").getAsString();
                userService.addTeacher(teacher, password);
                responseJson.addProperty("message", "Teacher added successfully");

            } else {
                throw new Exception("Invalid endpoint. Use /student or /teacher");
            }

            responseJson.addProperty("status", "success");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Access Denied. Admins only.\"}");
            return;
        }

        String pathInfo = req.getPathInfo();

        try {
            JsonObject body = parseBody(req);
            
            int userId = body.has("userId") ? body.get("userId").getAsInt() : body.get("id").getAsInt();

            if ("/student".equals(pathInfo)) {
                Student student = gson.fromJson(body, Student.class);
                student.setId(userId);
                userService.updateStudent(student);
                responseJson.addProperty("message", "Student updated successfully");

            } else if ("/teacher".equals(pathInfo)) {
                Teacher teacher = gson.fromJson(body, Teacher.class);
                teacher.setId(userId);
                userService.updateTeacher(teacher);
                responseJson.addProperty("message", "Teacher updated successfully");

            } else {
                throw new Exception("Invalid endpoint. Use /student or /teacher");
            }

            responseJson.addProperty("status", "success");

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Access Denied. Admins only.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);
            if (!body.has("userId") && !body.has("id")) {
                throw new Exception("userId is required");
            }
            int userId = body.has("userId") ? body.get("userId").getAsInt() : body.get("id").getAsInt();

            userService.deleteUser(userId);

            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "User deleted successfully");

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