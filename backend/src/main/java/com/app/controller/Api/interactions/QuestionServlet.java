package com.app.controller.api.interactions;

import com.app.model.interactions.Question;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.service.course.ChapterService;
import com.app.service.course.CourseService;
import com.app.service.course.ModuleService;
import com.app.service.interactions.QuestionService;
import com.app.service.interactions.QuizService;
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
import java.util.List;

/*
    Teacher/Admin:
    POST : Create Question
        Body: { "quizId": 1, "text": "What is 2+2?", "score": 5, "materialPath": "optional/image.png" }

    PUT : Update Question
        Body: { "questionId": ..., "text": "Updated text", "score": 10 }

    DELETE : Delete Question
        Body: { "questionId": ... }

    GET : List Questions (quizId required)
        URL: /api/question?quizId=1
*/
@WebServlet("/api/question")
public class QuestionServlet extends HttpServlet {

    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String quizIdParam = req.getParameter("quizId");
        String questionIdParam = req.getParameter("questionId");
        
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");
        if (userId == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            if (quizIdParam != null) {

                int quizId = Integer.parseInt(quizIdParam);
                int chapterId = quizService.getQuizById(quizId, role).getChapterId();
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view these questions.\"}");
                        return;
                    }
                }
                if("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view questions of this course.\"}");
                        return;
                    }
                }
                List<Question> questions = questionService.getQuestionsByQuiz(quizId);
                out.print(gson.toJson(questions));
            } else if (questionIdParam != null) {

                int id = Integer.parseInt(questionIdParam);
                Question question = questionService.getQuestionById(id);
                int quizId = question.getQuizId();
                int chapterId = quizService.getQuizById(quizId, role).getChapterId();
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();
                
                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view this question.\"}");
                        return;
                    }
                }
                if("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view questions of this course.\"}");
                        return;
                    }
                }
                out.print(gson.toJson(question));
            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing quizId or questionId parameter\"}");
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

        // RBAC: Only Teacher or Admin
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(403);
            out.print("{\"error\": \"Permission denied\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Question question = gson.fromJson(body, Question.class);
                questionService.createQuestion(question, userId, role);
                System.out.println("Created question with ID: " + question.getId());
                responseJson.addProperty("message", "Question created successfully");
                responseJson.addProperty("questionId", question.getId());
            } else if ("UPDATE".equals(action)) {
                Question question = gson.fromJson(body, Question.class);
                if (!body.has("questionId") && !body.has("id")) throw new Exception("questionId is required");
                
                int id = body.has("questionId") ? body.get("questionId").getAsInt() : body.get("id").getAsInt();
                question.setId(id);
                
                questionService.updateQuestion(question, userId, role);
                responseJson.addProperty("message", "Question updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("questionId") && !body.has("id")) throw new Exception("questionId is required");
                
                int id = body.has("questionId") ? body.get("questionId").getAsInt() : body.get("id").getAsInt();
                questionService.deleteQuestion(id, userId, role);
                responseJson.addProperty("message", "Question deleted successfully");
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