package com.app.service;

import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.interactions.AnswerDAO;
import com.app.dao.implementation.interactions.QuestionDAO;
import com.app.dao.implementation.interactions.QuizDAO;
import com.app.dao.implementation.interactions.SubmissionDAO;
import com.app.model.interactions.Answer;
import com.app.model.interactions.Question;
import com.app.model.interactions.Quiz;
import com.app.model.interactions.Submission;
import com.app.model.course.Chapter;
import com.app.model.course.Course;
import com.app.model.course.Module;

import java.util.List;
import java.util.Map;

public class SubmissionService {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final CourseService courseService = new CourseService();
    private final NotificationService notificationService = new NotificationService();

    public void submitFile(int studentId, int quizId, String filePath) {
        Submission submission = new Submission();
        submission.setStudentId(studentId);
        submission.setQuizId(quizId);
        submission.setSubmissionPath(filePath);
        submission.setGrade(null);
        submission.setFeedback(null);

        submissionDAO.insert(submission);
    }

    public double submitMCQ(int studentId, int quizId, Map<Integer, Integer> answersMap) {
        double totalScore = 0.0;

        List<Question> questions = questionDAO.findByQuizId(quizId);

        for (Question q : questions) {
            Integer selectedAnswerId = answersMap.get(q.getId());

            if (selectedAnswerId != null) {
                Answer answer = answerDAO.findById(selectedAnswerId);

                if (answer != null && answer.getQuestionId() == q.getId() && answer.isCorrect()) {
                    totalScore += q.getScore();
                }
            }
        }

        Submission submission = new Submission();
        submission.setStudentId(studentId);
        submission.setQuizId(quizId);
        submission.setSubmissionPath(null);
        submission.setGrade(totalScore);
        submission.setFeedback("Auto-graded");

        submissionDAO.insert(submission);
        try{
            sendGradeNotification(studentId, quizId, totalScore);
        }
        catch(Exception e){
            System.out.println("Failed to send grade notification: " + e.getMessage());
        }
        return totalScore;
    }
    public boolean gradeSubmission(int studentId, int quizId, double grade, String feedback) {
        // Fetch existing to preserve file path
        Submission existing = submissionDAO.findByCompositeId(studentId, quizId);
        if (existing == null) {
            return false;
        }

        // Update fields
        existing.setGrade(grade);
        existing.setFeedback(feedback);
        // DAO.update will use the existing path inside the object, so it won't be lost
        submissionDAO.update(existing);
        try{
            sendGradeNotification(studentId, quizId, grade);
        }
        catch(Exception e){
            System.out.println("Failed to send grade notification: " + e.getMessage());
        }
        return true;
    }

    // 2. Fetch Logic
    public Submission getSubmission(int studentId, int quizId) {
        return submissionDAO.findByCompositeId(studentId, quizId);
    }

    public List<Submission> getSubmissionsByQuiz(int quizId) {
        return submissionDAO.findByQuizId(quizId);
    }

    public List<Submission> getSubmissionsByStudent(int studentId) {
        return submissionDAO.findByStudentId(studentId); 
    }
    public void sendGradeNotification(int studentId, int quizId, double grade) throws Exception {
        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) throw new Exception("Quiz not found");

        Chapter chapter = chapterDAO.findById(quiz.getChapterId());
        if (chapter == null) throw new Exception("Chapter not found");

        Module module = moduleDAO.findById(chapter.getModuleId());
        if (module == null) throw new Exception("Module not found");

        int courseId = module.getCourseId();
        Course course = courseDAO.findById(courseId);
        if (course == null) throw new Exception("Course not found");
        if(courseService.isStudentEnrolledInCourse(studentId, courseId)){
            String notificationTitle = "Grade Released for Quiz: " + quiz.getTitle();
            String notificationMessage = "Your grade for the quiz '" + quiz.getTitle() + "' in course '" + course.getTitle() + "' is: " + grade;
            notificationService.sendNotification(studentId, notificationTitle, notificationMessage, "GRADE_RELEASED");
        }
    }
}