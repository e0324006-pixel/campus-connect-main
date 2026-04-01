package service;

import exceptions.DuplicateApplicationException;
import exceptions.ProfileIncompleteException;
import model.Application;
import model.Job;
import model.Student;
import storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages job applications with custom exception handling.
 */
public class ApplicationService {
    private List<Application> applications;
    private final FileStorageManager storage;

    public ApplicationService(FileStorageManager storage) {
        this.storage = storage;
        this.applications = storage.loadApplications();
    }

    /**
     * Apply for a job - checks for duplicates and profile completeness.
     * Throws custom exceptions for error cases.
     */
    public Application applyForJob(Student student, Job job, String coverLetter)
            throws DuplicateApplicationException, ProfileIncompleteException {

        // EXCEPTION HANDLING: Check profile completeness
        if (!student.isProfileComplete()) {
            List<String> missing = getMissingProfileFields(student);
            throw new ProfileIncompleteException(
                    "Please complete your profile before applying.", missing);
        }

        // EXCEPTION HANDLING: Check for duplicate application
        for (Application a : applications) {
            if (a.getStudentId().equals(student.getUserId())
                    && a.getJobId().equals(job.getJobId())) {
                throw new DuplicateApplicationException(
                        "You have already applied for this job.",
                        student.getUserId(), job.getJobId(), a.getApplicationId());
            }
        }

        String appId = "APP" + System.currentTimeMillis();
        Application app = new Application(appId, student.getUserId(), job.getJobId(),
                job.getCompanyName(), job.getTitle());
        app.setCoverLetter(coverLetter);

        applications.add(app);
        student.addApplicationId(appId);
        job.addApplicationId(appId);
        storage.saveApplications(applications);

        return app;
    }

    private List<String> getMissingProfileFields(Student s) {
        List<String> missing = new ArrayList<>();
        if (s.getPhone() == null || s.getPhone().isEmpty()) missing.add("Phone Number");
        if (s.getCollegeName() == null || s.getCollegeName().isEmpty()) missing.add("College Name");
        if (s.getResumeSummary() == null || s.getResumeSummary().isEmpty()) missing.add("Resume Summary");
        if (s.getSkills() == null || s.getSkills().isEmpty()) missing.add("Skills");
        if (s.getCgpa() <= 0) missing.add("CGPA");
        return missing;
    }

    public Application getApplicationById(String id) {
        for (Application a : applications) {
            if (a.getApplicationId().equals(id)) return a;
        }
        return null;
    }

    public List<Application> getApplicationsByStudent(String studentId) {
        List<Application> result = new ArrayList<>();
        for (Application a : applications) {
            if (a.getStudentId().equals(studentId)) result.add(a);
        }
        return result;
    }

    public List<Application> getApplicationsByJob(String jobId) {
        List<Application> result = new ArrayList<>();
        for (Application a : applications) {
            if (a.getJobId().equals(jobId)) result.add(a);
        }
        return result;
    }

    public boolean updateApplicationStatus(String applicationId, Application.Status newStatus,
                                            String recruiterNote) {
        for (Application a : applications) {
            if (a.getApplicationId().equals(applicationId)) {
                a.setStatus(newStatus);
                if (recruiterNote != null && !recruiterNote.isEmpty()) {
                    a.setRecruiterNote(recruiterNote);
                }
                storage.saveApplications(applications);
                return true;
            }
        }
        return false;
    }

    public boolean setInterviewForApplication(String applicationId, String interviewId) {
        for (Application a : applications) {
            if (a.getApplicationId().equals(applicationId)) {
                a.setInterviewId(interviewId);
                a.setStatus(Application.Status.INTERVIEW_SCHEDULED);
                storage.saveApplications(applications);
                return true;
            }
        }
        return false;
    }

    public List<Application> getAllApplications() { return applications; }

    // Statistics
    public int getTotalApplications() { return applications.size(); }

    public int getPlacedCount() {
        int count = 0;
        for (Application a : applications) {
            if (a.getStatus() == Application.Status.SELECTED) count++;
        }
        return count;
    }

    public int getShortlistedCount() {
        int count = 0;
        for (Application a : applications) {
            if (a.getStatus() == Application.Status.SHORTLISTED
                    || a.getStatus() == Application.Status.INTERVIEW_SCHEDULED) count++;
        }
        return count;
    }
}
