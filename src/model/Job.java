package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private String companyName;
    private String recruiterId;
    private List<String> requiredSkills;
    private double ctcMin;
    private double ctcMax;
    private double minCgpa;
    private int minYear;
    private String location;
    private String jobType; // FULL_TIME, INTERNSHIP, PART_TIME
    private String status;  // OPEN, CLOSED
    private long postedDate;
    private String deadline;
    private List<String> applicationIds;

    public Job(String jobId, String title, String description, String companyName,
               String recruiterId, double ctcMin, double ctcMax, double minCgpa,
               int minYear, String location, String jobType) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.companyName = companyName;
        this.recruiterId = recruiterId;
        this.ctcMin = ctcMin;
        this.ctcMax = ctcMax;
        this.minCgpa = minCgpa;
        this.minYear = minYear;
        this.location = location;
        this.jobType = jobType;
        this.status = "OPEN";
        this.postedDate = System.currentTimeMillis();
        this.deadline = "";
        this.requiredSkills = new ArrayList<>();
        this.applicationIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public void addRequiredSkill(String skill) { this.requiredSkills.add(skill); }

    public double getCtcMin() { return ctcMin; }
    public void setCtcMin(double ctcMin) { this.ctcMin = ctcMin; }

    public double getCtcMax() { return ctcMax; }
    public void setCtcMax(double ctcMax) { this.ctcMax = ctcMax; }

    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }

    public int getMinYear() { return minYear; }
    public void setMinYear(int minYear) { this.minYear = minYear; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getPostedDate() { return postedDate; }
    public void setPostedDate(long postedDate) { this.postedDate = postedDate; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public List<String> getApplicationIds() { return applicationIds; }
    public void addApplicationId(String id) { this.applicationIds.add(id); }
    public void removeApplicationId(String id) { this.applicationIds.remove(id); }

    public String toFileString() {
        String skillsStr = String.join(",", requiredSkills);
        return jobId + "|" + title + "|" + description.replace("|", ";") + "|" + companyName
                + "|" + recruiterId + "|" + skillsStr + "|" + ctcMin + "|" + ctcMax
                + "|" + minCgpa + "|" + minYear + "|" + location + "|" + jobType
                + "|" + status + "|" + postedDate + "|" + deadline;
    }

    public static Job fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 13) return null;
        Job j = new Job(parts[0], parts[1], parts[2], parts[3], parts[4],
                Double.parseDouble(parts[6]), Double.parseDouble(parts[7]),
                Double.parseDouble(parts[8]), Integer.parseInt(parts[9]),
                parts[10], parts[11]);
        if (!parts[5].isEmpty()) {
            j.setRequiredSkills(new ArrayList<>(Arrays.asList(parts[5].split(","))));
        }
        j.setStatus(parts[12]);
        if (parts.length > 13) j.setPostedDate(Long.parseLong(parts[13]));
        if (parts.length > 14) j.setDeadline(parts[14]);
        return j;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobId\":\"").append(jobId).append("\",");
        sb.append("\"title\":\"").append(title).append("\",");
        sb.append("\"description\":\"").append(description.replace("\"", "'")).append("\",");
        sb.append("\"companyName\":\"").append(companyName).append("\",");
        sb.append("\"recruiterId\":\"").append(recruiterId).append("\",");
        sb.append("\"requiredSkills\":[");
        for (int i = 0; i < requiredSkills.size(); i++) {
            sb.append("\"").append(requiredSkills.get(i)).append("\"");
            if (i < requiredSkills.size() - 1) sb.append(",");
        }
        sb.append("],");
        sb.append("\"ctcMin\":").append(ctcMin).append(",");
        sb.append("\"ctcMax\":").append(ctcMax).append(",");
        sb.append("\"minCgpa\":").append(minCgpa).append(",");
        sb.append("\"minYear\":").append(minYear).append(",");
        sb.append("\"location\":\"").append(location).append("\",");
        sb.append("\"jobType\":\"").append(jobType).append("\",");
        sb.append("\"status\":\"").append(status).append("\",");
        sb.append("\"postedDate\":").append(postedDate).append(",");
        sb.append("\"deadline\":\"").append(deadline).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
