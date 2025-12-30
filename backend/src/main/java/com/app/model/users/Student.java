package com.app.model.users;

public class Student extends User {
    String studentCardNumber;
    String academicYear;
    public Student(int id, String firstName, String lastName, String email, String passwordHash, String salt,
                   String studentCardNumber, String academicYear) {
        super(id, firstName, lastName, email, passwordHash, salt);
        this.studentCardNumber = studentCardNumber;
        this.academicYear = academicYear;
    }
    public Student(String firstName, String lastName, String email, String passwordHash, String salt,
                   String studentCardNumber, String academicYear) {
        super(firstName, lastName, email, passwordHash, salt);
        this.studentCardNumber = studentCardNumber;
        this.academicYear = academicYear;
    }
    public Student(User user, String studentCardNumber, String academicYear) {
        super(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPasswordHash(), user.getSalt());
        this.studentCardNumber = studentCardNumber;
        this.academicYear = academicYear;
    }
    public String getStudentCardNumber() {
        return studentCardNumber;
    }
    public void setStudentCardNumber(String studentCardNumber) {
        this.studentCardNumber = studentCardNumber;
    }
    public String getAcademicYear() {
        return academicYear;
    }
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    public String getRole() {
        return "STUDENT";
    }
}
