package com.app.model.users;

public class Student extends User {
    String studentCardNumber;
    String academicYear;
    public Student(int id, String username, String firstName, String lastName, String email, String passwordHash, String salt, String profilePicturePath, boolean isVerified,
                   String studentCardNumber, String academicYear) {
        super(id,username, firstName, lastName, email, passwordHash, salt, profilePicturePath, isVerified);
        this.studentCardNumber = studentCardNumber;
        this.academicYear = academicYear;
    }
    public Student(String username, String firstName, String lastName, String email, String passwordHash, String salt, String profilePicturePath, boolean isVerified,
                   String studentCardNumber, String academicYear) {
        super(username, firstName, lastName, email, passwordHash, salt, profilePicturePath, isVerified);
        this.studentCardNumber = studentCardNumber;
        this.academicYear = academicYear;
    }
    public Student(User user, String studentCardNumber, String academicYear) {
        super(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPasswordHash(), user.getSalt(), user.getProfilePicturePath(), user.isVerified());
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
    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", studentCardNumber='" + studentCardNumber + '\'' +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }
}
