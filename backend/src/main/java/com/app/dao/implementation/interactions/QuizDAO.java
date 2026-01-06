package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Quiz;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO implements DAO<Quiz> {

    public void insert(Connection conn, Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quiz (chapter_id, title, description, available_from, available_to) VALUES (?, ?, ?, ?, ?)";
        
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
        String sql = "UPDATE quiz SET chapter_id = ?, title = ?, description = ?, available_from = ?, available_to = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, quiz);
            ps.setInt(6, quiz.getId());
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


    private void setStatementParameters(PreparedStatement ps, Quiz quiz) throws SQLException {
        ps.setInt(1, quiz.getChapterId());
        ps.setString(2, quiz.getTitle());
        ps.setString(3, quiz.getDescription());
        
        if (quiz.getAvailableFrom() != null) {
            ps.setTimestamp(4, new Timestamp(quiz.getAvailableFrom().getTime()));
        } else {
            ps.setNull(4, Types.TIMESTAMP);
        }
        
        if (quiz.getAvailableTo() != null) {
            ps.setTimestamp(5, new Timestamp(quiz.getAvailableTo().getTime()));
        } else {
            ps.setNull(5, Types.TIMESTAMP);
        }
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int chapterId = rs.getInt("chapter_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        Timestamp availableFrom = rs.getTimestamp("available_from");
        Timestamp availableTo = rs.getTimestamp("available_to");
        return new Quiz(id, chapterId, title, description, availableFrom, availableTo);
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