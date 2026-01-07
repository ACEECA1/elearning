package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Note;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO implements DAO<Note> {

    public void insert(Connection conn, Note note) throws SQLException {
        String sql = "INSERT INTO note (student_id, quiz_id, grade) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, note);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting note: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Note note) {
        try (Connection conn = Database.getConnection()) {
            if (!studentExists(conn, note.getStudentId())) {
                throw new SQLException("Cannot insert note: Student with ID " + note.getStudentId() + " does not exist.");
            }
            if (!quizExists(conn, note.getQuizId())) {
                 throw new SQLException("Cannot insert note: Quiz with ID " + note.getQuizId() + " does not exist.");
            }
            this.insert(conn, note);
        } catch (SQLException e) {
            System.out.println("Error inserting note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Note note) throws SQLException {
        String sql = "UPDATE note SET student_id = ?, quiz_id = ?, grade = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, note);
            ps.setInt(4, note.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating note: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Note note) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, note);
        } catch (SQLException e) {
            System.out.println("Error updating note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM note WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting note: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Note findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM note WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToNote(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding note by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Note findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding note by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Note> findAll(Connection conn) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM note";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all notes: " + e.getMessage());
            throw e;
        }
        return notes;
    }

    @Override
    public List<Note> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all notes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Note> findByStudentId(Connection conn, int studentId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM note WHERE student_id = ? ORDER BY date_recorded DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding notes by student ID: " + e.getMessage());
            throw e;
        }
        return notes;
    }

    public List<Note> findByStudentId(int studentId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByStudentId(conn, studentId);
        } catch (SQLException e) {
            System.out.println("Error finding notes by student ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Note> findByQuizId(Connection conn, int quizId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM note WHERE quiz_id = ? ORDER BY grade DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding notes by quiz ID: " + e.getMessage());
            throw e;
        }
        return notes;
    }

    public List<Note> findByQuizId(int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByQuizId(conn, quizId);
        } catch (SQLException e) {
            System.out.println("Error finding notes by quiz ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setStatementParameters(PreparedStatement ps, Note note) throws SQLException {
        ps.setInt(1, note.getStudentId());
        ps.setInt(2, note.getQuizId());
        ps.setDouble(3, note.getGrade());
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int studentId = rs.getInt("student_id");
        int quizId = rs.getInt("quiz_id");
        double grade = rs.getDouble("grade");
        java.util.Date dateRecorded = new java.util.Date(rs.getTimestamp("date_recorded").getTime());

        return new Note(id, studentId, quizId, grade, dateRecorded);
    }

    private boolean studentExists(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM student WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private boolean quizExists(Connection conn, int quizId) throws SQLException {
        String sql = "SELECT 1 FROM quiz WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}