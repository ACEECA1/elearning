package com.app.dao.implementation.interactions;

// import com.app.dao.interfaces.DAO; // Removed if your DAO interface enforces findById(int)
import com.app.model.interactions.Note;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO { // Removed "implements DAO<Note>" if the interface enforces single-ID methods

    // --- INSERT ---
    public void insert(Connection conn, Note note) throws SQLException {
        // No ID generation here
        String sql = "INSERT INTO note (student_id, quiz_id, grade) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, note); // Sets: student, quiz, grade
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting note: " + e.getMessage());
            throw e;
        }
    }

    public void insert(Note note) {
        try (Connection conn = Database.getConnection()) {
            if (!studentExists(conn, note.getStudentId())) {
                throw new SQLException("Cannot insert note: Student " + note.getStudentId() + " does not exist.");
            }
            if (!quizExists(conn, note.getQuizId())) {
                 throw new SQLException("Cannot insert note: Quiz " + note.getQuizId() + " does not exist.");
            }
            this.insert(conn, note);
        } catch (SQLException e) {
            System.out.println("Error inserting note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- UPDATE ---
    public void update(Connection conn, Note note) throws SQLException {
        // FIXED: Parameter order must match SQL (Grade first, then IDs)
        String sql = "UPDATE note SET grade = ? WHERE student_id = ? AND quiz_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Manual mapping because order is different from INSERT
            ps.setDouble(1, note.getGrade());
            ps.setInt(2, note.getStudentId());
            ps.setInt(3, note.getQuizId());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating note: " + e.getMessage());
            throw e;
        }
    }

    public void update(Note note) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, note);
        } catch (SQLException e) {
            System.out.println("Error updating note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- DELETE (Composite Key) ---
    public void delete(Connection conn, int studentId, int quizId) throws SQLException {
        String sql = "DELETE FROM note WHERE student_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting note: " + e.getMessage());
            throw e;
        }
    }

    public void delete(int studentId, int quizId) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, studentId, quizId);
        } catch (SQLException e) {
            System.out.println("Error deleting note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- FIND BY COMPOSITE ID ---
    public Note findByCompositeId(Connection conn, int studentId, int quizId) throws SQLException {
        String sql = "SELECT * FROM note WHERE student_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToNote(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding note: " + e.getMessage());
            throw e;
        }
    }

    public Note findByCompositeId(int studentId, int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCompositeId(conn, studentId, quizId);
        } catch (SQLException e) {
            System.out.println("Error finding note: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- FIND ALL ---
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

    public List<Note> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all notes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- FIND BY STUDENT ---
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
            System.out.println("Error finding notes by student: " + e.getMessage());
            throw e;
        }
        return notes;
    }

    public List<Note> findByStudentId(int studentId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByStudentId(conn, studentId);
        } catch (SQLException e) {
            System.out.println("Error finding notes by student: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- FIND BY QUIZ ---
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
            System.out.println("Error finding notes by quiz: " + e.getMessage());
            throw e;
        }
        return notes;
    }

    public List<Note> findByQuizId(int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByQuizId(conn, quizId);
        } catch (SQLException e) {
            System.out.println("Error finding notes by quiz: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // --- HELPERS ---

    private void setStatementParameters(PreparedStatement ps, Note note) throws SQLException {
        ps.setInt(1, note.getStudentId());
        ps.setInt(2, note.getQuizId());
        ps.setDouble(3, note.getGrade());
    }

    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        int studentId = rs.getInt("student_id");
        int quizId = rs.getInt("quiz_id");
        double grade = rs.getDouble("grade");
        // Ensure your Note class has a constructor that matches this!
        java.util.Date dateRecorded = new java.util.Date(rs.getTimestamp("date_recorded").getTime());

        return new Note(studentId, quizId, grade, dateRecorded);
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