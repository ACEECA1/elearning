package com.app.dao.implementation.users;
import com.app.model.users.User;
import com.app.util.Database;
import com.app.dao.interfaces.DAO;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class UserDAO implements DAO<User>{
    public void insert(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO user (username, first_name, last_name, email, password_hash, salt , profile_picture_path, is_verified) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(pstmt, user);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserting user failed, no ID obtained.");
                }
            }
        }
    }
    @Override
    public void insert(User user) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.insert(conn, user);
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            throw e; 
        }
    }
    public void update(Connection conn, User user) throws SQLException {
        String sql = "UPDATE user SET username = ?, first_name = ?, last_name = ?, email = ?, password_hash = ?, salt = ? , profile_picture_path = ?, is_verified = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, user);
            pstmt.setInt(9, user.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        }
    }
    @Override
    public void update(User user) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, user);
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e; 
        }
    }
    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw e; 
        }
    }
    public User findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }
    @Override
    public User findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            throw e;
        }
    }
    public List<User> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM user";
        List<User> users = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }
        return users;
    }
    @Override
    public List<User> findAll() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
            throw e;
        }
    }
    public void delete(Connection conn, User user) throws SQLException {
        String sql = "DELETE FROM user WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getEmail());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        }
    }
    public void delete(User user) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, user);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw e; 
        }
    }
    public int count(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM user";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
    public int count() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return count(conn);
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
            throw e;
        }
    }

    
    private void setStatementParameters(PreparedStatement pstmt, User user) throws SQLException {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getFirstName());
        pstmt.setString(3, user.getLastName());
        pstmt.setString(4, user.getEmail());
        pstmt.setString(5, user.getPasswordHash());
        pstmt.setString(6, user.getSalt());
        pstmt.setString(7, user.getProfilePicturePath());
        pstmt.setBoolean(8, user.isVerified());
    }
    public static User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        String username = rs.getString("username");
        String profilePicturePath = rs.getString("profile_picture_path");
        boolean isVerified = rs.getBoolean("is_verified");
        return new User(id,username, firstName, lastName, email, passwordHash, salt,profilePicturePath, isVerified);
    }
    

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        catch(Exception e){
            System.out.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }
    public User findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }


    public User findByEmail(String email) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return findByEmail(conn, email);
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            throw e;
        }
    }
    public String getUserEmailById(Connection conn, int userId) throws SQLException {
        String sql = "SELECT email FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }
    public String getUserEmailById(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return getUserEmailById(conn, userId);
        } catch (SQLException e) {
            System.err.println("Error retrieving user email by ID: " + e.getMessage());
            throw e;
        }
    }
}
