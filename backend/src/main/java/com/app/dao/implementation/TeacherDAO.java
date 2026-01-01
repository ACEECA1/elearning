package com.app.dao.implementation;
import com.app.dao.interfaces.DAO;
import com.app.model.users.Teacher;
import com.app.model.users.User;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
/*
private String domain;
    private String grade;
*/
public class TeacherDAO implements DAO<Teacher> {
    void insert(Connection conn, Teacher teacher) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.insert(conn, teacher);
        String sql = "INSERT INTO teacher (id, domain, grade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, teacher);
            pstmt.executeUpdate();
        }
    }
    @Override
    public void insert(Teacher teacher) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.insert(conn, teacher);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error inserting teacher: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    @Override
    public void update(Teacher teacher) throws SQLException {
    }
    @Override
    public void delete(int id) throws SQLException {
    }
    @Override
    public Teacher findById(int id) throws SQLException {
        return null;
    }
    @Override
    public List<Teacher> findAll() throws SQLException {
        return null;
    }


    private void setStatementParameters(PreparedStatement pstmt, Teacher teacher) throws SQLException {
        pstmt.setInt(1, teacher.getId());
        pstmt.setString(2, teacher.getDomain());
        pstmt.setString(3, teacher.getGrade());
    }
    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        User user = UserDAO.mapRowToUser(rs);
        String domain = rs.getString("domain");
        String grade = rs.getString("grade");

        return new Teacher(user, domain, grade);
    }
}