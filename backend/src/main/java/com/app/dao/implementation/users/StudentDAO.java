package com.app.dao.implementation.users;
import com.app.dao.interfaces.DAO;
import com.app.model.users.Student;
import com.app.model.users.User;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class StudentDAO implements DAO<Student> {
    void insert(Connection conn, Student student) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.insert(conn, student);
        String sql = "INSERT INTO student (id, student_card_number, academic_year) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, student);
            pstmt.executeUpdate();
        }
        catch(SQLException e){
            System.err.println("Error inserting student details: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void insert(Student student) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.insert(conn, student);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error inserting student: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public void update(Connection conn, Student student) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.update(conn, student);
        String sql = "UPDATE student SET student_card_number = ?, academic_year = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getStudentCardNumber());
            pstmt.setString(2, student.getAcademicYear());
            pstmt.setInt(3, student.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating student failed, no rows affected.");
            }
        }
        catch(SQLException e){
            System.err.println("Error updating student details: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void update(Student student) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.update(conn, student);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error updating student: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public void delete(Connection conn, int id) throws SQLException {
        UserDAO userDAO = new UserDAO();
        String sql = "DELETE FROM student WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting student failed, no rows affected.");
            }
            userDAO.delete(conn, id);
        }
        catch(SQLException e){
            System.err.println("Error deleting student details: " + e.getMessage());
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
            System.err.println("Error deleting student: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }

    public Student findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT u.*, s.student_card_number, s.academic_year FROM user u JOIN student s ON u.id = s.id WHERE u.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }
    @Override
    public Student findById(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            return this.findById(conn, id);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public List<Student> findAll(Connection conn) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT u.*, s.student_card_number, s.academic_year FROM user u JOIN student s ON u.id = s.id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Student student = mapResultSetToStudent(rs);
                students.add(student);
            }
        }
        return students;
    }
    @Override
    public List<Student> findAll() throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            return this.findAll(conn);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void setStatementParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setInt(1, student.getId());
        pstmt.setString(2, student.getStudentCardNumber());
        pstmt.setString(3, student.getAcademicYear());
    }
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        User user = UserDAO.mapRowToUser(rs);
        String studentCardNumber = rs.getString("student_card_number");
        String academicYear = rs.getString("academic_year");
        return new Student(user, studentCardNumber, academicYear);
    }
    
}
