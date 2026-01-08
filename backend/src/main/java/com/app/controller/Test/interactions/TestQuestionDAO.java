package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.QuestionDAO;
import com.app.model.interactions.Question;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/test/api/question/*")
public class TestQuestionDAO extends HttpServlet {

    private QuestionDAO questionDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        questionDAO = new QuestionDAO();
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
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            String jsonInput = sb.toString();

            if ("/insert".equals(pathInfo)) {
                Question question = gson.fromJson(jsonInput, Question.class);
                questionDAO.insert(question);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Question inserted ID: " + question.getId());
                jsonResponse.add("question", gson.toJsonTree(question));

            } else if ("/update".equals(pathInfo)) {
                Question question = gson.fromJson(jsonInput, Question.class);
                questionDAO.update(question);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Question updated.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int id = jobj.get("id").getAsInt();
                questionDAO.delete(id);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Question deleted.");

            } else {
                resp.setStatus(404);
                jsonResponse.addProperty("status", "error");
            }
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            e.printStackTrace();
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
                String quizIdStr = req.getParameter("quizId");

                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Question q = questionDAO.findById(id);
                    if(q != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("question", gson.toJsonTree(q));
                    } else {
                        resp.setStatus(404);
                        jsonResponse.addProperty("status", "error");
                    }
                } else if (quizIdStr != null) {
                    int quizId = Integer.parseInt(quizIdStr);
                    List<Question> questions = questionDAO.findByQuizId(quizId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("questions", gson.toJsonTree(questions));
                } else {
                    List<Question> questions = questionDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("questions", gson.toJsonTree(questions));
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(404);
            }
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }
}