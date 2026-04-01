package service;

import exceptions.InvalidLoginException;
import model.*;
import storage.FileStorageManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles authentication, registration, and session management.
 */
public class AuthService {
    private Map<String, Student> students;
    private Map<String, Recruiter> recruiters;
    private Map<String, Admin> admins;
    private Map<String, User> activeSessions; // token -> user
    private final FileStorageManager storage;

    public AuthService(FileStorageManager storage) {
        this.storage = storage;
        this.activeSessions = new HashMap<>();
        loadData();
    }

    private void loadData() {
        students = storage.loadStudents();
        recruiters = storage.loadRecruiters();
        admins = storage.loadAdmins();
        // Seed default admin if none exist
        if (admins.isEmpty()) {
            Admin defaultAdmin = new Admin("admin001", "Admin User", "admin@campus.edu", "admin123");
            admins.put(defaultAdmin.getUserId(), defaultAdmin);
            storage.saveAdmins(admins.values());
        }
    }

    /**
     * Authenticate a user by email and password.
     * POLYMORPHISM: returns different User subtypes.
     */
    public Map<String, Object> login(String email, String password) throws InvalidLoginException {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new InvalidLoginException("Email and password are required.");
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new InvalidLoginException("No account found with email: " + email, email);
        }
        if (!user.checkPassword(password)) {
            throw new InvalidLoginException("Incorrect password for: " + email, email);
        }
        if (!user.isApproved()) {
            throw new InvalidLoginException("Account pending approval by admin.");
        }

        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        result.put("role", user.getRole());
        result.put("dashboard", user.getDashboard()); // POLYMORPHISM
        return result;
    }

    public void logout(String token) {
        activeSessions.remove(token);
    }

    public User getUserByToken(String token) {
        return activeSessions.get(token);
    }

    public String registerStudent(String name, String email, String password, String department,
                                   int year, double cgpa, String phone, String college) {
        if (findUserByEmail(email) != null) {
            return "error:Email already registered.";
        }
        if (!isValidPassword(password)) {
            return "error:Password must be at least 6 characters.";
        }
        String id = "STU" + System.currentTimeMillis();
        Student s = new Student(id, name, email, password, department, year, cgpa);
        s.setPhone(phone);
        s.setCollegeName(college);
        students.put(id, s);
        storage.saveStudents(students.values());
        return "ok:" + id;
    }

    public String registerRecruiter(String name, String email, String password,
                                     String companyName, String industry, String phone) {
        if (findUserByEmail(email) != null) {
            return "error:Email already registered.";
        }
        if (!isValidPassword(password)) {
            return "error:Password must be at least 6 characters.";
        }
        String id = "REC" + System.currentTimeMillis();
        Recruiter r = new Recruiter(id, name, email, password, companyName, industry);
        r.setPhone(phone);
        recruiters.put(id, r);
        storage.saveRecruiters(recruiters.values());
        return "ok:" + id;
    }

    private User findUserByEmail(String email) {
        for (Student s : students.values()) {
            if (s.getEmail().equalsIgnoreCase(email)) return s;
        }
        for (Recruiter r : recruiters.values()) {
            if (r.getEmail().equalsIgnoreCase(email)) return r;
        }
        for (Admin a : admins.values()) {
            if (a.getEmail().equalsIgnoreCase(email)) return a;
        }
        return null;
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public Map<String, Student> getStudents() { return students; }
    public Map<String, Recruiter> getRecruiters() { return recruiters; }
    public Map<String, Admin> getAdmins() { return admins; }

    public Student getStudentById(String id) { return students.get(id); }
    public Recruiter getRecruiterById(String id) { return recruiters.get(id); }

    public void updateStudent(Student s) {
        students.put(s.getUserId(), s);
        storage.saveStudents(students.values());
    }

    public void updateRecruiter(Recruiter r) {
        recruiters.put(r.getUserId(), r);
        storage.saveRecruiters(recruiters.values());
    }

    public void approveRecruiter(String recruiterId) {
        Recruiter r = recruiters.get(recruiterId);
        if (r != null) {
            r.setApproved(true);
            storage.saveRecruiters(recruiters.values());
        }
    }

    public void rejectRecruiter(String recruiterId) {
        recruiters.remove(recruiterId);
        storage.saveRecruiters(recruiters.values());
    }
}
