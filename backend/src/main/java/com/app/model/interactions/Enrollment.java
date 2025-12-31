package com.app.model.interactions;

import java.util.Date;
import com.app.model.users.Student;
import com.app.model.course.Course;

// CREATE TABLE `enrollment` (
//     student_id INT NOT NULL,
//     course_id INT NOT NULL,
//     enrollment_date DATETIME NOT NULL,
//     PRIMARY KEY (student_id, course_id),
//     FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
//     FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
// );
public class Enrollment {
    private int studentId;
    private int courseId;
    private Date enrollmentDate;
    private Student student;
    private Course course;
    public Enrollment(int studentId, int courseId, Date enrollmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
    }
    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    public int getCourseId() {
        return courseId;
    }
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
}
