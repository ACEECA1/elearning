package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO;
import com.app.model.interactions.Answer;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnswerDAO implements DAO<Answer> {

    public void insert(Connection conn, Answer answer) throws SQLException {
        String sql = "INSERT INTO answer (question_id, text, is_correct) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, answer);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    answer.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting answer: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Answer answer) {
        try (Connection conn = Database.getConnection()) {
            if (!questionExists(conn, answer.getQuestionId())) {
                throw new SQLException("Cannot insert answer: Question with ID " + answer.getQuestionId() + " does not exist.");
            }
            this.insert(conn, answer);
        } catch (SQLException e) {
            System.out.println("Error inserting answer: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Connection conn, Answer answer) throws SQLException {
        String sql = "UPDATE answer SET question_id = ?, text = ?, is_correct = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, answer);
            ps.setInt(4, answer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating answer: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Answer answer) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, answer);
        } catch (SQLException e) {
            System.out.println("Error updating answer: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM answer WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting answer: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error deleting answer: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Answer findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM answer WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToAnswer(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding answer by ID: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Answer findById(int id) {
        try (Connection conn = Database.getConnection()) {
            return this.findById(conn, id);
        } catch (SQLException e) {
            System.out.println("Error finding answer by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Answer> findAll(Connection conn) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answer";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                answers.add(mapResultSetToAnswer(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all answers: " + e.getMessage());
            throw e;
        }
        return answers;
    }

    @Override
    public List<Answer> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all answers: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Answer> findByQuestionId(Connection conn, int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answer WHERE question_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapResultSetToAnswer(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding answers by question ID: " + e.getMessage());
            throw e;
        }
        return answers;
    }

    public List<Answer> findByQuestionId(int questionId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByQuestionId(conn, questionId);
        } catch (SQLException e) {
            System.out.println("Error finding answers by question ID: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private void setStatementParameters(PreparedStatement ps, Answer answer) throws SQLException {
        ps.setInt(1, answer.getQuestionId());
        ps.setString(2, answer.getText());
        ps.setBoolean(3, answer.isCorrect());
    }

    private Answer mapResultSetToAnswer(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int questionId = rs.getInt("question_id");
        String text = rs.getString("text");
        boolean isCorrect = rs.getBoolean("is_correct");

        return new Answer(id, text, isCorrect, questionId);
    }

    private boolean questionExists(Connection conn, int questionId) throws SQLException {
        String sql = "SELECT 1 FROM question WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}