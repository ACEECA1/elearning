package com.app.dao.implementation.course;
import com.app.dao.interfaces.DAO;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class CourseDAO implements DAO<Course>{
    public void insert(Connection conn , Course course)throws SQLException{
        String sql = "INSERT INTO course (teacher_id, title, target_audience, description, enrollment_key, thumbnail_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, course);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    course.setId(rs.getInt(1));
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error inserting course: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void insert(Course course) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.insert(conn, course);
        }
        catch(SQLException e){
            System.out.println("Error inserting course: " + e.getMessage());
            throw e;
        }
    }
    public void update(Connection conn , Course course)throws SQLException{
        String sql = "UPDATE course SET teacher_id = ?, title = ?, target_audience = ?, description = ?, enrollment_key = ?, thumbnail_path = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, course);
            ps.setInt(7, course.getId());
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error updating course: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void update(Course course) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, course);
        }
        catch(SQLException e){
            System.out.println("Error updating course: " + e.getMessage());
            throw e;
        }
    }
    public void delete(Connection conn , int id)throws SQLException{
        String sql = "DELETE FROM course WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error deleting course: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error deleting course: " + e.getMessage());
            throw e;
        }
    }
    public Course findById(Connection conn , int id)throws SQLException{
        String sql = "SELECT * FROM course WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error finding course by id: " + e.getMessage());
            throw e;
        }
        return null;
    }
    @Override
    public Course findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error finding course by id: " + e.getMessage());
            throw e;
        }
    }

    public List<Course> findAll(Connection conn) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all courses: " + e.getMessage());
            throw e;
        }
        return courses;
    }
    @Override
    public List<Course> findAll() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all courses: " + e.getMessage());
            throw e;
        }
    }
    public static boolean existsById(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM course WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking existence of course by id: " + e.getMessage());
            throw e;
        }
    }
    public static boolean existsById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return existsById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error checking existence of course by id: " + e.getMessage());
            throw e;
        }
    }
    public List<Course> findByTeacherId(Connection conn, int teacherId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE teacher_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding courses by teacher id: " + e.getMessage());
            throw e;
        }
        return courses;
    }

    public List<Course> searchCourses(Connection conn, String query) throws SQLException {
        String sql = "SELECT * FROM course WHERE title LIKE ? OR description LIKE ?";
        List<Course> courses = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }
        return courses;
    }

    public void setStatementParameters(PreparedStatement ps, Course course) throws SQLException {
        ps.setInt(1, course.getTeacherId());
        ps.setString(2, course.getTitle());
        ps.setString(3, course.getTargetAudience());
        ps.setString(4, course.getDescription());
        ps.setString(5, course.getEnrollmentKey());
        ps.setString(6, course.getThumbnailPath());
    }
    public Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int teacherId = rs.getInt("teacher_id");
        String title = rs.getString("title");
        String targetAudience = rs.getString("target_audience");
        String description = rs.getString("description");
        String enrollmentKey = rs.getString("enrollment_key");
        String thumbnailPath = rs.getString("thumbnail_path");

        return new Course(id, teacherId, title, targetAudience, description, enrollmentKey, thumbnailPath);
    }
    public int countCourses(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM course";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error counting courses: " + e.getMessage());
            throw e;
        }
        return 0;
    }
} 
