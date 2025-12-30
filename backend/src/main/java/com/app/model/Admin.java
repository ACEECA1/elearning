package com.app.model;

public class Admin extends User {
    public Admin(int id, String firstName, String lastName, String email, String passwordHash, String salt) {
        super(id, firstName, lastName, email, passwordHash, salt);
    }
    public Admin(String firstName, String lastName, String email, String passwordHash, String salt) {
        super(firstName, lastName, email, passwordHash, salt);
    }
    public Admin(User user) {
        super(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPasswordHash(), user.getSalt());
    }
    public String getRole() {
        return "ADMIN";
    }
}
