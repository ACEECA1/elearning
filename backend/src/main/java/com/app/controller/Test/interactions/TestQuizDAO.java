package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.QuizDAO;
import com.app.model.interactions.Quiz;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/test/api/quiz/*")
public class TestQuizDAO extends HttpServlet {

    private QuizDAO quizDAO;
    // Date format must match what you send from Postman/React
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void init() {
        quizDAO = new QuizDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonInput = sb.toString();

            if ("/insert".equals(pathInfo)) {
                Quiz quiz = gson.fromJson(jsonInput, Quiz.class);
                quizDAO.insert(quiz);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Quiz inserted with ID: " + quiz.getId());
                jsonResponse.add("quiz", gson.toJsonTree(quiz));

            } else if ("/update".equals(pathInfo)) {
                Quiz quiz = gson.fromJson(jsonInput, Quiz.class);
                quizDAO.update(quiz);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Quiz " + quiz.getId() + " updated successfully.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int quizId = jobj.get("id").getAsInt();
                quizDAO.delete(quizId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Quiz " + quizId + " deleted.");

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unknown endpoint: " + pathInfo);
            }
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo != null && pathInfo.startsWith("/find")) {
                String idStr = req.getParameter("id");
                String chapterIdStr = req.getParameter("chapterId");

                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Quiz quiz = quizDAO.findById(id);
                    if (quiz != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("quiz", gson.toJsonTree(quiz));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Quiz not found");
                    }
                } else if (chapterIdStr != null) {
                    int chapterId = Integer.parseInt(chapterIdStr);
                    List<Quiz> quizzes = quizDAO.findByChapterId(chapterId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("quizzes", gson.toJsonTree(quizzes));
                } else {
                    List<Quiz> quizzes = quizDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("quizzes", gson.toJsonTree(quizzes));
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.addProperty("status", "error");
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}