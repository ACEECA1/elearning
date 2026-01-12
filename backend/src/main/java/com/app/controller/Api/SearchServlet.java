package com.app.controller.api;

import com.app.model.course.Course;
import com.app.service.course.CourseService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    private final CourseService courseService = new CourseService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String query = req.getParameter("q");

        if (query == null || query.trim().isEmpty()) {
            resp.setStatus(400);
            out.print("{\"error\": \"Search query 'q' is required\"}");
            return;
        }

        try {
            List<Course> results = courseService.search(query);
            out.print(gson.toJson(results));
        } catch (Exception e) {
            resp.setStatus(500);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}