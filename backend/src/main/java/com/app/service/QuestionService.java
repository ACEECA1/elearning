package com.app.service;

import com.app.dao.implementation.interactions.QuestionDAO;
import com.app.dao.implementation.interactions.QuizDAO;
import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.interactions.Question;
import com.app.model.interactions.Quiz;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.util.List;

public class QuestionService {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Question> getQuestionsByQuiz(int quizId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return questionDAO.findByQuizId(conn, quizId);
        }
    }

    public Question getQuestionById(int questionId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Question question = questionDAO.findById(conn, questionId);
            if (question == null) throw new Exception("Question not found");
            return question;
        }
    }

    public void createQuestion(Question question, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuizOwnership(conn, question.getQuizId(), userId);
            }
            questionDAO.insert(conn, question);
        }
    }

    public void updateQuestion(Question questionUpdates, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Question existing = questionDAO.findById(conn, questionUpdates.getId());
            if (existing == null) throw new Exception("Question not found");

            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuizOwnership(conn, existing.getQuizId(), userId);
            }

            existing.setText(questionUpdates.getText());
            existing.setMaterialPath(questionUpdates.getMaterialPath());
            existing.setScore(questionUpdates.getScore());
            
            questionDAO.update(conn, existing);
        }
    }

    public void deleteQuestion(int questionId, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Question existing = questionDAO.findById(conn, questionId);
            if (existing == null) throw new Exception("Question not found");

            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyQuizOwnership(conn, existing.getQuizId(), userId);
            }

            questionDAO.delete(conn, questionId);
        }
    }

    // Helper: Traces Quiz -> Chapter -> Module -> Course -> Teacher
    private void verifyQuizOwnership(Connection conn, int quizId, int teacherId) throws Exception {
        Quiz quiz = quizDAO.findById(conn, quizId);
        if (quiz == null) throw new Exception("Quiz not found");

        Chapter chapter = chapterDAO.findById(conn, quiz.getChapterId());
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this question belongs to.");
        }
    }
}