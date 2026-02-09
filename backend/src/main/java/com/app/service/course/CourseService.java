package com.app.service.course;

import com.app.dao.implementation.course.CourseDAO;
import com.app.dao.implementation.interactions.EnrollmentDAO;
import com.app.dao.implementation.users.StudentDAO;
import com.app.model.course.Course;
import com.app.model.interactions.Enrollment;
import com.app.model.users.Student;
import com.app.service.interactions.NotificationService;
import com.app.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final NotificationService notificationService = new NotificationService();
    public List<Course> getAllAvailableCourses() throws Exception {
        try (Connection conn = Database.getConnection()) {
            return courseDAO.findAll(conn);
        } catch (SQLException e) {
            throw new Exception("Error fetching courses: " + e.getMessage());
        }
    }

    public Course getCourseById(int courseId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Course course = courseDAO.findById(conn, courseId);
            if (course == null) {
                throw new Exception("Course not found");
            }
            return course;
        }
    }
    public boolean isStudentEnrolledInCourse(int studentId, int courseId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return EnrollmentDAO.isEnrolled(conn, studentId, courseId);
        }
    }
    public List<Course> getCoursesByTeacher(int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            List<Course> teacherCourses = courseDAO.findByTeacherId(conn, teacherId);
            return teacherCourses;
        }
    }

    public List<Course> getEnrolledCourses(int studentId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            List<Enrollment> enrollments = enrollmentDAO.findByStudentId(conn, studentId);
            List<Course> courses = new ArrayList<>();
            for (Enrollment e : enrollments) {
                Course c = courseDAO.findById(conn, e.getCourseId());
                if (c != null) courses.add(c);
            }
            return courses;
        }
    }

    public List<Student> getCourseParticipants(int courseId, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Course course = courseDAO.findById(conn, courseId);
            if (course == null) throw new Exception("Course not found");
            if (course.getTeacherId() != teacherId) {
                throw new Exception("Unauthorized: You do not own this course");
            }

            List<Enrollment> enrollments = enrollmentDAO.findByCourseId(conn, courseId);
            List<Student> students = new ArrayList<>();

            for (Enrollment e : enrollments) {
                Student s = studentDAO.findById(conn, e.getStudentId());
                if (s != null) students.add(s);
            }
            return students;
        }
    }


    public void createCourse(Course course, int teacherId) throws Exception {
        try (Connection conn = Database.getConnection()) {

            course.setTeacherId(teacherId);
            courseDAO.insert(conn, course);
        } catch (SQLException e) {
            throw new Exception("Error creating course: " + e.getMessage());
        }
    }

    public void updateCourse(Course courseUpdates, int teacherId , String userRole) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Course existing = courseDAO.findById(conn, courseUpdates.getId());
            if (existing == null) throw new Exception("Course not found");
            
            if (existing.getTeacherId() != teacherId && !"ADMIN".equalsIgnoreCase(userRole)) {
                throw new Exception("Unauthorized: You do not own this course");
            }

            existing.setTitle(courseUpdates.getTitle());
            existing.setDescription(courseUpdates.getDescription());
            existing.setTargetAudience(courseUpdates.getTargetAudience());
            existing.setEnrollmentKey(courseUpdates.getEnrollmentKey());
            existing.setThumbnailPath(courseUpdates.getThumbnailPath());
            
            courseDAO.update(conn, existing);
        }
    }

    public void deleteCourse(int courseId, int teacherId, String userRole) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Course existing = courseDAO.findById(conn, courseId);
            if (existing == null) throw new Exception("Course not found");

            if (existing.getTeacherId() != teacherId && !"ADMIN".equalsIgnoreCase(userRole)) {
                throw new Exception("Unauthorized: You do not own this course");
            }
            courseDAO.delete(conn, courseId);
        }
    }


    public void enrollStudent(int courseId, int studentId, String enrollmentKey) throws Exception {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            if (EnrollmentDAO.isEnrolled(conn, studentId, courseId)) {
                throw new Exception("Student is already enrolled in this course");
            }

            Course course = courseDAO.findById(conn, courseId);
            if (course == null) throw new Exception("Course not found");
            if (course.getEnrollmentKey() != null && !course.getEnrollmentKey().isEmpty()) {
                if (!course.getEnrollmentKey().equals(enrollmentKey)) {
                    throw new Exception("Invalid enrollment key");
                }
            }

            Enrollment enrollment = new Enrollment(studentId, courseId, new java.sql.Timestamp(System.currentTimeMillis()));
            enrollmentDAO.insert(conn, enrollment);
            int teacherId = course.getTeacherId();
            notificationService.sendNotification(conn ,teacherId, "New Enrollment",
                "A new student has enrolled in your course: " + course.getTitle(),
                "NEW_ENROLLMENT");
            conn.commit();
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            Database.closeConnection(conn);
        }
    }

    public void unenrollStudent(int courseId, int studentId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (!EnrollmentDAO.isEnrolled(conn, studentId, courseId)) {
                throw new Exception("Student is not enrolled in this course");
            }
            enrollmentDAO.delete(conn, studentId, courseId);
        }
    }

    public boolean teacherOwnsCourse(int teacherId, int courseId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Course course = courseDAO.findById(conn, courseId);
            if (course == null) {
                throw new Exception("Course not found");
            }
            return course.getTeacherId() == teacherId;
        }
    }
    public List<Course> search(String query) throws Exception {
        try (Connection conn = Database.getConnection()) {
            return courseDAO.searchCourses(conn, query);
        }
    }
    public int getCourseCount() throws Exception {
        try (Connection conn = Database.getConnection()) {
            return courseDAO.countCourses(conn);
        }
    }
}