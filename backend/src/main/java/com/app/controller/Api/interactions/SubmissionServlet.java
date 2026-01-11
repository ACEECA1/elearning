package com.app.controller.Api.interactions;

import com.app.model.interactions.Submission;
import com.app.service.interactions.SubmissionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Map;

/*
    Student:
    POST : Submit Quiz (File Upload)
        Body: { 
            "quizId": 1, 
            "type": "FILE", 
            "filePath": "/uploads/math_assignment.pdf" 
        }

    POST : Submit Quiz (Multiple Choice)
        Body: { 
            "quizId": 2, 
            "type": "MCQ", 
            "answers": { 
                "101": 5,  // "QuestionID": OptionID
                "102": 8 
            } 
        }

    Teacher/Admin:
    PUT : Grade a Submission
        Body: { 
            "studentId": 5, 
            "quizId": 1, 
            "grade": 18.5, 
            "feedback": "Excellent work on the calculus section." 
        }

    GET : Get Specific Submission Details
        URL: /api/submission?studentId=5&quizId=1

    GET : List All Submissions for a Quiz
        URL: /api/submission?quizId=1

    GET : List All Submissions by a Student
        URL: /api/submission?studentId=5

    DELETE : Remove a Submission
        Body: { "studentId": 5, "quizId": 1 }
*/

@WebServlet("/api/submission")
public class SubmissionServlet extends HttpServlet {

    private SubmissionService submissionService = new SubmissionService();
    private Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 1. Authentication Check
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of("error", "Unauthorized.")));
            return;
        }

        // 2. Authorization: Only Teachers or Admins can grade
        if (!"TEACHER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.write(gson.toJson(Map.of("error", "Forbidden. Only teachers can grade submissions.")));
            return;
        }

        try {
            // 3. Parse JSON Body
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = JsonParser.parseReader(reader).getAsJsonObject();

            if (!jsonBody.has("studentId") || !jsonBody.has("quizId") || !jsonBody.has("grade")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("error", "Missing required fields: studentId, quizId, grade")));
                return;
            }

            int studentId = jsonBody.get("studentId").getAsInt();
            int quizId = jsonBody.get("quizId").getAsInt();
            double grade = jsonBody.get("grade").getAsDouble();
            String feedback = jsonBody.has("feedback") ? jsonBody.get("feedback").getAsString() : null;

            // 4. Call Service
            boolean success = submissionService.gradeSubmission(studentId, quizId, grade, feedback);

            if (success) {
                out.write(gson.toJson(Map.of("message", "Submission graded successfully.")));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(Map.of("error", "Submission not found.")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("error", "Server error: " + e.getMessage())));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of("error", "Unauthorized.")));
            return;
        }

        String studentIdParam = request.getParameter("studentId");
        String quizIdParam = request.getParameter("quizId");

        try {
            // Case A: Get Specific Submission (studentId + quizId)
            if (studentIdParam != null && quizIdParam != null) {
                int studentId = Integer.parseInt(studentIdParam);
                int quizId = Integer.parseInt(quizIdParam);

                // Security: Student can only view their own. Teacher/Admin can view any.
                if ("STUDENT".equalsIgnoreCase(role) && !userId.equals(studentId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(Map.of("error", "You can only view your own submissions.")));
                    return;
                }

                Submission submission = submissionService.getSubmission(studentId, quizId);
                if (submission != null) {
                    out.write(gson.toJson(submission));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(Map.of("error", "Submission not found.")));
                }
            } 
            // Case B: Get All Submissions for a Quiz (Teacher Only)
            else if (quizIdParam != null) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(Map.of("error", "Students cannot list all submissions.")));
                    return;
                }
                int quizId = Integer.parseInt(quizIdParam);
                out.write(gson.toJson(submissionService.getSubmissionsByQuiz(quizId)));
            }
            // Case C: Get All Submissions by a Student
            else if (studentIdParam != null) {
                int studentId = Integer.parseInt(studentIdParam);
                
                if ("STUDENT".equalsIgnoreCase(role) && !userId.equals(studentId)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(Map.of("error", "You can only view your own history.")));
                    return;
                }
                out.write(gson.toJson(submissionService.getSubmissionsByStudent(studentId)));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("error", "Missing parameters. Provide studentId, quizId, or both.")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("error", "Server error: " + e.getMessage())));
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 1. Authentication Check
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        // Strict check: Only logged-in users (ideally specifically students) can submit
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write(gson.toJson(Map.of("error", "Unauthorized. Please log in.")));
            return;
        }

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = JsonParser.parseReader(reader).getAsJsonObject();

            if (!jsonBody.has("quizId") || !jsonBody.has("type")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("error", "Missing required fields: quizId, type")));
                return;
            }

            int quizId = jsonBody.get("quizId").getAsInt();
            String type = jsonBody.get("type").getAsString();
            if(!"STUDENT".equalsIgnoreCase(role)){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(Map.of("error", "Only students can submit answers to quizzes.")));
                return;
            }
            if ("FILE".equalsIgnoreCase(type)) {
                
                if (!jsonBody.has("filePath")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(Map.of("error", "Missing required field: filePath")));
                    return;
                }
                
                String filePath = jsonBody.get("filePath").getAsString();
                submissionService.submitFile(userId, quizId, filePath);
                
                out.write(gson.toJson(Map.of("message", "File submitted successfully.")));

            } else if ("MCQ".equalsIgnoreCase(type)) {
                
                if (!jsonBody.has("answers")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(Map.of("error", "Missing required field: answers")));
                    return;
                }

                // Convert "answers" JSON object to Java Map<QuestionID, AnswerID>
                Type mapType = new TypeToken<Map<Integer, Integer>>(){}.getType();
                Map<Integer, Integer> answers = gson.fromJson(jsonBody.get("answers"), mapType);

                double score = submissionService.submitMCQ(userId, quizId, answers);
                
                out.write(gson.toJson(Map.of("message", "Quiz submitted.", "grade", score)));

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(Map.of("error", "Invalid submission type. Must be 'FILE' or 'MCQ'.")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(Map.of("error", "Server error: " + e.getMessage())));
        }
    }
}