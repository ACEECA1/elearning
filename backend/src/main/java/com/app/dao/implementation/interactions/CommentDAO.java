package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Comment;
import com.app.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO implements DAO<Comment> {

    public void insert(Connection conn, Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (user_id, forum_id, content, is_reply, parent_comment_id) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comment.getUserId());
            ps.setInt(2, comment.getForumId());
            ps.setString(3, comment.getContent());
            ps.setBoolean(4, comment.isReply());

            // Handle Nullable Parent ID
            if (comment.getParentCommentId() > 0) {
                ps.setInt(5, comment.getParentCommentId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting comment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Comment comment) {
        try (Connection conn = Database.getConnection()) {
            if (!forumExists(conn, comment.getForumId())) {
                throw new SQLException("Cannot insert comment: Forum with ID " + comment.getForumId() + " does not exist.");
            }
            this.insert(conn, comment);
        } catch (SQLException e) {
            System.out.println("Error inserting comment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Comment comment) throws SQLException {
        String sql = "UPDATE comment SET user_id = ?, forum_id = ?, content = ?, likes = ?, dislikes = ?, is_modified = ?, is_reply = ?, parent_comment_id = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comment.getUserId());
            ps.setInt(2, comment.getForumId());
            ps.setString(3, comment.getContent());
            ps.setInt(4, comment.getLikes());
            ps.setInt(5, comment.getDislikes());
            ps.setBoolean(6, comment.isModified());
            ps.setBoolean(7, comment.isReply());

            if (comment.getParentCommentId() > 0) {
                ps.setInt(8, comment.getParentCommentId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setInt(9, comment.getId()); // WHERE id = ?
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating comment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Comment comment) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, comment);
        } catch (SQLException e) {
            System.out.println("Error updating comment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting comment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting comment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Comment findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM comment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding comment by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Comment findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding comment by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Comment> findAll(Connection conn) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all comments: " + e.getMessage());
            throw e;
        }
        return comments;
    }

    @Override
    public List<Comment> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all comments: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Comment> findByForumId(Connection conn, int forumId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE forum_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, forumId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding comments by forum ID: " + e.getMessage());
            throw e;
        }
        return comments;
    }

    public List<Comment> findByForumId(int forumId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByForumId(conn, forumId);
        } catch (SQLException e) {
            System.out.println("Error finding comments by forum ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        int forumId = rs.getInt("forum_id");
        String content = rs.getString("content");
        String createdAt = rs.getString("created_at");
        int likes = rs.getInt("likes");
        int dislikes = rs.getInt("dislikes");
        boolean isModified = rs.getBoolean("is_modified");
        boolean isReply = rs.getBoolean("is_reply");
        
        int parentCommentId = rs.getInt("parent_comment_id"); 
        if (rs.wasNull()) {
            parentCommentId = -1;
        }

        if (isReply) {
            return new Comment(content, createdAt, likes, dislikes, isModified, forumId, userId, isReply, parentCommentId);
        } else {
            return new Comment(id, content, createdAt, likes, dislikes, isModified, forumId, userId);
        }
    }

    private boolean forumExists(Connection conn, int forumId) throws SQLException {
        String sql = "SELECT 1 FROM forum WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, forumId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}