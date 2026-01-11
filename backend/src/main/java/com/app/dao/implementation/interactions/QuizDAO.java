package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Quiz;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO implements DAO<Quiz> {

    public void insert(Connection conn, Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quiz (chapter_id, title, description, max_grade, file_path, available_from, available_to) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, quiz);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    quiz.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting quiz: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Quiz quiz) {
        try (Connection conn = Database.getConnection()) {
            if (!chapterExists(conn, quiz.getChapterId())) {
                throw new SQLException("Cannot insert quiz: Chapter with ID " + quiz.getChapterId() + " does not exist.");
            }
            this.insert(conn, quiz);
        } catch (SQLException e) {
            System.out.println("Error inserting quiz: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Quiz quiz) throws SQLException {
        String sql = "UPDATE quiz SET chapter_id = ?, title = ?, description = ?, max_grade = ?, file_path = ?, available_from = ?, available_to = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, quiz);
            ps.setInt(8, quiz.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating quiz: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Quiz quiz) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, quiz);
        } catch (SQLException e) {
            System.out.println("Error updating quiz: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting quiz: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting quiz: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Quiz findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM quiz WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToQuiz(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding quiz by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Quiz findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding quiz by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Quiz> findAll(Connection conn) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quiz";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                quizzes.add(mapResultSetToQuiz(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all quizzes: " + e.getMessage());
            throw e;
        }
        return quizzes;
    }

    @Override
    public List<Quiz> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all quizzes: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Quiz> findByChapterId(Connection conn, int chapterId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE chapter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding quizzes by chapter ID: " + e.getMessage());
            throw e;
        }
        return quizzes;
    }

    public List<Quiz> findByChapterId(int chapterId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByChapterId(conn, chapterId);
        } catch (SQLException e) {
            System.out.println("Error finding quizzes by chapter ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public List<Quiz> findAllAvailableForStudent(Connection conn, int chapterId, Timestamp currentTime) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE chapter_id = ? AND (available_from IS NULL OR available_from <= ?) AND (available_to IS NULL OR available_to >= ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            ps.setTimestamp(2, currentTime);
            ps.setTimestamp(3, currentTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding available quizzes for student: " + e.getMessage());
            throw e;
        }
        return quizzes;
    }
    public Quiz findAvailableForStudent(Connection conn, int quizId, Timestamp currentTime) throws SQLException {
        String sql = "SELECT * FROM quiz WHERE id = ? AND (available_from IS NULL OR available_from <= ?) AND (available_to IS NULL OR available_to >= ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setTimestamp(2, currentTime);
            ps.setTimestamp(3, currentTime);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToQuiz(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding quiz by ID for student: " + e.getMessage());
            throw e;
        }
    }

    private void setStatementParameters(PreparedStatement ps, Quiz quiz) throws SQLException {
        ps.setInt(1, quiz.getChapterId());
        ps.setString(2, quiz.getTitle());
        ps.setString(3, quiz.getDescription());
        ps.setDouble(4, quiz.getMaxGrade());
        ps.setString(5, quiz.getFilePath());
        if (quiz.getAvailableFrom() != null) {
            ps.setTimestamp(6, new Timestamp(quiz.getAvailableFrom().getTime()));
        } else {
            ps.setNull(6, Types.TIMESTAMP);
        }
        
        if (quiz.getAvailableTo() != null) {
            ps.setTimestamp(7, new Timestamp(quiz.getAvailableTo().getTime()));
        } else {
            ps.setNull(7, Types.TIMESTAMP);
        }
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int chapterId = rs.getInt("chapter_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        double maxGrade = rs.getDouble("max_grade");
        Timestamp availableFrom = rs.getTimestamp("available_from");
        Timestamp availableTo = rs.getTimestamp("available_to");
        String filePath = rs.getString("file_path");
        return new Quiz(id, chapterId, title, description, maxGrade, filePath, availableFrom, availableTo);
    }

    private boolean chapterExists(Connection conn, int chapterId) throws SQLException {
        String sql = "SELECT 1 FROM chapter WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}