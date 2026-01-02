package com.app.dao.implementation.course;
import com.app.dao.interfaces.DAO;
import com.app.model.course.Chapter;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/*
private int id;
    private int moduleId;
    private String title;
    private String content;
    private Module module;
*/
public class ChapterDAO implements DAO<Chapter> {
    public void insert(Connection conn , Chapter chapter)throws SQLException{
        String sql = "INSERT INTO chapter (module_id, title, content , order_index) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, chapter);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    chapter.setId(rs.getInt(1));
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error inserting chapter: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void insert(Chapter chapter) {
        try (Connection conn = Database.getConnection()) {
            if(ModuleDAO.existsById(conn , chapter.getModuleId()) == false){
                throw new SQLException("Cannot insert chapter: Module with ID " + chapter.getModuleId() + " does not exist.");
            }
            this.insert(conn, chapter);
        }
        catch(SQLException e){
            System.out.println("Error inserting chapter: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void update(Connection conn , Chapter chapter)throws SQLException{
        String sql = "UPDATE chapter SET module_id = ?, title = ?, content = ?, order_index = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, chapter);
            ps.setInt(5, chapter.getId());
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error updating chapter: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void update(Chapter chapter) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, chapter);
        }
        catch(SQLException e){
            System.out.println("Error updating chapter: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void delete(Connection conn , int id)throws SQLException{
        String sql = "DELETE FROM chapter WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        catch(SQLException e){
            System.out.println("Error deleting chapter: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error deleting chapter: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    //Chapter id
    public Chapter findById(Connection conn , int id)throws SQLException{
        String sql = "SELECT * FROM chapter WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChapter(rs);
                } else {
                    return null;
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error finding chapter by ID: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public Chapter findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        }
        catch(SQLException e){
            System.out.println("Error finding chapter by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Chapter> findAll(Connection conn)throws SQLException{
        List<Chapter> chapters = new ArrayList<>();
        String sql = "SELECT * FROM chapter";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                chapters.add(mapResultSetToChapter(rs));
            }
        }
        catch(SQLException e){
            System.out.println("Error finding all chapters: " + e.getMessage());
            throw e;
        }
        return chapters;
    }
    @Override
    public List<Chapter> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        }
        catch(SQLException e){
            System.out.println("Error finding all chapters: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Chapter> findByModuleId(Connection conn , int moduleId)throws SQLException{
        List<Chapter> chapters = new ArrayList<>();
        String sql = "SELECT * FROM chapter WHERE module_id = ? ORDER BY order_index ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chapters.add(mapResultSetToChapter(rs));
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error finding chapters by module ID: " + e.getMessage());
            throw e;
        }
        return chapters;
    }
    public List<Chapter> findByModuleId(int moduleId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByModuleId(conn, moduleId);
        }
        catch(SQLException e){
            System.out.println("Error finding chapters by module ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Chapter> findByCourseId(Connection conn , int courseId)throws SQLException{
        String sql = "SELECT ch.* FROM chapter ch " +
                     "JOIN module m ON ch.module_id = m.id " +
                     "WHERE m.course_id = ? " +
                     "ORDER BY m.order_index ASC, ch.order_index ASC";
        List<Chapter> chapters = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chapter chapter = mapResultSetToChapter(rs);
                    chapters.add(chapter);
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error finding chapters by course ID: " + e.getMessage());
            throw e;
        }
        return chapters;
    }


    public List<Chapter> findByCourseId(int courseId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCourseId(conn, courseId);
        }
        catch(SQLException e){
            System.out.println("Error finding chapters by course ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public void setStatementParameters(PreparedStatement ps, Chapter chapter) throws SQLException {
        ps.setInt(1, chapter.getModuleId());
        ps.setString(2, chapter.getTitle());
        ps.setString(3, chapter.getContent());
        ps.setInt(4, chapter.getOrderIndex());
    }
    public Chapter mapResultSetToChapter(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int moduleId = rs.getInt("module_id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        int orderIndex = rs.getInt("order_index");
        return new Chapter(id, moduleId, title, content , orderIndex);
    }
}
