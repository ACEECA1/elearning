package com.app.dao.implementation;
import com.app.model.users.User;
import com.app.util.Database;
import com.app.dao.interfaces.DAO;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class UserDAO implements DAO<User>{
    @Override
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO user (username, first_name, last_name, email, password_hash, salt) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try{
            conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
            catch(SQLException e){
                System.out.println("Error retrieving generated keys: " + e.getMessage());
            }
        }
        catch(SQLException e){
            System.out.println("Error inserting user: " + e.getMessage());
        }
    }
    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET username = ?, first_name = ?, last_name = ?, email = ?, password_hash = ?, salt = ? WHERE id = ?";
        Connection conn = null;
        try{
            conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            setStatementParameters(pstmt, user);
            pstmt.setInt(7, user.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        }
        catch(SQLException e){
            System.out.println("Error updating user: " + e.getMessage());
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        Connection conn = null;
        try{
            conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        }
        catch(SQLException e){
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        Connection conn = null;
        try{
            conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        }
        catch(SQLException e){
            System.out.println("Error finding user by ID: " + e.getMessage());
            return null;
        }
        return null;
    }
    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM user";
        Connection conn = null;
        List<User> users = new ArrayList<>();
        try{
            conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }
        catch(SQLException e){
            System.out.println("Error finding all users: " + e.getMessage());
        }
        return users;
    }
    public void delete(User user) throws SQLException {
        String sql = "DELETE FROM user WHERE email = ?";
        Connection conn = null;
        try{
            conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getEmail());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting user failed, no rows affected.");
            }
        }
        catch(SQLException e){
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }

    private void setStatementParameters(PreparedStatement pstmt, User user) throws SQLException {
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getFirstName());
        pstmt.setString(3, user.getLastName());
        pstmt.setString(4, user.getEmail());
        pstmt.setString(5, user.getPasswordHash());
        pstmt.setString(6, user.getSalt());
    }
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        String username = rs.getString("username");
        return new User(id,username, firstName, lastName, email, passwordHash, salt);
    }
    
}
