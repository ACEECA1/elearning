package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Forum;
import com.app.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
    
public class ForumDAO implements DAO<Forum> {

    public void insert(Connection conn, Forum forum) throws SQLException {
        String sql = "INSERT INTO forum (chapter_id, title) VALUES (?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, forum);
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    forum.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void insert(Forum forum) {
        try (Connection conn = Database.getConnection()) {
            this.insert(conn, forum);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting forum: " + e.getMessage(), e);
        }
    }

    public void update(Connection conn, Forum forum) throws SQLException {
        String sql = "UPDATE forum SET chapter_id = ?, title = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, forum);
            ps.setInt(3, forum.getId()); // ID is now the 3rd parameter
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Forum forum) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, forum);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating forum: " + e.getMessage(), e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM forum WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting forum: " + e.getMessage(), e);
        }
    }

    public Forum findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM forum WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToForum(rs) : null;
            }
        }
    }

    @Override
    public Forum findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding forum: " + e.getMessage(), e);
        }
    }

    public List<Forum> findAll(Connection conn) throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String sql = "SELECT * FROM forum";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) forums.add(mapResultSetToForum(rs));
        }
        return forums;
    }

    @Override
    public List<Forum> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all forums: " + e.getMessage(), e);
        }
    }

    public List<Forum> findByChapterId(Connection conn, int chapterId) throws SQLException {
        List<Forum> forums = new ArrayList<>();
        String sql = "SELECT * FROM forum WHERE chapter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    forums.add(mapResultSetToForum(rs));
                }
            }
        }
        return forums;
    }

    public List<Forum> findByChapterId(int chapterId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByChapterId(conn, chapterId);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding forums by chapter ID: " + e.getMessage(), e);
        }
    }

    
    public void setStatementParameters(PreparedStatement ps, Forum forum) throws SQLException {
        ps.setInt(1, forum.getChapterId());
        ps.setString(2, forum.getTitle());
    }

    public Forum mapResultSetToForum(ResultSet rs) throws SQLException {
        return new Forum(
            rs.getInt("id"),
            rs.getInt("chapter_id"),
            rs.getString("title"),
            rs.getTimestamp("created_at") 
        );
    }
}