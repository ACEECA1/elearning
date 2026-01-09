package com.app.service;

import com.app.dao.implementation.interactions.CommentDAO;
import com.app.dao.implementation.interactions.ForumDAO;
import com.app.dao.implementation.interactions.EnrollmentDAO;
import com.app.dao.implementation.course.ChapterDAO;
import com.app.dao.implementation.course.ModuleDAO;
import com.app.dao.implementation.course.CourseDAO;
import com.app.model.interactions.Comment;
import com.app.model.interactions.Forum;
import com.app.model.course.Chapter;
import com.app.model.course.Module;
import com.app.model.course.Course;
import com.app.util.Database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentService {

    private final CommentDAO commentDAO = new CommentDAO();
    private final ForumDAO forumDAO = new ForumDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Comment> getCommentsByForum(int forumId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            List<Comment> allComments = commentDAO.findByForumId(conn, forumId);
            return organizeCommentsIntoTree(allComments);
        }
    }

    public void addComment(Comment comment, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            verifyForumAccess(conn, comment.getForumId(), userId, role);
            
            comment.setUserId(userId);
            commentDAO.insert(conn, comment);
        }
    }

    public void deleteComment(int commentId, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Comment existing = commentDAO.findById(conn, commentId);
            if (existing == null) throw new Exception("Comment not found");

            // Allow delete if:
            //User is the author of the comment
            //User is the Teacher of the course
            boolean isAuthor = existing.getUserId() == userId;
            
            if (!isAuthor && "TEACHER".equalsIgnoreCase(role)) {
                // Check if teacher owns the course
                if (!isTeacherOfCommentForum(conn, existing.getForumId(), userId)) {
                    throw new Exception("Unauthorized: You do not own this course");
                }
            } else if (!isAuthor) {
                throw new Exception("Unauthorized: You can only delete your own comments.");
            }

            commentDAO.delete(conn, commentId);
        }
    }

    // Helper: Build nested reply structure
    private List<Comment> organizeCommentsIntoTree(List<Comment> allComments) {
        Map<Integer, Comment> commentMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, c -> c));
        
        List<Comment> rootComments = new ArrayList<>();

        for (Comment c : allComments) {
            c.setReplies(new ArrayList<>()); // Initialize list
        }

        for (Comment c : allComments) {
            if (c.isReply() && c.getParentCommentId() > 0) {
                Comment parent = commentMap.get(c.getParentCommentId());
                if (parent != null) {
                    parent.getReplies().add(c);
                }
            } else {
                rootComments.add(c);
            }
        }
        return rootComments;
    }

    public void updateComment(int commentId , String newContent, int userId, String role) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Comment existing = commentDAO.findById(conn, commentId);
            if (existing == null) throw new Exception("Comment not found");

            // Allow update if:
            //User is the author of the comment
            //User is the Teacher of the course
            boolean isAuthor = existing.getUserId() == userId;

            if (!isAuthor && "TEACHER".equalsIgnoreCase(role)) {
                if (!isTeacherOfCommentForum(conn, existing.getForumId(), userId)) {
                    throw new Exception("Unauthorized: You do not own this course");
                }
            } else if (!isAuthor) {
                throw new Exception("Unauthorized: You can only update your own comments.");
            }

            existing.setContent(newContent);
            existing.setModified(true);

            commentDAO.update(conn, existing);
        }
    }
    private void verifyForumAccess(Connection conn, int forumId, int userId, String role) throws Exception {
        Forum forum = forumDAO.findById(conn, forumId);
        if (forum == null) throw new Exception("Forum not found");

        Chapter chapter = chapterDAO.findById(conn, forum.getChapterId());
        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        Course course = courseDAO.findById(conn, module.getCourseId());

        if ("TEACHER".equalsIgnoreCase(role)) {
            if (course.getTeacherId() != userId) {
                throw new Exception("Unauthorized: You do not own this course");
            }
        } else {
            // Check enrollment for students
            if (!EnrollmentDAO.isEnrolled(conn, userId, course.getId())) {
                throw new Exception("Unauthorized: You must be enrolled to comment");
            }
        }
    }
    
    private boolean isTeacherOfCommentForum(Connection conn, int forumId, int teacherId) throws Exception {
        Forum forum = forumDAO.findById(conn, forumId);
        Chapter chapter = chapterDAO.findById(conn, forum.getChapterId());
        Module module = moduleDAO.findById(conn, chapter.getModuleId());
        Course course = courseDAO.findById(conn, module.getCourseId());
        return course.getTeacherId() == teacherId;
    }
}