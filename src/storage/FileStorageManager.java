package storage;

import model.*;
import java.io.*;
import java.util.*;

/**
 * Handles File I/O persistence for all entities.
 * Demonstrates File I/O operations and data serialization.
 */
public class FileStorageManager {
    private final String dataDir;

    private static final String STUDENTS_FILE = "students.txt";
    private static final String RECRUITERS_FILE = "recruiters.txt";
    private static final String ADMINS_FILE = "admins.txt";
    private static final String JOBS_FILE = "jobs.txt";
    private static final String APPLICATIONS_FILE = "applications.txt";
    private static final String NOTIFICATIONS_FILE = "notifications.txt";
    private static final String INTERVIEWS_FILE = "interviews.txt";

    public FileStorageManager(String dataDir) {
        this.dataDir = dataDir;
        new File(dataDir).mkdirs();
    }

    // ===== STUDENT PERSISTENCE =====
    public void saveStudents(Collection<Student> students) {
        writeLines(STUDENTS_FILE, students, Student::toFileString);
    }

    public Map<String, Student> loadStudents() {
        Map<String, Student> map = new HashMap<>();
        for (String line : readLines(STUDENTS_FILE)) {
            if (!line.trim().isEmpty()) {
                Student s = Student.fromFileString(line);
                if (s != null) map.put(s.getUserId(), s);
            }
        }
        return map;
    }

    // ===== RECRUITER PERSISTENCE =====
    public void saveRecruiters(Collection<Recruiter> recruiters) {
        writeLines(RECRUITERS_FILE, recruiters, Recruiter::toFileString);
    }

    public Map<String, Recruiter> loadRecruiters() {
        Map<String, Recruiter> map = new HashMap<>();
        for (String line : readLines(RECRUITERS_FILE)) {
            if (!line.trim().isEmpty()) {
                Recruiter r = Recruiter.fromFileString(line);
                if (r != null) map.put(r.getUserId(), r);
            }
        }
        return map;
    }

    // ===== ADMIN PERSISTENCE =====
    public void saveAdmins(Collection<Admin> admins) {
        writeLines(ADMINS_FILE, admins, Admin::toFileString);
    }

    public Map<String, Admin> loadAdmins() {
        Map<String, Admin> map = new HashMap<>();
        for (String line : readLines(ADMINS_FILE)) {
            if (!line.trim().isEmpty()) {
                Admin a = Admin.fromFileString(line);
                if (a != null) map.put(a.getUserId(), a);
            }
        }
        return map;
    }

    // ===== JOB PERSISTENCE =====
    public void saveJobs(Collection<Job> jobs) {
        writeLines(JOBS_FILE, jobs, Job::toFileString);
    }

    public List<Job> loadJobs() {
        List<Job> list = new ArrayList<>();
        for (String line : readLines(JOBS_FILE)) {
            if (!line.trim().isEmpty()) {
                Job j = Job.fromFileString(line);
                if (j != null) list.add(j);
            }
        }
        return list;
    }

    // ===== APPLICATION PERSISTENCE =====
    public void saveApplications(Collection<Application> applications) {
        writeLines(APPLICATIONS_FILE, applications, Application::toFileString);
    }

    public List<Application> loadApplications() {
        List<Application> list = new ArrayList<>();
        for (String line : readLines(APPLICATIONS_FILE)) {
            if (!line.trim().isEmpty()) {
                Application a = Application.fromFileString(line);
                if (a != null) list.add(a);
            }
        }
        return list;
    }

    // ===== NOTIFICATION PERSISTENCE =====
    public void saveNotifications(Collection<Notification> notifications) {
        writeLines(NOTIFICATIONS_FILE, notifications, Notification::toFileString);
    }

    public LinkedList<Notification> loadNotifications() {
        LinkedList<Notification> list = new LinkedList<>();
        for (String line : readLines(NOTIFICATIONS_FILE)) {
            if (!line.trim().isEmpty()) {
                Notification n = Notification.fromFileString(line);
                if (n != null) list.add(n);
            }
        }
        return list;
    }

    // ===== INTERVIEW PERSISTENCE =====
    public void saveInterviews(Collection<Interview> interviews) {
        writeLines(INTERVIEWS_FILE, interviews, Interview::toFileString);
    }

    public List<Interview> loadInterviews() {
        List<Interview> list = new ArrayList<>();
        for (String line : readLines(INTERVIEWS_FILE)) {
            if (!line.trim().isEmpty()) {
                Interview i = Interview.fromFileString(line);
                if (i != null) list.add(i);
            }
        }
        return list;
    }

    // ===== GENERIC I/O HELPERS =====
    private <T> void writeLines(String filename, Collection<T> items, java.util.function.Function<T, String> serializer) {
        String path = dataDir + File.separator + filename;
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (T item : items) {
                writer.println(serializer.apply(item));
            }
        } catch (IOException e) {
            System.err.println("Error saving " + filename + ": " + e.getMessage());
        }
    }

    private List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        String path = dataDir + File.separator + filename;
        File f = new File(path);
        if (!f.exists()) return lines;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + filename + ": " + e.getMessage());
        }
        return lines;
    }
}
