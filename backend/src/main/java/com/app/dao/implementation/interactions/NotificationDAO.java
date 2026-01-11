package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Notification;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO implements DAO<Notification> {

    public void insert(Connection conn, Notification notification) throws SQLException {
        String sql = "INSERT INTO notification (user_id, title, message, type, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, notification);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Notification notification) {
        try (Connection conn = Database.getConnection()) {
            if (!userExists(conn, notification.getUserId())) {
                throw new SQLException("Cannot insert notification: User with ID " + notification.getUserId() + " does not exist.");
            }
            this.insert(conn, notification);
        } catch (SQLException e) {
            System.out.println("Error inserting notification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Notification notification) throws SQLException {
        String sql = "UPDATE notification SET user_id = ?, title = ?, message = ?, type = ?, is_read = ?, created_at = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, notification);
            ps.setInt(7, notification.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Notification notification) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, notification);
        } catch (SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM notification WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Notification findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToNotification(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding notification by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Notification findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding notification by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Notification> findAll(Connection conn) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all notifications: " + e.getMessage());
            throw e;
        }
        return notifications;
    }

    @Override
    public List<Notification> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all notifications: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Notification> findByUserId(Connection conn, int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding notifications by user ID: " + e.getMessage());
            throw e;
        }
        return notifications;
    }

    public List<Notification> findByUserId(int userId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByUserId(conn, userId);
        } catch (SQLException e) {
            System.out.println("Error finding notifications by user ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void markAsRead(Connection conn, int id, int userId) throws SQLException {
        String sql = "UPDATE notification SET is_read = TRUE WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error marking notification as read: " + e.getMessage());
            throw e;
        }
    }

    public void markAllAsRead(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE notification SET is_read = TRUE WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error marking all notifications as read: " + e.getMessage());
            throw e;
        }
    }

    private void setStatementParameters(PreparedStatement ps, Notification notification) throws SQLException {
        ps.setInt(1, notification.getUserId());
        ps.setString(2, notification.getTitle());
        ps.setString(3, notification.getMessage());
        ps.setString(4, notification.getType());
        ps.setBoolean(5, notification.isRead());
        
        if (notification.getCreatedAt() != null) {
            ps.setTimestamp(6, notification.getCreatedAt());
        } else {
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String title = rs.getString("title");
        String message = rs.getString("message");
        String type = rs.getString("type");
        boolean isRead = rs.getBoolean("is_read");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Notification(id, userId, title, message, type, isRead, createdAt);
    }

    private boolean userExists(Connection conn, int userId) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}