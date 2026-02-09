package com.app.controller.api.course;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.app.model.course.Course;
import com.app.model.users.Student;
import com.app.service.course.CourseService;

import java.util.List;

/*
    The user id will be passed from the AuthentificationFilter as a request attribute "userId"
    /api/course
    if the user is a teacher
    POST : (Format: application/json)
        Body: { ...courseDetails }
    PUT : (Format: application/json)
        Body: { "courseId": courseId, ...updatedCourseDetails }
    DELETE : 
        Body: { "courseId": courseId }
    /api/course/details
    GET :
        Query Params: ?courseId=courseId
        Response: { ...courseDetails }
    /api/course/students
    if the user is a teacher
    GET : 
        Query Params: ?courseId=courseId
        Response: [ { ...student1Details }, { ...student2Details }, ... ]
    /api/course/list
    Courses of the teacher
    GET :
        Response: [ { ...course1Details }, { ...course2Details }, ... ]
    /api/course/enroll
    if the user is a student
    POST : (Format: application/json)
        Body: { "courseId": courseId, "code" : enrollmentCode }
    /api/course/unenroll
    if the user is a student
    POST : (Format: application/json)
        Body: { "courseId": courseId }
    /api/course/my-courses
    GET :
        Response: [ { ...course1Details }, { ...course2Details }, ... ]
    /api/course/available
    GET :
        Response: [ { ...course1Details }, { ...course2Details }, ... ]
*/

@WebServlet("/api/course/*")
public class CourseServlet extends HttpServlet {

    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    // GET REQUESTS (Read Operations)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String pathInfo = req.getPathInfo();
        
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");
        
        if (userIdObj == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        int userId = userIdObj;

        try {
            // 1. /api/course/details?courseId=123
            if ("/details".equals(pathInfo)) {
                if("STUDENT".equalsIgnoreCase(role) && !courseService.isStudentEnrolledInCourse(userId, Integer.parseInt(req.getParameter("courseId")))){
                    throw new Exception("Access Denied: You are not enrolled in this course.");
                }

                String idParam = req.getParameter("courseId");
                if (idParam == null) throw new Exception("Missing courseId parameter");
                
                Course course = courseService.getCourseById(Integer.parseInt(idParam));
                out.print(gson.toJson(course));

            // 2. /api/course/list (Teacher's created courses)
            } else if ("/list".equals(pathInfo)) {
                List<Course> courses = null;
                if ("ADMIN".equalsIgnoreCase(role)) {
                    courses = courseService.getAllAvailableCourses(); // Get ALL courses for admin
                } else if ("TEACHER".equalsIgnoreCase(role)) {
                    courses = courseService.getCoursesByTeacher(userId);
                }
                out.print(gson.toJson(courses));
            // 3. /api/course/my-courses (Student's enrolled courses)
            } else if ("/my-courses".equals(pathInfo)) {
                if (!"STUDENT".equalsIgnoreCase(role)) {
                    throw new Exception("Access Denied: Only students have enrolled courses.");
                }
                List<Course> courses = courseService.getEnrolledCourses(userId);
                out.print(gson.toJson(courses));

            // 4. /api/course/available (Public/All courses)
            } else if ("/available".equals(pathInfo)) {
                List<Course> courses = courseService.getAllAvailableCourses();
                out.print(gson.toJson(courses));
            }
            else if ("/count".equals(pathInfo)) {
                int count = courseService.getCourseCount();
                out.print("{\"courseCount\": " + count + "}");
            } else if ("/students".equals(pathInfo)) {
                if (!"TEACHER".equalsIgnoreCase(role)) {
                    throw new Exception("Access Denied: Only teachers can view students.");
                }
                if(!courseService.teacherOwnsCourse(userId, Integer.parseInt(req.getParameter("courseId")))){
                    throw new Exception("Access Denied: You do not own this course.");
                }
                String idParam = req.getParameter("courseId");
                if (idParam == null) throw new Exception("Missing courseId parameter");

                List<Student> students = courseService.getCourseParticipants(Integer.parseInt(idParam), userId);
                out.print(gson.toJson(students));

            } else {
                resp.setStatus(404);
                out.print("{\"error\": \"Endpoint not found\"}");
            }

        } catch (Exception e) {
            resp.setStatus(400); // Bad Request
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", e.getMessage());
            System.out.println("Error in GET /api/course: " + e.getMessage());
            out.print(error.toString());
        }
    }
    
    // POST REQUESTS (Create, Enroll, Unenroll)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        String pathInfo = req.getPathInfo();
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            return;
        }
        int userId = userIdObj;

        try {
            JsonObject body = parseBody(req);

            // 1. /api/course (Create Course - Teacher Only)
            if (pathInfo == null || "/".equals(pathInfo)) {
                boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
                boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
                if (!isTeacher && !isAdmin) {
                    throw new Exception("Only teachers or admins can create courses.");
                }
                
                Course newCourse = gson.fromJson(body, Course.class);
                courseService.createCourse(newCourse, userId);
                
                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Course created successfully.");

            // 2. /api/course/enroll (Student Only)
            } else if ("/enroll".equals(pathInfo)) {
                if (!"STUDENT".equalsIgnoreCase(role)) {
                    throw new Exception("Only students can enroll.");
                }
                
                int courseId = body.get("courseId").getAsInt();
                String code = body.has("code") ? body.get("code").getAsString() : "";
                
                courseService.enrollStudent(courseId, userId, code);
                
                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Enrolled successfully.");

            // 3. /api/course/unenroll (Student Only)
            } else if ("/unenroll".equals(pathInfo)) {
                if (!"STUDENT".equalsIgnoreCase(role)) {
                    throw new Exception("Only students can unenroll.");
                }

                int courseId = body.get("courseId").getAsInt();
                courseService.unenrollStudent(courseId, userId);
                
                responseJson.addProperty("status", "success");
                responseJson.addProperty("message", "Unenrolled successfully.");

            } else {
                resp.setStatus(404);
                responseJson.addProperty("status", "error");
                responseJson.addProperty("message", "Endpoint not found");
            }

        } catch (Exception e) {
            resp.setStatus(400);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        }
        out.print(responseJson.toString());
    }

    // PUT REQUEST (Update Course)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            return;
        }

        try {
            boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            if (!isTeacher && !isAdmin) {
                throw new Exception("Only teachers or admins can update courses.");
            }

            JsonObject body = parseBody(req);
            Course courseUpdates = gson.fromJson(body, Course.class);
            
            // The JSON must contain "courseId" or "id" to know which one to update
            if (body.has("courseId")) {
                courseUpdates.setId(body.get("courseId").getAsInt());
            }

            courseService.updateCourse(courseUpdates, userIdObj , role);

            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Course updated successfully.");

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

        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            return;
        }

        try {
            boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            if (!isTeacher && !isAdmin) {
                throw new Exception("Only teachers or admins can delete courses.");
            }

            // Parse body to get { "courseId": 1 }
            JsonObject body = parseBody(req);
            if (!body.has("courseId")) {
                throw new Exception("courseId is required in the request body.");
            }
            
            int courseId = body.get("courseId").getAsInt();
            courseService.deleteCourse(courseId, userIdObj, role);

            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Course deleted successfully.");

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