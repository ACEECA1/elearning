package com.app.model.users;

public class Admin extends User {
    public Admin(int id, String username, String firstName, String lastName, String email, String passwordHash, String salt, boolean isVerified) {
        super(id, username, firstName, lastName, email, passwordHash, salt, isVerified);
    }
    public Admin(String username, String firstName, String lastName, String email, String passwordHash, String salt, boolean isVerified) {
        super(username, firstName, lastName, email, passwordHash, salt, isVerified);
    }
    public Admin(User user) {
        super(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPasswordHash(), user.getSalt(), user.isVerified());
    }
    public String getRole() {
        return "ADMIN";
    }
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
