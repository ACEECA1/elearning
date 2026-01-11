package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Question;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO implements DAO<Question> {

    public void insert(Connection conn, Question question) throws SQLException {
        String sql = "INSERT INTO question (quiz_id, text, material, score) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, question);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting question: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Question question) {
        try (Connection conn = Database.getConnection()) {
            if (!quizExists(conn, question.getQuizId())) {
                throw new SQLException("Cannot insert question: Quiz with ID " + question.getQuizId() + " does not exist.");
            }
            this.insert(conn, question);
        } catch (SQLException e) {
            System.out.println("Error inserting question: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Question question) throws SQLException {
        String sql = "UPDATE question SET quiz_id = ?, text = ?, material = ?, score = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, question);
            ps.setInt(5, question.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating question: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Question question) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, question);
        } catch (SQLException e) {
            System.out.println("Error updating question: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM question WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Question findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM question WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToQuestion(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding question by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Question findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding question by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Question> findAll(Connection conn) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM question";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all questions: " + e.getMessage());
            throw e;
        }
        return questions;
    }

    @Override
    public List<Question> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all questions: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Question> findByQuizId(Connection conn, int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM question WHERE quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding questions by quiz ID: " + e.getMessage());
            throw e;
        }
        return questions;
    }

    public List<Question> findByQuizId(int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByQuizId(conn, quizId);
        } catch (SQLException e) {
            System.out.println("Error finding questions by quiz ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private void setStatementParameters(PreparedStatement ps, Question question) throws SQLException {
        ps.setInt(1, question.getQuizId());
        ps.setString(2, question.getText());
        ps.setString(3, question.getMaterialPath());
        ps.setInt(4, question.getScore());
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int quizId = rs.getInt("quiz_id");
        String text = rs.getString("text");
        String materialPath = rs.getString("material");
        int score = rs.getInt("score");

        return new Question(id, text, materialPath, score, quizId);
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