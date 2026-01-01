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
            User user = new User("walid123","Walid", "Chemat", "walidchemat@gmail.com", "123123", "salt" , false);
            try{
                userDAO.insert(user);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "User inserted with ID: " + user.getId());
                jsonResponse.addProperty("user", user.toString());
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Insertion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                user.setFirstName("UpdatedName");
                userDAO.update(user);
                jsonResponse.addProperty("message", "User updated : " + user.toString());
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Update failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            
        } catch (Exception e) {
            System.out.println("Error during TestUserDAO operations: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }   
    }
    protected void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "POST method in TestUserDAO is operational.");
            out.println(jsonResponse.toString());
        } catch (Exception e) {
            System.out.println("Error during TestUserDAO POST operation: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }
    }
}
