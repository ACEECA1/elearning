package com.app.service;

import com.app.dao.implementation.interactions.AnswerDAO;
import com.app.dao.implementation.interactions.QuestionDAO;
import com.app.dao.implementation.interactions.SubmissionDAO;
import com.app.model.interactions.Answer;
import com.app.model.interactions.Question;
import com.app.model.interactions.Submission;

import java.util.List;
import java.util.Map;

public class SubmissionService {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final AnswerDAO answerDAO = new AnswerDAO();

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
    
}