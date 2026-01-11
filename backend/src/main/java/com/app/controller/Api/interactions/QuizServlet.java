package com.app.controller.Api.interactions;

import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.interactions.Quiz;
import com.app.service.ChapterService;
import com.app.service.CourseService;
import com.app.service.ModuleService;
import com.app.service.QuizService;
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

/*
    Teacher/Admin:
    POST : Create Quiz
        Body: { "chapterId": 1, "title": "Quiz 1", "description": "...", "availableFrom": "...", "availableTo": "..." }

    DELETE : Delete Quiz
        Body: { "quizId": ... } or { "id": ... }

    PUT : Update Quiz
        Body: { "quizId": ..., "title": "Updated Title", ... }

    GET : Get Quiz Details
        URL: /api/quiz?quizId=...

    GET : List All Quizzes in a Chapter
        URL: /api/quiz?chapterId=...
*/

@WebServlet("/api/quiz")
public class QuizServlet extends HttpServlet {

    private final QuizService quizService = new QuizService();
    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    
    private final Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy HH:mm:ss").create(); 

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String chapterIdParam = req.getParameter("chapterId");
        String quizIdParam = req.getParameter("quizId");
        
        Integer userIdObj = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userIdObj == null) {
            resp.setStatus(401);
            out.println("{\"error\": \"You have to be logged in\"}");
            return;
        }
        int userId = userIdObj;

        try {
            if (chapterIdParam != null) {
                int chapterId = Integer.parseInt(chapterIdParam);
                
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view its quizzes.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view quizzes of this course.\"}");
                        return;
                    }
                }

                List<Quiz> quizzes = quizService.getQuizzesByChapter(chapterId, role);
                out.print(gson.toJson(quizzes));

            } 
            else if (quizIdParam != null) {
                int quizId = Integer.parseInt(quizIdParam);
                Quiz quiz = quizService.getQuizById(quizId, role);
                if(quiz == null) {
                    resp.setStatus(404);
                    out.print("{\"error\": \"Quiz not found.\"}");
                    return;
                }
                Chapter chapter = chapterService.getChapterById(quiz.getChapterId());
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view this quiz.\"}");
                        return;
                    }
                    
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view this quiz.\"}");
                        return;
                    }
                }
                out.print(gson.toJson(quiz));
            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing chapterId or quizId parameter\"}");
            }

        } catch (Exception e) {
            resp.setStatus(400);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "CREATE");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "UPDATE");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWriteRequest(req, resp, "DELETE");
    }

    private void handleWriteRequest(HttpServletRequest req, HttpServletResponse resp, String action) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        JsonObject responseJson = new JsonObject();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            return;
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isTeacher && !isAdmin) {
            resp.setStatus(403);
            out.print("{\"error\": \"Only teachers or admins can modify quizzes.\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Quiz quiz = gson.fromJson(body, Quiz.class);
                quizService.createQuiz(quiz, userId, role);
                responseJson.addProperty("message", "Quiz created successfully");

            } else if ("UPDATE".equals(action)) {
                Quiz quiz = gson.fromJson(body, Quiz.class);
                if (!body.has("quizId") && !body.has("id")) {
                    throw new Exception("quizId is required");
                }
                int id = body.has("quizId") ? body.get("quizId").getAsInt() : body.get("id").getAsInt();
                quiz.setId(id);
                
                quizService.updateQuiz(quiz, userId, role);
                responseJson.addProperty("message", "Quiz updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("quizId") && !body.has("id")) {
                    throw new Exception("quizId is required");
                }
                int quizId = body.has("quizId") ? body.get("quizId").getAsInt() : body.get("id").getAsInt();
                
                quizService.deleteQuiz(quizId, userId, role);
                responseJson.addProperty("message", "Quiz deleted successfully");
            }
            
            responseJson.addProperty("status", "success");

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