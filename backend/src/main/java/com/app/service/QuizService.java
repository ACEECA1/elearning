package com.app.service;

import com.app.dao.implementation.interactions.QuizDAO;
import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.interactions.Quiz;
import com.app.model.users.Student;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public class QuizService {

    private final QuizDAO quizDAO = new QuizDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final ChapterService chapterService = new ChapterService();
    private final ModuleService moduleService = new ModuleService();
    private final CourseService courseService = new CourseService();
    private final NotificationService notificationService = new NotificationService();

    public List<Quiz> getQuizzesByChapter(int chapterId, String role) throws Exception {
        if ("STUDENT".equalsIgnoreCase(role)) {
            try (Connection conn = Database.getConnection()) {
                return quizDAO.findAllAvailableForStudent(conn, chapterId, new Timestamp(System.currentTimeMillis()));
            }
        } else {
            try (Connection conn = Database.getConnection()) {
                return quizDAO.findByChapterId(conn, chapterId);
            }
        }
    }

    public Quiz getQuizById(int quizId , String role) throws Exception {
        if("STUDENT".equalsIgnoreCase(role)){
            try (Connection conn = Database.getConnection()) {
                return quizDAO.findAvailableForStudent(conn, quizId, new Timestamp(System.currentTimeMillis()));
            }
        } else {
            try (Connection conn = Database.getConnection()) {
                return quizDAO.findById(conn, quizId);
            }
        }
    }

    public void createQuiz(Quiz quiz, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyChapterOwnership(conn, quiz.getChapterId(), userId);
            }
            Chapter chapter = chapterService.getChapterById(quiz.getChapterId());
            Module module = moduleService.getModuleById(chapter.getModuleId());
            int courseId = module.getCourseId();
            List<Student> enrolledStudents = courseService.getCourseParticipants(courseId, userId);
            quizDAO.insert(conn, quiz);
            for(Student student : enrolledStudents){
                notificationService.sendNotification(student.getId(), "New Quiz Available",
                        "A new quiz titled '" + quiz.getTitle() + "' has been created in chapter '" + chapter.getTitle() + "'.",
                        "NEW_QUIZ");
            }
        }
    }

    public void updateQuiz(Quiz quizUpdates, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Quiz existing = quizDAO.findById(conn, quizUpdates.getId());
            if (existing == null) throw new Exception("Quiz not found");

            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyChapterOwnership(conn, existing.getChapterId(), userId);
            }

            existing.setTitle(quizUpdates.getTitle());
            existing.setDescription(quizUpdates.getDescription());
            existing.setAvailableFrom(quizUpdates.getAvailableFrom());
            existing.setAvailableTo(quizUpdates.getAvailableTo());
            
            quizDAO.update(conn, existing);
        }
    }

    public void deleteQuiz(int quizId, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Quiz existing = quizDAO.findById(conn, quizId);
            if (existing == null) throw new Exception("Quiz not found");

            if (!"ADMIN".equalsIgnoreCase(role)) {
                verifyChapterOwnership(conn, existing.getChapterId(), userId);
            }

            quizDAO.delete(conn, quizId);
        }
    }

    private void verifyChapterOwnership(Connection conn, int chapterId, int teacherId) throws Exception {
        Chapter chapter = chapterDAO.findById(conn, chapterId);
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        Course course = courseDAO.findById(conn, module.getCourseId());
        if (course == null) throw new Exception("Course not found");

        if (course.getTeacherId() != teacherId) {
            throw new Exception("Unauthorized: You do not own the course this quiz belongs to.");
        }
    }
}