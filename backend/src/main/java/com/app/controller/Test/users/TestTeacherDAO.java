package com.app.controller.Test.users;

import com.app.dao.implementation.users.TeacherDAO;
import com.app.model.users.*;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
@WebServlet("/testTeacherDAO")
public class TestTeacherDAO extends HttpServlet {
    public TeacherDAO teacherDAO;
    @Override
    public void init() {
        teacherDAO = new TeacherDAO();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            Teacher teacher = new Teacher(new User("walid123","Walid", "Chemat", "walidchemat@gmail.com", "123123", "salt" , false), "Mathematics", "Senior");
            try{
                teacherDAO.insert(teacher);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Teacher inserted with ID: " + teacher.getId());
                jsonResponse.addProperty("teacher", teacher.toString());
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Insertion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                teacher.setDomain("Physics");
                teacherDAO.update(teacher);
                jsonResponse.addProperty("message", "Teacher with ID " + teacher.getId() + " updated.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Update failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                teacherDAO.delete(teacher.getId());
                jsonResponse.addProperty("message", "Teacher with ID " + teacher.getId() + " deleted.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Deletion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            System.out.println("Error during TestUserDAO operations: " + e.getMessage());
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
            jsonResponse.addProperty("message", "POST method in TestTeacherDAO is operational.");
            out.println(jsonResponse.toString());
        } catch (Exception e) {
            System.out.println("Error during TestTeacherDAO POST operation: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }
    }
}
