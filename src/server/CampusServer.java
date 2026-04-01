package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import exceptions.DuplicateApplicationException;
import exceptions.InvalidLoginException;
import exceptions.ProfileIncompleteException;
import model.*;
import service.*;
import storage.FileStorageManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Embedded HTTP server using com.sun.net.httpserver.
 * Routes all API calls and serves static frontend files.
 */
public class CampusServer {
    private final int port;
    private HttpServer server;

    // Services
    private final FileStorageManager storage;
    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final InterviewService interviewService;
    private final CareerGuidanceEngine guidanceEngine;

    private final String frontendDir;

    public CampusServer(int port, String dataDir, String frontendDir) {
        this.port = port;
        this.frontendDir = frontendDir;
        this.storage = new FileStorageManager(dataDir);
        this.authService = new AuthService(storage);
        this.jobService = new JobService(storage);
        this.applicationService = new ApplicationService(storage);
        this.notificationService = new NotificationService(storage);
        this.interviewService = new InterviewService(storage);
        this.guidanceEngine = new CareerGuidanceEngine(jobService);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/logout", new LogoutHandler());
        server.createContext("/api/register/student", new RegisterStudentHandler());
        server.createContext("/api/register/recruiter", new RegisterRecruiterHandler());
        server.createContext("/api/jobs", new JobsHandler());
        server.createContext("/api/jobs/post", new PostJobHandler());
        server.createContext("/api/jobs/close", new CloseJobHandler());
        server.createContext("/api/apply", new ApplyHandler());
        server.createContext("/api/applications", new ApplicationsHandler());
        server.createContext("/api/applications/update", new UpdateApplicationHandler());
        server.createContext("/api/profile/student", new StudentProfileHandler());
        server.createContext("/api/profile/recruiter", new RecruiterProfileHandler());
        server.createContext("/api/notifications", new NotificationsHandler());
        server.createContext("/api/notifications/read", new MarkReadHandler());
        server.createContext("/api/interview/schedule", new ScheduleInterviewHandler());
        server.createContext("/api/interviews", new InterviewsHandler());
        server.createContext("/api/guidance", new GuidanceHandler());
        server.createContext("/api/admin/stats", new AdminStatsHandler());
        server.createContext("/api/admin/approve", new AdminApproveHandler());
        server.createContext("/api/admin/broadcast", new AdminBroadcastHandler());
        server.createContext("/api/admin/users", new AdminUsersHandler());

        // Static file server
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("==============================================");
        System.out.println("  Campus Connect Server started on port " + port);
        System.out.println("  Open: http://localhost:" + port);
        System.out.println("==============================================");
    }

    public void stop() { server.stop(0); }

    // ===== UTILITY METHODS =====

    private String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("\"", "");
                String val = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, val);
            }
        }
        return map;
    }

    private String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private void handleOptions(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(204, -1);
    }

    private String getToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return getQueryParam(ex.getRequestURI().getQuery(), "token");
    }

    private String err(String msg) { return "{\"error\":\"" + msg + "\"}"; }
    private String ok(String msg) { return "{\"success\":true,\"message\":\"" + msg + "\"}"; }

    private String jsonEscape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    // ===== HANDLERS =====

    class LoginHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> data = parseJson(body);
            try {
                Map<String, Object> result = authService.login(data.get("email"), data.get("password"));
                StringBuilder sb = new StringBuilder("{");
                sb.append("\"token\":\"").append(result.get("token")).append("\",");
                sb.append("\"userId\":\"").append(result.get("userId")).append("\",");
                sb.append("\"name\":\"").append(result.get("name")).append("\",");
                sb.append("\"role\":\"").append(result.get("role")).append("\"");
                sb.append("}");
                sendJson(ex, 200, sb.toString());
            } catch (InvalidLoginException e) {
                sendJson(ex, 401, err(e.getMessage()));
            }
        }
    }

    class LogoutHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String token = getToken(ex);
            if (token != null) authService.logout(token);
            sendJson(ex, 200, ok("Logged out"));
        }
    }

    class RegisterStudentHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            int year = 1;
            double cgpa = 0.0;
            try { year = Integer.parseInt(d.getOrDefault("year", "1")); } catch (Exception ignored) {}
            try { cgpa = Double.parseDouble(d.getOrDefault("cgpa", "0")); } catch (Exception ignored) {}
            String result = authService.registerStudent(d.get("name"), d.get("email"),
                    d.get("password"), d.getOrDefault("department", "CS"),
                    year, cgpa, d.getOrDefault("phone", ""), d.getOrDefault("college", ""));
            if (result.startsWith("ok:")) {
                sendJson(ex, 200, "{\"success\":true,\"userId\":\"" + result.substring(3) + "\"}");
            } else {
                sendJson(ex, 400, err(result.substring(6)));
            }
        }
    }

    class RegisterRecruiterHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            String result = authService.registerRecruiter(d.get("name"), d.get("email"),
                    d.get("password"), d.getOrDefault("companyName", ""),
                    d.getOrDefault("industry", ""), d.getOrDefault("phone", ""));
            if (result.startsWith("ok:")) {
                sendJson(ex, 200, "{\"success\":true,\"message\":\"Account pending admin approval.\"}");
            } else {
                sendJson(ex, 400, err(result.substring(6)));
            }
        }
    }

    class JobsHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            Map<String, String> filters = new HashMap<>();
            if (query != null) {
                String skill = getQueryParam(query, "skill");
                String company = getQueryParam(query, "company");
                String location = getQueryParam(query, "location");
                String jobType = getQueryParam(query, "jobType");
                String minCtc = getQueryParam(query, "minCtc");
                String recruiterId = getQueryParam(query, "recruiterId");
                if (skill != null) filters.put("skill", skill);
                if (company != null) filters.put("company", company);
                if (location != null) filters.put("location", location);
                if (jobType != null) filters.put("jobType", jobType);
                if (minCtc != null) filters.put("minCtc", minCtc);

                if (recruiterId != null) {
                    List<Job> jobs = jobService.getJobsByRecruiter(recruiterId);
                    sendJson(ex, 200, jobListToJson(jobs));
                    return;
                }
            }
            List<Job> jobs = filters.isEmpty() ? jobService.getAllOpenJobs() : jobService.searchJobs(filters);
            sendJson(ex, 200, jobListToJson(jobs));
        }
    }

    class PostJobHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String token = getToken(ex);
            User user = authService.getUserByToken(token);
            if (user == null || !"RECRUITER".equals(user.getRole())) {
                sendJson(ex, 403, err("Unauthorized")); return;
            }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            double ctcMin = 0, ctcMax = 0, minCgpa = 0;
            int minYear = 1;
            try { ctcMin = Double.parseDouble(d.getOrDefault("ctcMin","0")); } catch(Exception ignored){}
            try { ctcMax = Double.parseDouble(d.getOrDefault("ctcMax","0")); } catch(Exception ignored){}
            try { minCgpa = Double.parseDouble(d.getOrDefault("minCgpa","0")); } catch(Exception ignored){}
            try { minYear = Integer.parseInt(d.getOrDefault("minYear","1")); } catch(Exception ignored){}

            String skillsRaw = d.getOrDefault("skills", "");
            List<String> skills = new ArrayList<>();
            if (!skillsRaw.isEmpty()) {
                skillsRaw = skillsRaw.replaceAll("[\\[\\]\"]", "");
                for (String s : skillsRaw.split(",")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) skills.add(trimmed);
                }
            }

            Recruiter recruiter = authService.getRecruiterById(user.getUserId());
            String company = recruiter != null ? recruiter.getCompanyName() : d.getOrDefault("companyName","");

            Job job = jobService.postJob(user.getUserId(), company,
                    d.getOrDefault("title",""), d.getOrDefault("description",""),
                    skills, ctcMin, ctcMax, minCgpa, minYear,
                    d.getOrDefault("location",""), d.getOrDefault("jobType","FULL_TIME"),
                    d.getOrDefault("deadline",""));

            // Notify all students
            for (Student s : authService.getStudents().values()) {
                notificationService.sendNotification(s.getUserId(),
                        "New job posted: " + job.getTitle() + " at " + company, "JOB_POSTED");
            }
            sendJson(ex, 200, "{\"success\":true,\"jobId\":\"" + job.getJobId() + "\"}");
        }
    }

    class CloseJobHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            jobService.closeJob(d.get("jobId"));
            sendJson(ex, 200, ok("Job closed"));
        }
    }

    class ApplyHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String token = getToken(ex);
            User user = authService.getUserByToken(token);
            if (user == null || !"STUDENT".equals(user.getRole())) {
                sendJson(ex, 403, err("Unauthorized")); return;
            }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            Student student = authService.getStudentById(user.getUserId());
            Job job = jobService.getJobById(d.get("jobId"));
            if (student == null || job == null) {
                sendJson(ex, 400, err("Student or job not found")); return;
            }
            try {
                Application app = applicationService.applyForJob(student, job, d.getOrDefault("coverLetter",""));
                authService.updateStudent(student);
                Job updatedJob = jobService.getJobById(job.getJobId());
                if (updatedJob != null) updatedJob.addApplicationId(app.getApplicationId());
                jobService.updateJob(job);
                // Notify recruiter
                Recruiter rec = authService.getRecruiterById(job.getRecruiterId());
                if (rec != null) {
                    notificationService.sendNotification(rec.getUserId(),
                            student.getName() + " applied for " + job.getTitle(), "STATUS_UPDATE");
                }
                sendJson(ex, 200, "{\"success\":true,\"applicationId\":\"" + app.getApplicationId() + "\"}");
            } catch (DuplicateApplicationException e) {
                sendJson(ex, 409, err("You have already applied for this job."));
            } catch (ProfileIncompleteException e) {
                sendJson(ex, 400, err("Profile incomplete: " + e.getMissingFieldsSummary()));
            }
        }
    }

    class ApplicationsHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String studentId = getQueryParam(query, "studentId");
            String jobId = getQueryParam(query, "jobId");
            List<Application> apps;
            if (studentId != null) apps = applicationService.getApplicationsByStudent(studentId);
            else if (jobId != null) apps = applicationService.getApplicationsByJob(jobId);
            else apps = applicationService.getAllApplications();
            sendJson(ex, 200, appListToJson(apps));
        }
    }

    class UpdateApplicationHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            String appId = d.get("applicationId");
            String statusStr = d.getOrDefault("status", "APPLIED");
            String note = d.getOrDefault("note", "");
            Application.Status status;
            try { status = Application.Status.valueOf(statusStr); } catch (Exception e) { status = Application.Status.APPLIED; }
            boolean ok = applicationService.updateApplicationStatus(appId, status, note);
            if (ok) {
                Application app = applicationService.getApplicationById(appId);
                if (app != null) {
                    notificationService.sendNotification(app.getStudentId(),
                            "Your application for " + app.getJobTitle() + " at " + app.getCompanyName()
                                    + " is now " + status.name(), "STATUS_UPDATE");
                }
                sendJson(ex, 200, ok("Status updated"));
            } else {
                sendJson(ex, 404, err("Application not found"));
            }
        }
    }

    class StudentProfileHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String studentId = getQueryParam(query, "studentId");

            if ("GET".equals(ex.getRequestMethod())) {
                if (studentId == null) { sendJson(ex, 400, err("studentId required")); return; }
                Student s = authService.getStudentById(studentId);
                if (s == null) { sendJson(ex, 404, err("Not found")); return; }
                sendJson(ex, 200, studentToJson(s));
            } else {
                String body = readBody(ex);
                Map<String, String> d = parseJson(body);
                String id = d.get("userId");
                Student s = authService.getStudentById(id);
                if (s == null) { sendJson(ex, 404, err("Not found")); return; }
                if (d.containsKey("resumeSummary")) s.setResumeSummary(d.get("resumeSummary"));
                if (d.containsKey("phone")) s.setPhone(d.get("phone"));
                if (d.containsKey("college")) s.setCollegeName(d.get("college"));
                if (d.containsKey("cgpa")) { try { s.setCgpa(Double.parseDouble(d.get("cgpa"))); } catch(Exception ignored){} }
                if (d.containsKey("skills")) {
                    String raw = d.get("skills").replaceAll("[\\[\\]\"]", "");
                    List<String> skills = new ArrayList<>();
                    for (String sk : raw.split(",")) { String t = sk.trim(); if (!t.isEmpty()) skills.add(t); }
                    s.setSkills(skills);
                }
                authService.updateStudent(s);
                sendJson(ex, 200, ok("Profile updated"));
            }
        }
    }

    class RecruiterProfileHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String recruiterId = getQueryParam(query, "recruiterId");
            if ("GET".equals(ex.getRequestMethod())) {
                if (recruiterId == null) { sendJson(ex, 400, err("recruiterId required")); return; }
                Recruiter r = authService.getRecruiterById(recruiterId);
                if (r == null) { sendJson(ex, 404, err("Not found")); return; }
                sendJson(ex, 200, recruiterToJson(r));
            } else {
                String body = readBody(ex);
                Map<String, String> d = parseJson(body);
                String id = d.get("userId");
                Recruiter r = authService.getRecruiterById(id);
                if (r == null) { sendJson(ex, 404, err("Not found")); return; }
                if (d.containsKey("companyDescription")) r.setCompanyDescription(d.get("companyDescription"));
                if (d.containsKey("website")) r.setWebsite(d.get("website"));
                if (d.containsKey("phone")) r.setPhone(d.get("phone"));
                authService.updateRecruiter(r);
                sendJson(ex, 200, ok("Profile updated"));
            }
        }
    }

    class NotificationsHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String userId = getQueryParam(query, "userId");
            if (userId == null) { sendJson(ex, 400, err("userId required")); return; }
            List<Notification> notifs = notificationService.getNotificationsForUser(userId);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < notifs.size(); i++) {
                sb.append(notifs.get(i).toJson());
                if (i < notifs.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        }
    }

    class MarkReadHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            String notifId = d.get("notificationId");
            String userId = d.get("userId");
            if (notifId != null) notificationService.markAsRead(notifId);
            else if (userId != null) notificationService.markAllAsRead(userId);
            sendJson(ex, 200, ok("Marked as read"));
        }
    }

    class ScheduleInterviewHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            Interview interview = interviewService.scheduleInterviewFull(
                    d.get("applicationId"), d.get("studentId"), d.get("jobId"),
                    d.get("recruiterId"), d.get("dateTime"), d.getOrDefault("venue","Online"),
                    d.getOrDefault("interviewType","ONLINE"),
                    d.getOrDefault("meetingLink",""), d.getOrDefault("notes",""));
            applicationService.setInterviewForApplication(d.get("applicationId"), interview.getInterviewId());
            // Notify student
            notificationService.sendNotification(d.get("studentId"),
                    "Interview scheduled on " + d.get("dateTime") + ". Venue: " + d.getOrDefault("venue","Online"),
                    "INTERVIEW_SCHEDULED");
            sendJson(ex, 200, "{\"success\":true,\"interviewId\":\"" + interview.getInterviewId() + "\"}");
        }
    }

    class InterviewsHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String studentId = getQueryParam(query, "studentId");
            String recruiterId = getQueryParam(query, "recruiterId");
            List<Interview> list;
            if (studentId != null) list = interviewService.getInterviewsByStudent(studentId);
            else if (recruiterId != null) list = interviewService.getInterviewsByRecruiter(recruiterId);
            else list = interviewService.getAllInterviews();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        }
    }

    class GuidanceHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String studentId = getQueryParam(query, "studentId");
            if (studentId == null) { sendJson(ex, 400, err("studentId required")); return; }
            Student s = authService.getStudentById(studentId);
            if (s == null) { sendJson(ex, 404, err("Student not found")); return; }
            String json = guidanceEngine.buildGuidanceJson(s);
            sendJson(ex, 200, json);
        }
    }

    class AdminStatsHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            int totalStudents = authService.getStudents().size();
            int totalRecruiters = authService.getRecruiters().size();
            int totalJobs = jobService.getTotalJobsCount();
            int openJobs = jobService.getOpenJobsCount();
            int totalApplications = applicationService.getTotalApplications();
            int placed = applicationService.getPlacedCount();
            int shortlisted = applicationService.getShortlistedCount();
            double avgCtc = jobService.getAverageCTC();
            double placementRate = totalStudents > 0 ? (placed * 100.0 / totalStudents) : 0;

            Map<String, Integer> byCompany = jobService.getJobsByCompany();
            StringBuilder companySb = new StringBuilder("[");
            int ci = 0;
            for (Map.Entry<String, Integer> e : byCompany.entrySet()) {
                companySb.append("{\"company\":\"").append(e.getKey()).append("\",\"jobs\":").append(e.getValue()).append("}");
                if (ci++ < byCompany.size() - 1) companySb.append(",");
            }
            companySb.append("]");

            String json = "{" +
                    "\"totalStudents\":" + totalStudents + "," +
                    "\"totalRecruiters\":" + totalRecruiters + "," +
                    "\"totalJobs\":" + totalJobs + "," +
                    "\"openJobs\":" + openJobs + "," +
                    "\"totalApplications\":" + totalApplications + "," +
                    "\"placed\":" + placed + "," +
                    "\"shortlisted\":" + shortlisted + "," +
                    "\"avgCtcLpa\":" + String.format("%.2f", avgCtc / 100000.0) + "," +
                    "\"placementRate\":" + String.format("%.1f", placementRate) + "," +
                    "\"jobsByCompany\":" + companySb +
                    "}";
            sendJson(ex, 200, json);
        }
    }

    class AdminApproveHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            String recruiterId = d.get("recruiterId");
            String action = d.getOrDefault("action", "approve");
            if ("approve".equals(action)) {
                authService.approveRecruiter(recruiterId);
                Recruiter r = authService.getRecruiterById(recruiterId);
                if (r != null) notificationService.sendNotification(recruiterId,
                        "Your recruiter account has been approved! You can now post jobs.", "APPROVAL");
                sendJson(ex, 200, ok("Recruiter approved"));
            } else {
                authService.rejectRecruiter(recruiterId);
                sendJson(ex, 200, ok("Recruiter rejected"));
            }
        }
    }

    class AdminBroadcastHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String body = readBody(ex);
            Map<String, String> d = parseJson(body);
            String message = d.get("message");
            String target = d.getOrDefault("target", "ALL");
            if ("STUDENT".equals(target)) {
                notificationService.sendBroadcastToUsers(message, authService.getStudents());
            } else if ("RECRUITER".equals(target)) {
                notificationService.sendBroadcastToUsers(message, authService.getRecruiters());
            } else {
                notificationService.sendBroadcastToUsers(message, authService.getStudents());
                notificationService.sendBroadcastToUsers(message, authService.getRecruiters());
            }
            sendJson(ex, 200, ok("Broadcast sent"));
        }
    }

    class AdminUsersHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String query = ex.getRequestURI().getQuery();
            String role = getQueryParam(query, "role");
            StringBuilder sb = new StringBuilder("{");
            if (role == null || "STUDENT".equals(role)) {
                sb.append("\"students\":[");
                List<Student> studs = new ArrayList<>(authService.getStudents().values());
                for (int i = 0; i < studs.size(); i++) {
                    sb.append(studentToJson(studs.get(i)));
                    if (i < studs.size() - 1) sb.append(",");
                }
                sb.append("]");
            }
            if (role == null) sb.append(",");
            if (role == null || "RECRUITER".equals(role)) {
                sb.append("\"recruiters\":[");
                List<Recruiter> recs = new ArrayList<>(authService.getRecruiters().values());
                for (int i = 0; i < recs.size(); i++) {
                    sb.append(recruiterToJson(recs.get(i)));
                    if (i < recs.size() - 1) sb.append(",");
                }
                sb.append("]");
            }
            sb.append("}");
            sendJson(ex, 200, sb.toString());
        }
    }

    class StaticFileHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { handleOptions(ex); return; }
            String path = ex.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) path = "/index.html";
            File file = new File(frontendDir + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File(frontendDir + "/index.html");
            }
            String contentType = getContentType(path);
            byte[] bytes = Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().add("Content-Type", contentType);
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // ===== JSON SERIALIZERS =====

    private String jobListToJson(List<Job> jobs) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < jobs.size(); i++) {
            sb.append(jobs.get(i).toJson());
            if (i < jobs.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String appListToJson(List<Application> apps) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < apps.size(); i++) {
            sb.append(apps.get(i).toJson());
            if (i < apps.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String studentToJson(Student s) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"userId\":\"").append(jsonEscape(s.getUserId())).append("\",");
        sb.append("\"name\":\"").append(jsonEscape(s.getName())).append("\",");
        sb.append("\"email\":\"").append(jsonEscape(s.getEmail())).append("\",");
        sb.append("\"department\":\"").append(jsonEscape(s.getDepartment())).append("\",");
        sb.append("\"year\":").append(s.getYear()).append(",");
        sb.append("\"cgpa\":").append(s.getCgpa()).append(",");
        sb.append("\"phone\":\"").append(jsonEscape(s.getPhone())).append("\",");
        sb.append("\"college\":\"").append(jsonEscape(s.getCollegeName())).append("\",");
        sb.append("\"resumeSummary\":\"").append(jsonEscape(s.getResumeSummary())).append("\",");
        sb.append("\"role\":\"STUDENT\",");
        sb.append("\"profileComplete\":").append(s.isProfileComplete()).append(",");
        sb.append("\"skills\":[");
        List<String> skills = s.getSkills();
        for (int i = 0; i < skills.size(); i++) {
            sb.append("\"").append(jsonEscape(skills.get(i))).append("\"");
            if (i < skills.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String recruiterToJson(Recruiter r) {
        return "{" +
            "\"userId\":\"" + jsonEscape(r.getUserId()) + "\"," +
            "\"name\":\"" + jsonEscape(r.getName()) + "\"," +
            "\"email\":\"" + jsonEscape(r.getEmail()) + "\"," +
            "\"companyName\":\"" + jsonEscape(r.getCompanyName()) + "\"," +
            "\"industry\":\"" + jsonEscape(r.getIndustry()) + "\"," +
            "\"phone\":\"" + jsonEscape(r.getPhone()) + "\"," +
            "\"website\":\"" + jsonEscape(r.getWebsite()) + "\"," +
            "\"companyDescription\":\"" + jsonEscape(r.getCompanyDescription()) + "\"," +
            "\"role\":\"RECRUITER\"," +
            "\"isApproved\":" + r.isApproved() +
            "}";
    }
}
