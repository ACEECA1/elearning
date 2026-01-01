package com.app.dao.implementation.users;
import com.app.dao.interfaces.DAO;
import com.app.model.users.Admin;
import com.app.model.users.User;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class AdminDAO implements DAO<Admin> {
    void insert(Connection conn, Admin admin)  throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.insert(conn, admin);
        String sql = "INSERT INTO admin (id) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, admin.getId());
            pstmt.executeUpdate();
        }
    }
    @Override
    public void insert(Admin admin) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.insert(conn, admin);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error inserting admin: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public void update(Connection conn, Admin admin) throws SQLException {
        UserDAO userDAO = new UserDAO();
        userDAO.update(conn, admin);
    }
    @Override
    public void update(Admin admin) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.update(conn, admin);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error updating admin: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM admin WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting admin failed, no rows affected.");
            }
        }
        UserDAO userDAO = new UserDAO();
        userDAO.delete(conn, id);
    }
    @Override
    public void delete(int id) throws SQLException {
        Connection conn = null;
        try  {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            this.delete(conn, id);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Error deleting admin: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }
    public Admin findById(Connection conn, int id) throws SQLException {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findById(conn, id);
        if (user == null) {
            return null;
        }
        String sql = "SELECT * FROM admin WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(user);
                }
            }
        }
        return null;
    }
    @Override
    public Admin findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.err.println("Error finding admin by ID: " + e.getMessage());
            throw e;
        }
    }
    public List<Admin> findAll(Connection conn) throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT u.* FROM user u JOIN admin a ON u.id = a.id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = UserDAO.mapRowToUser(rs);
                admins.add(new Admin(user));
            }
        }
        return admins;
    }
    @Override
    public List<Admin> findAll() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Error finding all admins: " + e.getMessage());
            throw e;
        }
    }
    
}
