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
                    return mapResultSetToObject(rs);
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
                courses.add(mapResultSetToObject(rs));
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

    public void setStatementParameters(PreparedStatement ps, Course course) throws SQLException {
        ps.setInt(1, course.getTeacherId());
        ps.setString(2, course.getTitle());
        ps.setString(3, course.getTargetAudience());
        ps.setString(4, course.getDescription());
        ps.setString(5, course.getEnrollmentKey());
        ps.setString(6, course.getThumbnailPath());
    }
    public Course mapResultSetToObject(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int teacherId = rs.getInt("teacher_id");
        String title = rs.getString("title");
        String targetAudience = rs.getString("target_audience");
        String description = rs.getString("description");
        String enrollmentKey = rs.getString("enrollment_key");
        String thumbnailPath = rs.getString("thumbnail_path");

        return new Course(id, teacherId, title, targetAudience, description, enrollmentKey, thumbnailPath);
    }
} 
