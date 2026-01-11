package com.app.service;

import com.app.dao.implementation.interactions.NotificationDAO;
import com.app.dao.implementation.users.UserDAO;
import com.app.model.interactions.Notification;
import com.app.util.Database;
import com.app.util.EmailService;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();
    public void createNotification(Notification notification) throws Exception {
        try (Connection conn = Database.getConnection()) {
            notificationDAO.insert(conn, notification);
        } catch (SQLException e) {
            System.out.println("Error creating notification: " + e.getMessage());
            throw new Exception("Error creating notification");
        }
    }

    public void sendNotification(int userId, String title, String message, String type) {
        try {
            Notification notification = new Notification(userId, title, message, type);
            String email = userDAO.getUserEmailById(userId);
            if (email != null && !email.isEmpty()) {
                EmailService.sendNotificationEmail(email, title, message);
            }
            createNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send notification to user " + userId + ": " + e.getMessage());
        }
    }

    public List<Notification> getUserNotifications(int userId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return notificationDAO.findByUserId(conn, userId);
        } catch (SQLException e) {
            throw new Exception("Error retrieving notifications");
        }
    }

    public void markAsRead(int notificationId, int userId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            // Verify ownership or existence if necessary
            Notification notification = notificationDAO.findById(conn, notificationId);
            if (notification == null) {
                throw new Exception("Notification not found");
            }
            if (notification.getUserId() != userId) {
                throw new Exception("Unauthorized to modify this notification");
            }
            
            notificationDAO.markAsRead(conn, notificationId, userId);
        } catch (SQLException e) {
            throw new Exception("Error marking notification as read");
        }
    }

    public void markAllAsRead(int userId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            notificationDAO.markAllAsRead(conn, userId);
        } catch (SQLException e) {
            throw new Exception("Error marking all notifications as read");
        }
    }
    
    public void deleteNotification(int notificationId, int userId) throws Exception {
        try (Connection conn = Database.getConnection()) {
             Notification notification = notificationDAO.findById(conn, notificationId);
             if (notification == null) {
                 throw new Exception("Notification not found");
             }
             if (notification.getUserId() != userId) {
                 throw new Exception("Unauthorized to delete this notification");
             }
             notificationDAO.delete(conn, notificationId);
        } catch (SQLException e) {
            throw new Exception("Error deleting notification");
        }
    }
}