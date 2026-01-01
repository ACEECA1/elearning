package com.app.controller.Test;

import com.app.dao.implementation.*;
import com.app.model.users.*;
import com.google.gson.JsonObject;

import java.io.PrintWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
@WebServlet("/testStudentDAO")
public class TestStudentDAO extends HttpServlet {
    public StudentDAO studentDAO;
    @Override
    public void init() {
        studentDAO = new StudentDAO();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        PrintWriter out;
        JsonObject jsonResponse = new JsonObject();
        try {
            resp.setContentType("application/json");
            out = resp.getWriter();
            Student student = new Student(new User("walid123","Walid", "Chemat", "walidchemat@gmail.com", "123123", "salt" , false), "SCN123", "2023");
            try{
                studentDAO.insert(student);
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Student inserted with ID: " + student.getId());
                jsonResponse.addProperty("student", student.toString());
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Insertion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                student.setAcademicYear("2024");
                studentDAO.update(student);
                jsonResponse.addProperty("message", "Student with ID " + student.getId() + " updated.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Update failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
            try{
                studentDAO.delete(student.getId());
                jsonResponse.addProperty("message", "Student with ID " + student.getId() + " deleted.");
                out.println(jsonResponse.toString());
            }
            catch (Exception e){
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Deletion failed: " + e.getMessage());
                out.println(jsonResponse.toString());
            }
        } catch (Exception e) {
            System.out.println("Error during TestStudentDAO operations: " + e.getMessage());
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
            jsonResponse.addProperty("message", "POST method in TestStudentDAO is operational.");
            out.println(jsonResponse.toString());
        } catch (Exception e) {
            System.out.println("Error during TestStudentDAO POST operation: " + e.getMessage());
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
        }
    }
}
