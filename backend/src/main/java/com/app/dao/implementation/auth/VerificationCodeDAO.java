package com.app.dao.implementation.auth;

import com.app.util.Database;
import java.sql.*;
import java.time.LocalDateTime;

public class VerificationCodeDAO {

    public void save(String email, String code, int minutesToLive) throws SQLException {
        String sql = "INSERT INTO verification_code (email, code, expires_at) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE code = ?, expires_at = ?";
        
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutesToLive);
        Timestamp expiryTimestamp = Timestamp.valueOf(expiresAt);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            ps.setString(2, code);
            ps.setTimestamp(3, expiryTimestamp);
            
            ps.setString(4, code);
            ps.setTimestamp(5, expiryTimestamp);
            
            ps.executeUpdate();
        }
    }

    public String getValidCode(String email) throws SQLException {
        String sql = "SELECT code FROM verification_code WHERE email = ? AND expires_at > NOW()";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("code");
                }
            }
        }
        return null;
    }

    public void delete(String email) {
        String sql = "DELETE FROM verification_code WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting used code: " + e.getMessage());
        }
    }
}