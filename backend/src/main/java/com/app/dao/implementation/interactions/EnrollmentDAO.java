package com.app.dao.implementation.interactions;

import com.app.model.interactions.Enrollment;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public void insert(Connection conn, Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollment (student_id, course_id) VALUES (?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getCourseId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting enrollment: " + e.getMessage());
            throw e;
        }
    }

    public void insert(Enrollment enrollment) {
        try (Connection conn = Database.getConnection()) {
            if (!studentExists(conn, enrollment.getStudentId())) {
                throw new SQLException("Cannot enroll: Student with ID " + enrollment.getStudentId() + " does not exist.");
            }
            if (!courseExists(conn, enrollment.getCourseId())) {
                throw new SQLException("Cannot enroll: Course with ID " + enrollment.getCourseId() + " does not exist.");
            }
            this.insert(conn, enrollment);
        } catch (SQLException e) {
            System.out.println("Error inserting enrollment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollment WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting enrollment: " + e.getMessage());
            throw e;
        }
    }

    public void delete(int studentId, int courseId) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, studentId, courseId);
        } catch (SQLException e) {
            System.out.println("Error deleting enrollment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Enrollment findByCompositeId(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "SELECT * FROM enrollment WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEnrollment(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding enrollment: " + e.getMessage());
            throw e;
        }
    }

    public Enrollment findByCompositeId(int studentId, int courseId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCompositeId(conn, studentId, courseId);
        } catch (SQLException e) {
            System.out.println("Error finding enrollment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Enrollment> findByStudentId(Connection conn, int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollment WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding enrollments by student ID: " + e.getMessage());
            throw e;
        }
        return enrollments;
    }

    public List<Enrollment> findByStudentId(int studentId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByStudentId(conn, studentId);
        } catch (SQLException e) {
            System.out.println("Error finding enrollments by student ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Enrollment> findByCourseId(Connection conn, int courseId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollment WHERE course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding enrollments by course ID: " + e.getMessage());
            throw e;
        }
        return enrollments;
    }

    public List<Enrollment> findByCourseId(int courseId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCourseId(conn, courseId);
        } catch (SQLException e) {
            System.out.println("Error finding enrollments by course ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    public static boolean isEnrolled(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "SELECT 1 FROM enrollment WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    public static boolean isEnrolled(int studentId, int courseId) {
        try (Connection conn = Database.getConnection()) {
            return isEnrolled(conn, studentId, courseId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        int studentId = rs.getInt("student_id");
        int courseId = rs.getInt("course_id");
        Timestamp enrollmentDate = rs.getTimestamp("enrollment_date");
        
        return new Enrollment(studentId, courseId, enrollmentDate);
    }
    
    private boolean studentExists(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM student WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean courseExists(Connection conn, int courseId) throws SQLException {
        String sql = "SELECT 1 FROM course WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}