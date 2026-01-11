package com.app.dao.implementation.interactions;

import com.app.dao.interfaces.DAO; // Assuming this interface exists
import com.app.model.interactions.Submission; // Adjust package to where you put Submission
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO implements DAO<Submission> {

    // ---------------------------------------------------
    // INSERT (Create new submission)
    // ---------------------------------------------------
    public void insert(Connection conn, Submission submission) throws SQLException {
        // Note: Grade and Feedback are nullable (default NULL for file uploads)
        String sql = "INSERT INTO submission (student_id, quiz_id, submission_path, grade, feedback) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setStatementParameters(ps, submission);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting submission: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Submission submission) {
        try (Connection conn = Database.getConnection()) {
            if (!studentAndQuizExist(conn, submission.getStudentId(), submission.getQuizId())) {
                throw new SQLException("Cannot insert submission: Student or Quiz does not exist.");
            }
            this.insert(conn, submission);
        } catch (SQLException e) {
            System.out.println("Error inserting submission: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------
    // UPDATE (Teacher grading or Student resubmitting)
    // ---------------------------------------------------
    public void update(Connection conn, Submission submission) throws SQLException {
        // Updates grade, feedback, or the file path based on the composite key
        String sql = "UPDATE submission SET submission_path = ?, grade = ?, feedback = ?, submission_date = CURRENT_TIMESTAMP WHERE student_id = ? AND quiz_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Note: Parameter order is different in UPDATE than INSERT
            if (submission.getSubmissionPath() != null) {
                ps.setString(1, submission.getSubmissionPath());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }

            if (submission.getGrade() != null) {
                ps.setDouble(2, submission.getGrade());
            } else {
                ps.setNull(2, Types.DECIMAL);
            }

            if (submission.getFeedback() != null) {
                ps.setString(3, submission.getFeedback());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            // WHERE Clause parameters
            ps.setInt(4, submission.getStudentId());
            ps.setInt(5, submission.getQuizId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating submission: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Submission submission) {
        try (Connection conn = Database.getConnection()) {
            this.update(conn, submission);
        } catch (SQLException e) {
            System.out.println("Error updating submission: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------
    // DELETE (Remove a submission)
    // ---------------------------------------------------
    // NOTE: Standard DAO interface usually has delete(int id). 
    // Since we have a composite key, we overload it.
    
    public void delete(Connection conn, int studentId, int quizId) throws SQLException {
        String sql = "DELETE FROM submission WHERE student_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting submission: " + e.getMessage());
            throw e;
        }
    }

    // We can't implement the standard delete(int id) effectively due to composite key
    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Use delete(studentId, quizId) for Submissions");
    }

    public void delete(int studentId, int quizId) {
        try (Connection conn = Database.getConnection()) {
            this.delete(conn, studentId, quizId);
        } catch (SQLException e) {
            System.out.println("Error deleting submission: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------
    // FIND ONE (By Composite Key)
    // ---------------------------------------------------
    public Submission findByCompositeId(Connection conn, int studentId, int quizId) throws SQLException {
        String sql = "SELECT * FROM submission WHERE student_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToSubmission(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Error finding submission: " + e.getMessage());
            throw e;
        }
    }

    public Submission findByCompositeId(int studentId, int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByCompositeId(conn, studentId, quizId);
        } catch (SQLException e) {
            System.out.println("Error finding submission: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    // Standard DAO findById might not work well here, so we stub it or assume ID logic isn't used
    @Override
    public Submission findById(int id) {
        throw new UnsupportedOperationException("Use findByCompositeId(studentId, quizId)");
    }

    // ---------------------------------------------------
    // FIND ALL (For Teacher Dashboard)
    // ---------------------------------------------------
    public List<Submission> findAll(Connection conn) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submission";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all submissions: " + e.getMessage());
            throw e;
        }
        return submissions;
    }

    @Override
    public List<Submission> findAll() {
        try (Connection conn = Database.getConnection()) {
            return this.findAll(conn);
        } catch (SQLException e) {
            System.out.println("Error finding all submissions: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------
    // HELPER: Find by Quiz (See all students who answered a quiz)
    // ---------------------------------------------------
    public List<Submission> findByQuizId(Connection conn, int quizId) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submission WHERE quiz_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapResultSetToSubmission(rs));
                }
            }
        }
        return submissions;
    }

    public List<Submission> findByQuizId(int quizId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByQuizId(conn, quizId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Submission> findByStudentId(Connection conn, int studentId) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submission WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapResultSetToSubmission(rs));
                }
            }
        }
        return submissions;
    }
    public List<Submission> findByStudentId(int studentId) {
        try (Connection conn = Database.getConnection()) {
            return this.findByStudentId(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------
    // UTILITIES
    // ---------------------------------------------------
    private void setStatementParameters(PreparedStatement ps, Submission submission) throws SQLException {
        ps.setInt(1, submission.getStudentId());
        ps.setInt(2, submission.getQuizId());
        
        if (submission.getSubmissionPath() != null) {
            ps.setString(3, submission.getSubmissionPath());
        } else {
            ps.setNull(3, Types.VARCHAR);
        }
        
        if (submission.getGrade() != null) {
            ps.setDouble(4, submission.getGrade());
        } else {
            ps.setNull(4, Types.DECIMAL);
        }
        
        if (submission.getFeedback() != null) {
            ps.setString(5, submission.getFeedback());
        } else {
            ps.setNull(5, Types.VARCHAR);
        }
    }

    private Submission mapResultSetToSubmission(ResultSet rs) throws SQLException {
        Submission sub = new Submission();
        sub.setStudentId(rs.getInt("student_id"));
        sub.setQuizId(rs.getInt("quiz_id"));
        sub.setSubmissionPath(rs.getString("submission_path"));
        
        // Handle Nullable Grade
        double grade = rs.getDouble("grade");
        if (!rs.wasNull()) {
            sub.setGrade(grade);
        }
        
        sub.setFeedback(rs.getString("feedback"));
        sub.setSubmissionDate(rs.getTimestamp("submission_date"));
        return sub;
    }

    private boolean studentAndQuizExist(Connection conn, int studentId, int quizId) throws SQLException {
        String studentSql = "SELECT 1 FROM student WHERE id = ?";
        String quizSql = "SELECT 1 FROM quiz WHERE id = ?";
        
        boolean studentExists = false;
        try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                studentExists = rs.next();
            }
        }
        
        if (!studentExists) return false;

        try (PreparedStatement ps = conn.prepareStatement(quizSql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}