import server.CampusServer;
import storage.FileStorageManager;
import model.*;
import service.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Entry point for Campus Connect platform.
 * Seeds sample data and starts the HTTP server.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Determine paths (relative to where Main.java lives or project root)
        String baseDir = new File("").getAbsolutePath();
        String dataDir = baseDir + File.separator + "data";
        String frontendDir = baseDir + File.separator + "frontend";
        int port = 8080;

        System.out.println("Campus Connect - Intelligent Campus Placement Platform");
        System.out.println("Data directory: " + dataDir);
        System.out.println("Frontend directory: " + frontendDir);

        // Seed sample data if data directory is empty
        seedSampleData(dataDir);

        // Start server
        CampusServer server = new CampusServer(port, dataDir, frontendDir);
        server.start();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down Campus Connect...");
            server.stop();
        }));

                // Keep main thread alive
                if (System.console() == null) {
                        // Non-interactive stdin (e.g., IDE/run task). Keep server alive until interrupted.
                        try {
                                while (true) {
                                        Thread.sleep(1000);
                                }
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                } else {
                        System.out.println("Press ENTER to stop the server.");
                        System.in.read();
                        server.stop();
                        System.out.println("Server stopped.");
                }
    }

    private static void seedSampleData(String dataDir) {
        File studentsFile = new File(dataDir + File.separator + "students.txt");
        if (studentsFile.exists() && studentsFile.length() > 0) {
            System.out.println("Sample data already exists. Skipping seeding.");
            return;
        }

        System.out.println("Seeding sample data...");
        FileStorageManager storage = new FileStorageManager(dataDir);

        // === STUDENTS ===
        Map<String, Student> students = new HashMap<>();

        Student s1 = new Student("STU001", "Arjun Sharma", "arjun@student.edu", "pass123",
                "Computer Science", 4, 8.7);
        s1.setPhone("9876543210"); s1.setCollegeName("IIT Bombay");
        s1.setResumeSummary("Final year CS student with strong DSA and web dev skills.");
        s1.setSkills(Arrays.asList("Java", "Python", "React", "SQL", "Spring Boot"));
        students.put(s1.getUserId(), s1);

        Student s2 = new Student("STU002", "Priya Mehta", "priya@student.edu", "pass123",
                "Electronics", 3, 7.9);
        s2.setPhone("9765432109"); s2.setCollegeName("NIT Trichy");
        s2.setResumeSummary("Third year ECE student interested in embedded systems.");
        s2.setSkills(Arrays.asList("C", "C++", "MATLAB", "Embedded Systems", "Python"));
        students.put(s2.getUserId(), s2);

        Student s3 = new Student("STU003", "Rahul Verma", "rahul@student.edu", "pass123",
                "Information Technology", 4, 9.1);
        s3.setPhone("9654321098"); s3.setCollegeName("BITS Pilani");
        s3.setResumeSummary("Top-ranked IT student with full-stack and cloud expertise.");
        s3.setSkills(Arrays.asList("Java", "Kubernetes", "AWS", "React", "Node.js", "MongoDB"));
        students.put(s3.getUserId(), s3);

        Student s4 = new Student("STU004", "Anjali Singh", "anjali@student.edu", "pass123",
                "Computer Science", 2, 8.2);
        s4.setPhone("9543210987"); s4.setCollegeName("VIT Vellore");
        s4.setResumeSummary("Second year CS student passionate about machine learning.");
        s4.setSkills(Arrays.asList("Python", "TensorFlow", "NumPy", "Pandas"));
        students.put(s4.getUserId(), s4);

        Student s5 = new Student("STU005", "Karthik Nair", "karthik@student.edu", "pass123",
                "Mechanical Engineering", 4, 7.5);
        s5.setPhone("9432109876"); s5.setCollegeName("Anna University");
        s5.setResumeSummary("Mechanical engineer with CAD and simulation expertise.");
        s5.setSkills(Arrays.asList("AutoCAD", "ANSYS", "SolidWorks", "Python"));
        students.put(s5.getUserId(), s5);

        storage.saveStudents(students.values());

        // === RECRUITERS ===
        Map<String, Recruiter> recruiters = new HashMap<>();

        Recruiter r1 = new Recruiter("REC001", "Deepa Krishnan", "deepa@tcs.com", "pass123",
                "Tata Consultancy Services", "IT Services");
        r1.setApproved(true); r1.setPhone("8001112222");
        r1.setWebsite("https://www.tcs.com");
        r1.setCompanyDescription("India's largest IT services company, operating in 46 countries.");
        recruiters.put(r1.getUserId(), r1);

        Recruiter r2 = new Recruiter("REC002", "Suresh Rao", "suresh@infosys.com", "pass123",
                "Infosys", "IT Consulting");
        r2.setApproved(true); r2.setPhone("8002223333");
        r2.setWebsite("https://www.infosys.com");
        r2.setCompanyDescription("Global leader in next-generation digital services and consulting.");
        recruiters.put(r2.getUserId(), r2);

        Recruiter r3 = new Recruiter("REC003", "Nisha Patel", "nisha@wipro.com", "pass123",
                "Wipro", "IT Services");
        r3.setApproved(true); r3.setPhone("8003334444");
        r3.setWebsite("https://www.wipro.com");
        r3.setCompanyDescription("Technology company that delivers solutions across 6 continents.");
        recruiters.put(r3.getUserId(), r3);

        Recruiter r4 = new Recruiter("REC004", "Amit Joshi", "amit@amazon.com", "pass123",
                "Amazon", "E-Commerce & Cloud");
        r4.setApproved(true); r4.setPhone("8004445555");
        r4.setWebsite("https://www.amazon.com");
        r4.setCompanyDescription("Global technology and commerce leader.");
        recruiters.put(r4.getUserId(), r4);

        Recruiter r5 = new Recruiter("REC005", "Kavya Reddy", "kavya@pending.com", "pass123",
                "StartupXYZ", "FinTech");
        r5.setApproved(false); // Pending approval
        recruiters.put(r5.getUserId(), r5);

        storage.saveRecruiters(recruiters.values());

        // === ADMINS ===
        Map<String, Admin> admins = new HashMap<>();
        Admin admin = new Admin("admin001", "Campus Admin", "admin@campus.edu", "admin123");
        admins.put(admin.getUserId(), admin);
        storage.saveAdmins(admins.values());

        // === JOBS ===
        List<Job> jobs = new ArrayList<>();
        long now = System.currentTimeMillis();

        Job j1 = new Job("JOB001", "Software Engineer – Java", "Build enterprise applications using Java and Spring Boot.",
                "Tata Consultancy Services", "REC001", 600000, 900000, 7.0, 3, "Mumbai", "FULL_TIME");
        j1.setRequiredSkills(Arrays.asList("Java", "Spring Boot", "SQL", "REST APIs"));
        j1.setDeadline("2025-06-30"); j1.setPostedDate(now - 86400000L * 5);
        jobs.add(j1);

        Job j2 = new Job("JOB002", "Data Analyst", "Analyze large datasets and build dashboards.",
                "Infosys", "REC002", 500000, 750000, 7.5, 3, "Bangalore", "FULL_TIME");
        j2.setRequiredSkills(Arrays.asList("Python", "SQL", "Pandas", "Power BI"));
        j2.setDeadline("2025-07-15"); j2.setPostedDate(now - 86400000L * 3);
        jobs.add(j2);

        Job j3 = new Job("JOB003", "Full Stack Developer Intern", "Build features for our internal tools.",
                "Wipro", "REC003", 25000, 35000, 7.0, 2, "Hyderabad", "INTERNSHIP");
        j3.setRequiredSkills(Arrays.asList("React", "Node.js", "MongoDB", "JavaScript"));
        j3.setDeadline("2025-05-31"); j3.setPostedDate(now - 86400000L * 1);
        jobs.add(j3);

        Job j4 = new Job("JOB004", "SDE-1 – Cloud Engineering", "Work on AWS infrastructure and microservices.",
                "Amazon", "REC004", 1500000, 2200000, 8.0, 4, "Bangalore", "FULL_TIME");
        j4.setRequiredSkills(Arrays.asList("Java", "AWS", "Kubernetes", "Distributed Systems"));
        j4.setDeadline("2025-06-15"); j4.setPostedDate(now - 86400000L * 7);
        jobs.add(j4);

        Job j5 = new Job("JOB005", "Machine Learning Engineer", "Develop and deploy ML models at scale.",
                "Infosys", "REC002", 900000, 1300000, 8.0, 4, "Pune", "FULL_TIME");
        j5.setRequiredSkills(Arrays.asList("Python", "TensorFlow", "PyTorch", "MLOps", "SQL"));
        j5.setDeadline("2025-07-01"); j5.setPostedDate(now - 86400000L * 2);
        jobs.add(j5);

        Job j6 = new Job("JOB006", "DevOps Engineer", "Manage CI/CD pipelines and cloud infrastructure.",
                "Tata Consultancy Services", "REC001", 800000, 1100000, 7.5, 3, "Chennai", "FULL_TIME");
        j6.setRequiredSkills(Arrays.asList("Docker", "Kubernetes", "Jenkins", "AWS", "Linux"));
        j6.setDeadline("2025-06-20"); j6.setPostedDate(now - 86400000L * 4);
        jobs.add(j6);

        storage.saveJobs(jobs);

        // === APPLICATIONS ===
        List<Application> apps = new ArrayList<>();

        Application a1 = new Application("APP001", "STU001", "JOB001", "Tata Consultancy Services", "Software Engineer – Java");
        a1.setStatus(Application.Status.SHORTLISTED);
        a1.setCoverLetter("I have 2 years of Java experience and love backend development.");
        apps.add(a1);

        Application a2 = new Application("APP002", "STU001", "JOB004", "Amazon", "SDE-1 – Cloud Engineering");
        a2.setStatus(Application.Status.APPLIED);
        apps.add(a2);

        Application a3 = new Application("APP003", "STU003", "JOB004", "Amazon", "SDE-1 – Cloud Engineering");
        a3.setStatus(Application.Status.INTERVIEW_SCHEDULED);
        apps.add(a3);

        Application a4 = new Application("APP004", "STU002", "JOB003", "Wipro", "Full Stack Developer Intern");
        a4.setStatus(Application.Status.REJECTED);
        a4.setRecruiterNote("Skills don't match the current requirement.");
        apps.add(a4);

        Application a5 = new Application("APP005", "STU004", "JOB005", "Infosys", "Machine Learning Engineer");
        a5.setStatus(Application.Status.SELECTED);
        apps.add(a5);

        storage.saveApplications(apps);

        // === NOTIFICATIONS ===
        LinkedList<model.Notification> notifs = new LinkedList<>();

        notifs.add(new model.Notification("NOTIF001", "STU001",
                "Your application for Software Engineer at TCS has been shortlisted!", model.Notification.Type.STATUS_UPDATE));
        notifs.add(new model.Notification("NOTIF002", "STU003",
                "Interview scheduled for Amazon SDE-1 role on 2025-05-20 10:00 AM.", model.Notification.Type.INTERVIEW_SCHEDULED));
        notifs.add(new model.Notification("NOTIF003", "STU004",
                "Congratulations! You have been selected for ML Engineer at Infosys.", model.Notification.Type.STATUS_UPDATE));
        notifs.add(new model.Notification("NOTIF004", "REC001",
                "Arjun Sharma applied for Software Engineer – Java.", model.Notification.Type.STATUS_UPDATE));

        storage.saveNotifications(notifs);

        // === INTERVIEWS ===
        List<Interview> interviews = new ArrayList<>();
        Interview int1 = new Interview("INT001", "APP003", "STU003", "JOB004", "REC004",
                "2025-05-20T10:00", "Online - Google Meet", Interview.InterviewType.ONLINE);
        int1.setMeetingLink("https://meet.google.com/abc-xyz-123");
        int1.setNotes("Round 1: DSA + System Design");
        interviews.add(int1);
        storage.saveInterviews(interviews);

        System.out.println("✓ Sample data seeded successfully!");
        System.out.println("  Students: " + students.size());
        System.out.println("  Recruiters: " + recruiters.size());
        System.out.println("  Jobs: " + jobs.size());
        System.out.println("  Applications: " + apps.size());
        System.out.println();
        System.out.println("Demo Credentials:");
        System.out.println("  Student  → arjun@student.edu / pass123");
        System.out.println("  Recruiter→ deepa@tcs.com / pass123");
        System.out.println("  Admin    → admin@campus.edu / admin123");
    }
}
