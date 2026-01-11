package com.app.dao.implementation.course;
import com.app.dao.interfaces.DAO;
import com.app.model.course.Module;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
/*
private int id;
    private int courseId;
    private String title;
    private String description;
    private Course course;
*/

public class ModuleDAO implements DAO<Module>{
    public void insert(Connection conn , Module module)throws SQLException{
        String sql = "INSERT INTO module (course_id, title, description, order_index , thumbnail_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, module);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    module.setId(rs.getInt(1));
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error inserting module: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void insert(Module module) {
        try (Connection conn = Database.getConnection()) {
            if(CourseDAO.existsById(conn , module.getCourseId()) == false){
                throw new SQLException("Cannot insert module: Course with ID " + module.getCourseId() + " does not exist.");
            }
            this.insert(conn, module);
        }
        catch(SQLException e){
            System.out.println("Error inserting module: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void update(Connection conn , Module module)throws SQLException{
        String sql = "UPDATE module SET course_id = ?, title = ?, description = ?, order_index = ?, thumbnail_path = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, module);
            ps.setInt(6, module.getId());
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error updating module: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void update(Module module) {
        try (Connection conn = Database.getConnection()) {
            if(CourseDAO.existsById(conn , module.getCourseId()) == false){
                throw new SQLException("Cannot update module: Course with ID " + module.getCourseId() + " does not exist.");
            }
            this.update(conn, module);
        }
        catch(SQLException e){
            System.out.println("Error updating module: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void delete(Connection conn , int id)throws SQLException{
        String sql = "DELETE FROM module WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error deleting module: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error deleting module: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static boolean existsById(Connection conn , int id)throws SQLException{
        String sql = "SELECT 1 FROM module WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public Module findById(Connection conn , int id)throws SQLException{
        String sql = "SELECT * FROM module WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultToModule(rs);
                }
            }
        }
        return null;
    }
    @Override
    public Module findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error finding module by id: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Module> findAll(Connection conn)throws SQLException{
        String sql = "SELECT * FROM module";
        List<Module> modules = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Module module = mapResultToModule(rs);
                    modules.add(module);
                }
            }
        }
        return modules;
    }
    @Override 
    public List<Module> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        }
        catch(SQLException e){
            System.out.println("Error finding all modules: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Module> findByCourseId(Connection conn , int courseId)throws SQLException{
        String sql = "SELECT * FROM module WHERE course_id = ?";
        List<Module> modules = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Module module = mapResultToModule(rs);
                    modules.add(module);
                }
            }
        }
        return modules;
    }

    public List<Module> findByCourseId(int courseId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCourseId(conn, courseId);
        } catch (SQLException e) {
            System.out.println("Error finding modules by course ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setStatementParameters(PreparedStatement ps , Module module)throws SQLException{
        ps.setInt(1, module.getCourseId());
        ps.setString(2, module.getTitle());
        ps.setString(3, module.getDescription());
        ps.setInt(4, module.getOrderIndex());
        if(module.getThumbnailPath() != null){
            ps.setString(5, module.getThumbnailPath());
        }
        else{
            System.out.println("Setting thumbnail_path to NULL for module ID: " + module.getId());
            ps.setNull(5, Types.VARCHAR);
        }
    }
    
    public Module mapResultToModule(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int courseId = rs.getInt("course_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        int orderIndex = rs.getInt("order_index");
        String thumbnailPath = rs.getString("thumbnail_path");
        Module module = new Module(id , courseId , title , description, orderIndex , thumbnailPath);
        return module;
    }
}
