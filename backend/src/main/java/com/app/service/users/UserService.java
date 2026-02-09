package com.app.service.users;

import com.app.dao.implementation.users.AdminDAO;
import com.app.dao.implementation.users.StudentDAO;
import com.app.dao.implementation.users.TeacherDAO;
import com.app.dao.implementation.users.UserDAO;
import com.app.service.interactions.NotificationService;
import com.app.model.users.Admin;
import com.app.model.users.Student;
import com.app.model.users.Teacher;
import com.app.model.users.User;
import com.app.util.Database;
import com.app.util.PasswordUtils;

import java.sql.Connection;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final NotificationService notificationService = new NotificationService();
    public int userCount() throws Exception {
        try (Connection conn = Database.getConnection()) {
            System.out.println("Counting users...");
            int count = userDAO.count(conn);
            System.out.println("User count: " + count);
            return count;
        }
    }
    public int studentCount() throws Exception {
        try (Connection conn = Database.getConnection()) {
            System.out.println("Counting students...");
            int count = studentDAO.count(conn);
            System.out.println("Student count: " + count);
            return count;
        }
    }
    public int teacherCount() throws Exception {
        try (Connection conn = Database.getConnection()) {
            System.out.println("Counting teachers...");
            int count = teacherDAO.count(conn);
            System.out.println("Teacher count: " + count);
            return count;
        }
    }
    public User getUserById(int id) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Student s = studentDAO.findById(conn, id);
            if (s != null) return s;

            Teacher t = teacherDAO.findById(conn, id);
            if (t != null) return t;

            return userDAO.findById(conn, id);
        }
    }

    public List<User> getAllUsers() throws Exception {
        try (Connection conn = Database.getConnection()) {
            return userDAO.findAll(conn);
        }
    }

    public List<Student> getAllStudents() throws Exception {
        try (Connection conn = Database.getConnection()) {
            return studentDAO.findAll(conn);
        }
    }

    public List<Teacher> getAllTeachers() throws Exception {
        try (Connection conn = Database.getConnection()) {
            return teacherDAO.findAll(conn);
        }
    }

    public void addStudent(Student student, String rawPassword) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (userDAO.findByEmail(conn, student.getEmail()) != null) {
                throw new Exception("Email already exists");
            }

            String salt = PasswordUtils.getSalt();
            String hashedPassword = PasswordUtils.hashPassword(rawPassword, salt);
            
            student.setSalt(salt);
            student.setPasswordHash(hashedPassword);
            student.setVerified(true);

            studentDAO.insert(conn, student);
            System.out.println("Added student: " + student.getId());
            notificationService.sendStudentWelcomeNotification(student.getId());
        }
    }

    public void addTeacher(Teacher teacher, String rawPassword) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (userDAO.findByEmail(conn, teacher.getEmail()) != null) {
                throw new Exception("Email already exists");
            }

            String salt = PasswordUtils.getSalt();
            String hashedPassword = PasswordUtils.hashPassword(rawPassword, salt);

            teacher.setSalt(salt);
            teacher.setPasswordHash(hashedPassword);
            teacher.setVerified(true);

            teacherDAO.insert(conn, teacher);
            System.out.println("Added teacher: " + teacher.getId());
            notificationService.sendTeacherWelcomeNotification(teacher.getId());
        }
    }
    public void addAdmin(Admin admin, String rawPassword) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (userDAO.findByEmail(conn, admin.getEmail()) != null) {
                throw new Exception("Email already exists");
            }

            String salt = PasswordUtils.getSalt();
            String hashedPassword = PasswordUtils.hashPassword(rawPassword, salt);

            admin.setSalt(salt);
            admin.setPasswordHash(hashedPassword);
            admin.setVerified(true);

            adminDAO.insert(conn, admin);
            System.out.println("Added admin: " + admin.getId());
        }
    }

    public void updateStudent(Student student) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Student existing = studentDAO.findById(conn, student.getId());
            if (existing == null) {
                throw new Exception("Student not found");
            }

            student.setPasswordHash(existing.getPasswordHash());
            student.setSalt(existing.getSalt());
            student.setVerified(existing.isVerified());
            if (student.getProfilePicturePath() == null) {
                student.setProfilePicturePath(existing.getProfilePicturePath());
            }

            studentDAO.update(conn, student);
            notificationService.sendUpdateNotification(student.getId());
            System.out.println("Updated student: " + student.getId());
        }
    }

    public void updateTeacher(Teacher teacher) throws Exception {
        try (Connection conn = Database.getConnection()) {
            Teacher existing = teacherDAO.findById(conn, teacher.getId());
            if (existing == null) {
                throw new Exception("Teacher not found");
            }

            teacher.setPasswordHash(existing.getPasswordHash());
            teacher.setSalt(existing.getSalt());
            teacher.setVerified(existing.isVerified());
            if (teacher.getProfilePicturePath() == null) {
                teacher.setProfilePicturePath(existing.getProfilePicturePath());
            }

            teacherDAO.update(conn, teacher);
            notificationService.sendUpdateNotification(teacher.getId());
            System.out.println("Updated teacher: " + teacher.getId());
        }
    }


    public void deleteUser(int userId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            if (userDAO.findById(conn, userId) == null) {
                throw new Exception("User not found");
            }
            userDAO.delete(conn, userId);
            notificationService.sendAccountDeletionNotification(userId);
        }
        
    }
}