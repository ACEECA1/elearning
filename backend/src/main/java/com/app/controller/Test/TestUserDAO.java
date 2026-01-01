package com.app.controller.Test;

import com.app.dao.implementation.UserDAO;
import com.app.model.users.User;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
@WebServlet("/testUserDAO")
public class TestUserDAO extends HttpServlet {
    public UserDAO userDAO;
    @Override
    public void init() {
        userDAO = new UserDAO();
    }
    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            User user = new User("walid123","Walid", "Chemat", "walidchemat@gmail.com", "123123", "salt");
            userDAO.delete(user);
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "User "+ user.getUsername() + " deleted successfully");
            out.println(jsonResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }
}
