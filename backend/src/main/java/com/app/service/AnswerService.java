package com.app.service;

import com.app.dao.implementation.interactions.AnswerDAO;
import com.app.dao.implementation.interactions.QuestionDAO;
import com.app.dao.implementation.interactions.QuizDAO;
import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.interactions.Answer;
import com.app.model.interactions.Question;
import com.app.model.interactions.Quiz;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class AnswerService {

    private final AnswerDAO answerDAO = new AnswerDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Answer> getAnswersByQuestion(int questionId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return answerDAO.findByQuestionId(conn, questionId);
        }
    }

    public Answer getAnswerById(int answerId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Answer answer = answerDAO.findById(conn, answerId);
            if (answer == null) throw new Exception("Answer not found");
            return answer;
        }
    }

    public void createAnswer(Answer answer, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            // Admin bypasses ownership check
            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuestionOwnership(conn, answer.getQuestionId(), userId);
            }
            answerDAO.insert(conn, answer);
        }
    }

    public void updateAnswer(Answer answerUpdates, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Answer existing = answerDAO.findById(conn, answerUpdates.getId());
            if (existing == null) throw new Exception("Answer not found");

            // Admin bypasses ownership check
            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuestionOwnership(conn, existing.getQuestionId(), userId);
            }

            existing.setText(answerUpdates.getText());
            existing.setCorrect(answerUpdates.isCorrect());
            
            answerDAO.update(conn, existing);
        }
    }

    public void deleteAnswer(int answerId, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Answer existing = answerDAO.findById(conn, answerId);
            if (existing == null) throw new Exception("Answer not found");

            // Admin bypasses ownership check
            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuestionOwnership(conn, existing.getQuestionId(), userId);
            }

            answerDAO.delete(conn, answerId);
        }
    }

    // Helper: Traces Question -> Quiz -> Chapter -> Module -> Course -> Teacher
    private void verifyQuestionOwnership(Connection conn, int questionId, int teacherId) throws Exception {
        Question question = questionDAO.findById(conn, questionId);
        if (question == null) throw new Exception("Question not found");

        Quiz quiz = quizDAO.findById(conn, question.getQuizId());
        if (quiz == null) throw new Exception("Quiz not found");

        Chapter chapter = chapterDAO.findById(conn, quiz.getChapterId());
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this answer belongs to.");
        }
    }
}