package com.app.controller.Test.interactions; // or com.app.controller.Test.student

import com.app.dao.implementation.interactions.NoteDAO;
import com.app.model.interactions.Note;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/note/*")
public class TestNoteDAO extends HttpServlet {

    private NoteDAO noteDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        noteDAO = new NoteDAO();
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
                Note note = gson.fromJson(jsonInput, Note.class);
                noteDAO.insert(note);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Note inserted. Student ID: " + note.getStudentId() + ", Quiz ID: " + note.getQuizId());
                jsonResponse.add("note", gson.toJsonTree(note));

            } else if ("/update".equals(pathInfo)) {
                Note note = gson.fromJson(jsonInput, Note.class);
                noteDAO.update(note);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Note updated.");

            } else if ("/delete".equals(pathInfo)) {
                JsonObject jobj = gson.fromJson(jsonInput, JsonObject.class);
                int studentId = jobj.get("studentId").getAsInt();
                int quizId = jobj.get("quizId").getAsInt();
                noteDAO.delete(studentId, quizId);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Note deleted.");
            } else {
                resp.setStatus(404);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid path");
            }
            out.print(jsonResponse.toString());

        } catch (Exception e) {
            resp.setStatus(500);
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
                String studentIdStr = req.getParameter("studentId");
                String quizIdStr = req.getParameter("quizId");
                if (studentIdStr != null && quizIdStr != null) {
                    // Find Single Note by Composite ID
                    int studentId = Integer.parseInt(studentIdStr);
                    int quizId = Integer.parseInt(quizIdStr);
                    Note note = noteDAO.findByCompositeId(studentId, quizId);
                    if (note != null) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.add("note", gson.toJsonTree(note));
                    } else {
                        resp.setStatus(404);
                        jsonResponse.addProperty("status", "not found");
                    }
                } else if (studentIdStr != null) {
                    // Find Notes by Student (Report Card)
                    int sId = Integer.parseInt(studentIdStr);
                    List<Note> notes = noteDAO.findByStudentId(sId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("notes", gson.toJsonTree(notes));
                } else if (quizIdStr != null) {
                    // Find Notes by Quiz (Class Stats)
                    int qId = Integer.parseInt(quizIdStr);
                    List<Note> notes = noteDAO.findByQuizId(qId);
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("notes", gson.toJsonTree(notes));
                } else {
                    // Find All
                    List<Note> notes = noteDAO.findAll();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.add("notes", gson.toJsonTree(notes));
                }
                out.println(jsonResponse.toString());
            } else {
                resp.setStatus(404);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid path");
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            resp.setStatus(500);
            System.out.println("Error processing request: " + e.getMessage());
        }
    }
}