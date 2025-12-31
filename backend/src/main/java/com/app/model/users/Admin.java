package com.app.model.users;

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
