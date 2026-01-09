package com.app.dao.implementation.course;

import com.app.dao.interfaces.DAO;
import com.app.model.course.Material;
import com.app.util.Database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class MaterialDAO implements DAO<Material> {

    public void insert(Connection conn, Material material) throws SQLException {
        String sql = "INSERT INTO material (title, path, type, chapter_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, material);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    material.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting material: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Material material) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.insert(conn, material);
        } catch (SQLException e) {
            System.out.println("Error inserting material: " + e.getMessage());
            throw e;
        }
    }

    public void update(Connection conn, Material material) throws SQLException {
        String sql = "UPDATE material SET title = ?, path = ?, type = ?, chapter_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, material);
            ps.setInt(5, material.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating material: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Material material) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, material);
        } catch (SQLException e) {
            System.out.println("Error updating material: " + e.getMessage());
            throw e;
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM material WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting material: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting material: " + e.getMessage());
            throw e;
        }
    }

    public Material findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM material WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToObject(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding material by id: " + e.getMessage());
            throw e;
        }
        return null;
    }

    @Override
    public Material findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding material by id: " + e.getMessage());
            throw e;
        }
    }

    public List<Material> findAll(Connection conn) throws SQLException {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT * FROM material";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                materials.add(mapResultSetToObject(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all materials: " + e.getMessage());
            throw e;
        }
        return materials;
    }

    @Override
    public List<Material> findAll() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all materials: " + e.getMessage());
            throw e;
        }
    }

    public List<Material> findByChapterId(Connection conn, int chapterId) throws SQLException {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT * FROM material WHERE chapter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    materials.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding materials by chapter id: " + e.getMessage());
            throw e;
        }
        return materials;
    }
    public List<Material> findByChapterId(int chapterId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            return this.findByChapterId(conn, chapterId);
        } catch (SQLException e) {
            System.out.println("Error finding materials by chapter id: " + e.getMessage());
            throw e;
        }
    }

    public static boolean existsById(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM material WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking existence of material by id: " + e.getMessage());
            throw e;
        }
    }

    public void setStatementParameters(PreparedStatement ps, Material material) throws SQLException {
        ps.setString(1, material.getTitle());
        ps.setString(2, material.getPath());
        ps.setString(3, material.getType());
        ps.setInt(4, material.getChapterId());
    }

    public Material mapResultSetToObject(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String path = rs.getString("path");
        String type = rs.getString("type");
        int chapterId = rs.getInt("chapter_id");

        return new Material(id, title, path, type, chapterId);
    }
}