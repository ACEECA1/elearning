package com.app.controller.Test.interactions;

import com.app.dao.implementation.interactions.AnswerDAO;
import com.app.model.interactions.Answer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/answer/*")
public class TestAnswerDAO extends HttpServlet {

    private AnswerDAO answerDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        answerDAO = new AnswerDAO();
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
                Answer answer = gson.fromJson(jsonInput, Answer.class);
                answerDAO.insert(answer);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Answer inserted ID: " + answer.getId());
                jsonResponse.add("answer", gson.toJsonTree(answer));

            } else if ("/update".equals(pathInfo)) {
                Answer answer = gson.fromJson(jsonInput, Answer.class);
                answerDAO.update(answer);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Answer updated.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int id = jobj.get("id").getAsInt();
                answerDAO.delete(id);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Answer deleted.");
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
                String questionIdStr = req.getParameter("questionId");

                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Answer a = answerDAO.findById(id);
                    if(a != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("answer", gson.toJsonTree(a));
                    } else {
                        resp.setStatus(404);
                    }
                } else if (questionIdStr != null) {
                    int qId = Integer.parseInt(questionIdStr);
                    List<Answer> answers = answerDAO.findByQuestionId(qId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("answers", gson.toJsonTree(answers));
                } else {
                    List<Answer> answers = answerDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("answers", gson.toJsonTree(answers));
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