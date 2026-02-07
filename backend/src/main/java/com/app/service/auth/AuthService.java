package com.app.service.auth;

import com.app.dao.implementation.auth.VerificationCodeDAO;
import com.app.dao.implementation.users.StudentDAO;
import com.app.dao.implementation.users.TeacherDAO;
import com.app.dao.implementation.users.UserDAO;
import com.app.dao.implementation.users.AdminDAO;
import com.app.model.users.Student;
import com.app.model.users.Teacher;
import com.app.model.users.User;
import com.app.util.EmailService;
import com.app.util.JWTUtil;
import com.app.util.PasswordUtils;
import com.google.gson.*;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final Gson gson = new Gson();
    private final VerificationCodeDAO verificationDAO = new VerificationCodeDAO();
    private final int VERIFICATION_CODE_TTL_MINUTES = 15; // 15 minutes
    
    public void sendVerificationCode(String email) throws Exception {
        if (userDAO.emailExists(email)) {
            throw new Exception("Email is already registered.");
        }

        String code = JWTUtil.generateCode();
        verificationDAO.save(email, code, VERIFICATION_CODE_TTL_MINUTES);
        
        EmailService.sendVerificationEmail(email, code);
    }

    public String login(String email, String password) throws Exception {
        User user = userDAO.findByEmail(email);

        if (user == null) {
            throw new Exception("User not found");
        }

        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
            throw new Exception("Invalid password");
        }

        String role = "USER";
        if (studentDAO.findById(user.getId()) != null) role = "STUDENT";
        else if (teacherDAO.findById(user.getId()) != null) role = "TEACHER";
        else if (adminDAO.findById(user.getId()) != null) role = "ADMIN";

        return JWTUtil.generateToken(user.getId(), role);
    }

    private boolean validateCode(String email, String inputCode) throws Exception {
        String validCode = verificationDAO.getValidCode(email);
        if (validCode == null) throw new Exception("Verification code invalid or expired.");
        if (!validCode.equals(inputCode)) throw new Exception("Invalid verification code.");
        
        verificationDAO.delete(email);
        return true;
    }

    public void registerStudent(Student student, String plainPassword, String code) throws Exception {
        try{
            validateCode(student.getEmail(), code);
        } catch(Exception e){
            throw new Exception("Code validation failed: " + e.getMessage());
        }
        
        String salt = PasswordUtils.getSalt();
        String hash = PasswordUtils.hashPassword(plainPassword, salt);
        
        student.setSalt(salt);
        student.setPasswordHash(hash);
        student.setVerified(true);

        studentDAO.insert(student);
    }

    public void registerTeacher(Teacher teacher, String plainPassword, String code) throws Exception {
        try{
            validateCode(teacher.getEmail(), code);
        } catch(Exception e){
            throw new Exception("Code validation failed: " + e.getMessage());
        }
        
        String salt = PasswordUtils.getSalt();
        String hash = PasswordUtils.hashPassword(plainPassword, salt);
        
        teacher.setSalt(salt);
        teacher.setPasswordHash(hash);
        teacher.setVerified(true);

        teacherDAO.insert(teacher);
    }
    public JsonObject getUserInfoAsJson(String email) throws Exception {
        User user = userDAO.findByEmail(email);
        if (user == null) throw new Exception("User not found");

        JsonObject userJson = gson.toJsonTree(user).getAsJsonObject();
        if (studentDAO.findById(user.getId()) != null) {
            userJson.addProperty("role", "STUDENT");
        } else if (teacherDAO.findById(user.getId()) != null) {
            userJson.addProperty("role", "TEACHER");
        } else if (adminDAO.findById(user.getId()) != null) {
            userJson.addProperty("role", "ADMIN");
        } else {
            userJson.addProperty("role", "USER");
        }
        return userJson;
    }
}