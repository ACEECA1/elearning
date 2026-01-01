package com.app.controller.Test;

import com.app.dao.implementation.users.AdminDAO;
import com.app.model.users.*;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
@WebServlet("/testAdminDAO")
public class TestAdminDAO extends HttpServlet {
    public AdminDAO adminDAO;
    @Override
    public void init() {
        adminDAO = new AdminDAO();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            Admin admin = new Admin(new User("walid123","Walid", "Chemat", "walidchemat@gmail.com", "123123", "salt" , false));
            try{
                adminDAO.insert(admin);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Admin inserted with ID: " + admin.getId());
                jsonResponse.addProperty("admin", admin.toString());
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Insertion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                adminDAO.update(admin);
                jsonResponse.addProperty("message", "Admin with ID " + admin.getId() + " updated.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Update failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                adminDAO.delete(admin.getId());
                jsonResponse.addProperty("message", "Admin with ID " + admin.getId() + " deleted.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Deletion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            System.out.println("Error during TestAdminDAO operations: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }   
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "POST method in TestAdminDAO is operational.");
            out.println(jsonResponse.toString());
        } catch (Exception e) {
            System.out.println("Error during TestAdminDAO POST operation: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }
    }
}
