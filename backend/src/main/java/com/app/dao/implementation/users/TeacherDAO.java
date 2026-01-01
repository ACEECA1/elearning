package com.app.dao.implementation.users;
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
    public void update(Connection conn, Teacher teacher) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.update(conn, teacher);
        String sql = "UPDATE teacher SET domain = ?, grade = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getDomain());
            pstmt.setString(2, teacher.getGrade());
            pstmt.setInt(3, teacher.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating teacher failed, no rows affected.");
            }
        }
    }
    @Override
    public void update(Teacher teacher) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.update(conn, teacher);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error updating teacher: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public void delete(Connection conn, int id) throws SQLException {
        UserDAO userDAO = new UserDAO();
        String sql = "DELETE FROM teacher WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting teacher failed, no rows affected.");
            }
            try{
                userDAO.delete(conn, id);
            }
            catch(SQLException e){
                throw new SQLException("Error deleting associated user: " + e.getMessage());
            }
        }
        catch(SQLException e){
            System.err.println("Error deleting teacher: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.delete(conn, id);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error deleting teacher: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public Teacher findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT u.*, t.* FROM user u JOIN teacher t ON u.id = t.id WHERE u.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTeacher(rs);
                }
            }
        }
        return null;
    }
    @Override
    public Teacher findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.err.println("Error finding teacher by ID: " + e.getMessage());
            throw e;
        }
    }
    public List<Teacher> findAll(Connection conn) throws SQLException {
        String sql = "SELECT u.*, t.* FROM user u JOIN teacher t ON u.id = t.id";
        List<Teacher> teachers = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                teachers.add(mapResultSetToTeacher(rs));
            }
        }
        return teachers;
    }
    @Override
    public List<Teacher> findAll() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Error finding all teachers: " + e.getMessage());
            throw e;
        }
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