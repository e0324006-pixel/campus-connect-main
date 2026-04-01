package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class demonstrating ABSTRACTION and ENCAPSULATION.
 * All users share common fields but implement getDashboard() differently (POLYMORPHISM).
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: private fields with getters/setters
    private String userId;
    private String name;
    private String email;
    private String password;
    private String role; // "STUDENT", "RECRUITER", "ADMIN"
    private boolean isApproved;
    private List<String> notificationIds;

    public User(String userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isApproved = role.equals("ADMIN") || role.equals("STUDENT"); // Recruiters need approval
        this.notificationIds = new ArrayList<>();
    }

    // ABSTRACTION: subclasses must implement this
    public abstract String getDashboard();

    // Getters and Setters (ENCAPSULATION)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public List<String> getNotificationIds() { return notificationIds; }
    public void addNotificationId(String id) { this.notificationIds.add(id); }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public String toFileString() {
        return userId + "|" + name + "|" + email + "|" + password + "|" + role + "|" + isApproved;
    }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', name='" + name + "', role='" + role + "'}";
    }
}
