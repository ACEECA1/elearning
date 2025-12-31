package com.app.model.users;

public class Teacher extends User {
    private String domain;
    private String grade;
    public Teacher(int id, String firstName, String lastName, String email, String passwordHash, String salt,
                   String domain, String grade) {
        super(id, firstName, lastName, email, passwordHash, salt);
        this.domain = domain;
        this.grade = grade;
    }
    public Teacher(String firstName, String lastName, String email, String passwordHash, String salt,
                   String domain, String grade) {
        super(firstName, lastName, email, passwordHash, salt);
        this.domain = domain;
        this.grade = grade;
    }
    public Teacher(User user, String domain, String grade) {
        super(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPasswordHash(), user.getSalt());
        this.domain = domain;
        this.grade = grade;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRole() {
        return "TEACHER";
    }
    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", domain='" + domain + '\'' +
                ", grade='" + grade + '\'' +
                '}';
    }
}
