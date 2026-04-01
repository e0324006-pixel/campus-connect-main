package service;

import interfaces.Searchable;
import model.Job;
import model.Student;
import storage.FileStorageManager;

import java.util.*;

/**
 * Manages job listings - IMPLEMENTS Searchable interface.
 */
public class JobService implements Searchable {
    private List<Job> jobs;           // ArrayList for ordered job listings
    private final FileStorageManager storage;

    public JobService(FileStorageManager storage) {
        this.storage = storage;
        this.jobs = storage.loadJobs();
    }

    public Job postJob(String recruiterId, String companyName, String title, String description,
                       List<String> skills, double ctcMin, double ctcMax, double minCgpa,
                       int minYear, String location, String jobType, String deadline) {
        String jobId = "JOB" + System.currentTimeMillis();
        Job job = new Job(jobId, title, description, companyName, recruiterId,
                ctcMin, ctcMax, minCgpa, minYear, location, jobType);
        job.setRequiredSkills(skills);
        job.setDeadline(deadline);
        jobs.add(job);
        storage.saveJobs(jobs);
        return job;
    }

    public Job getJobById(String jobId) {
        for (Job j : jobs) {
            if (j.getJobId().equals(jobId)) return j;
        }
        return null;
    }

    public List<Job> getAllOpenJobs() {
        List<Job> open = new ArrayList<>();
        for (Job j : jobs) {
            if ("OPEN".equals(j.getStatus())) open.add(j);
        }
        return open;
    }

    public List<Job> getJobsByRecruiter(String recruiterId) {
        List<Job> result = new ArrayList<>();
        for (Job j : jobs) {
            if (j.getRecruiterId().equals(recruiterId)) result.add(j);
        }
        return result;
    }

    public void closeJob(String jobId) {
        Job j = getJobById(jobId);
        if (j != null) {
            j.setStatus("CLOSED");
            storage.saveJobs(jobs);
        }
    }

    public void deleteJob(String jobId) {
        jobs.removeIf(j -> j.getJobId().equals(jobId));
        storage.saveJobs(jobs);
    }

    public void updateJob(Job job) {
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getJobId().equals(job.getJobId())) {
                jobs.set(i, job);
                storage.saveJobs(jobs);
                return;
            }
        }
    }

    /** POLYMORPHISM via Searchable: search jobs by multiple filters */
    @Override
    public List<Job> searchJobs(Map<String, String> filters) {
        List<Job> result = new ArrayList<>();
        for (Job j : jobs) {
            if (!"OPEN".equals(j.getStatus())) continue;
            if (filters.containsKey("skill")) {
                String skill = filters.get("skill").toLowerCase();
                boolean hasSkill = false;
                for (String s : j.getRequiredSkills()) {
                    if (s.toLowerCase().contains(skill)) { hasSkill = true; break; }
                }
                if (!hasSkill) continue;
            }
            if (filters.containsKey("company")) {
                if (!j.getCompanyName().toLowerCase().contains(filters.get("company").toLowerCase())) continue;
            }
            if (filters.containsKey("location")) {
                if (!j.getLocation().toLowerCase().contains(filters.get("location").toLowerCase())) continue;
            }
            if (filters.containsKey("minCtc")) {
                try {
                    double minCtc = Double.parseDouble(filters.get("minCtc"));
                    if (j.getCtcMin() < minCtc) continue;
                } catch (NumberFormatException ignored) {}
            }
            if (filters.containsKey("jobType")) {
                if (!j.getJobType().equalsIgnoreCase(filters.get("jobType"))) continue;
            }
            result.add(j);
        }
        return result;
    }

    @Override
    public List<Student> searchStudents(Map<String, String> filters) {
        // Delegated to AuthService context; here returns empty
        return new ArrayList<>();
    }

    public List<Job> getAllJobs() { return jobs; }

    // Placement statistics
    public int getTotalJobsCount() { return jobs.size(); }
    public int getOpenJobsCount() { return (int) jobs.stream().filter(j -> "OPEN".equals(j.getStatus())).count(); }

    public double getAverageCTC() {
        if (jobs.isEmpty()) return 0;
        return jobs.stream().mapToDouble(j -> (j.getCtcMin() + j.getCtcMax()) / 2).average().orElse(0);
    }

    public Map<String, Integer> getJobsByCompany() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Job j : jobs) {
            map.merge(j.getCompanyName(), 1, Integer::sum);
        }
        return map;
    }
}
