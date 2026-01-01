package com.app.dao.implementation;
import com.app.dao.interfaces.DAO;
import com.app.model.users.Student;
import com.app.model.users.User;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/*    String studentCardNumber;
    String academicYear;
     */

public class StudentDAO implements DAO<Student> {
    @Override
    public void insert(Student student) throws SQLException {
    }

    @Override
    public void update(Student student) throws SQLException {
    }

    @Override
    public void delete(int id) throws SQLException {
    }

    @Override
    public Student findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Student> findAll() throws SQLException {
        return null;
    }

    private void setStatementParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setInt(1, student.getId());
        pstmt.setString(2, student.getStudentCardNumber());
        pstmt.setString(3, student.getAcademicYear());
    }
    
}
