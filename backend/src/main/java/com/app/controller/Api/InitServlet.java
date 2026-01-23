package com.app.controller.api;
import com.app.model.users.Admin;
import com.app.service.users.UserService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
// @WebServlet("/api/init")
public class InitServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Admin admin = new Admin(
            "username",
            "firstName",
            "lastName",
            "4ce010@gmail.com",
            "",
            "",
            null,
            true
        );
        UserService userService = new UserService();
        try {
            userService.addAdmin(admin, "admin123");
            out.println(gson.toJson("Admin user created successfully."));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println(gson.toJson("Error creating admin user: " + e.getMessage()));
        }
    }
}
