package com.app.controller.Api.interactions;

import com.app.model.interactions.Answer;
import com.app.model.interactions.Question;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.service.course.ChapterService;
import com.app.service.course.CourseService;
import com.app.service.course.ModuleService;
import com.app.service.interactions.AnswerService;
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
    POST : Create Answer
        Body: { "questionId": 1, "text": "Option A", "isCorrect": true }

    PUT : Update Answer
        Body: { "answerId": ..., "text": "Updated Option A", "isCorrect": false }

    DELETE : Delete Answer
        Body: { "answerId": ... }

    GET : List Answers (questionId required)
        URL: /api/answer?questionId=1
*/
@WebServlet("/api/answer")
public class AnswerServlet extends HttpServlet {

    private final AnswerService answerService = new AnswerService();
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
        
        String questionIdParam = req.getParameter("questionId");
        String answerIdParam = req.getParameter("answerId");
        
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("role");

        if (userId == null) {
            resp.setStatus(401);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        try {
            if (questionIdParam != null) {
                // 1. Fetch Hierarchy for LIST of Answers
                int qId = Integer.parseInt(questionIdParam);
                
                Question question = questionService.getQuestionById(qId);
                int quizId = question.getQuizId();
                int chapterId = quizService.getQuizById(quizId, role).getChapterId();
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                // 2. Perform Security Checks
                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view these answers.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view answers of this course.\"}");
                        return;
                    }
                }

                // 3. Return Data
                List<Answer> answers = answerService.getAnswersByQuestion(qId);
                if ("STUDENT".equalsIgnoreCase(role)) {
                for (Answer a : answers) {
                        a.setCorrect(false);
                    }
                }
                out.print(gson.toJson(answers));

            } else if (answerIdParam != null) {
                // 1. Fetch Hierarchy for SINGLE Answer
                int aId = Integer.parseInt(answerIdParam);
                
                Answer answer = answerService.getAnswerById(aId);
                int qId = answer.getQuestionId();
                Question question = questionService.getQuestionById(qId);
                int quizId = question.getQuizId();
                int chapterId = quizService.getQuizById(quizId, role).getChapterId();
                Chapter chapter = chapterService.getChapterById(chapterId);
                Module module = moduleService.getModuleById(chapter.getModuleId());
                int courseId = module.getCourseId();

                // 2. Perform Security Checks
                if ("STUDENT".equalsIgnoreCase(role)) {
                    boolean isEnrolled = courseService.isStudentEnrolledInCourse(userId, courseId);
                    if (!isEnrolled) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You must be enrolled in the course to view this answer.\"}");
                        return;
                    }
                }
                if ("TEACHER".equalsIgnoreCase(role)) {
                    boolean ownsCourse = courseService.teacherOwnsCourse(userId, courseId);
                    if (!ownsCourse) {
                        resp.setStatus(403);
                        out.print("{\"error\": \"You do not have permission to view this answer.\"}");
                        return;
                    }
                }
                if ("STUDENT".equalsIgnoreCase(role)) {
                    answer.setCorrect(false);
                }
                // 3. Return Data
                out.print(gson.toJson(answer));

            } else {
                resp.setStatus(400);
                out.print("{\"error\": \"Missing questionId or answerId parameter\"}");
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

        // Allow Teacher OR Admin
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(403);
            out.print("{\"error\": \"Permission denied\"}");
            return;
        }

        try {
            JsonObject body = parseBody(req);

            if ("CREATE".equals(action)) {
                Answer answer = gson.fromJson(body, Answer.class);
                answerService.createAnswer(answer, userId, role);
                responseJson.addProperty("message", "Answer created successfully");

            } else if ("UPDATE".equals(action)) {
                Answer answer = gson.fromJson(body, Answer.class);
                if (!body.has("answerId") && !body.has("id")) {
                    throw new Exception("answerId is required");
                }
                
                int id = body.has("answerId") ? body.get("answerId").getAsInt() : body.get("id").getAsInt();
                answer.setId(id);
                
                answerService.updateAnswer(answer, userId, role);
                responseJson.addProperty("message", "Answer updated successfully");

            } else if ("DELETE".equals(action)) {
                if (!body.has("answerId") && !body.has("id")) {
                    throw new Exception("answerId is required");
                }
                
                int id = body.has("answerId") ? body.get("answerId").getAsInt() : body.get("id").getAsInt();
                answerService.deleteAnswer(id, userId, role);
                responseJson.addProperty("message", "Answer deleted successfully");
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