package model;

public class Application {
    public enum Status { APPLIED, SHORTLISTED, REJECTED, SELECTED, INTERVIEW_SCHEDULED }

    private String applicationId;
    private String studentId;
    private String jobId;
    private String companyName;
    private String jobTitle;
    private Status status;
    private long appliedDate;
    private String coverLetter;
    private String interviewId;
    private String recruiterNote;

    public Application(String applicationId, String studentId, String jobId,
                       String companyName, String jobTitle) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.status = Status.APPLIED;
        this.appliedDate = System.currentTimeMillis();
        this.coverLetter = "";
        this.interviewId = "";
        this.recruiterNote = "";
    }

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getAppliedDate() { return appliedDate; }
    public void setAppliedDate(long appliedDate) { this.appliedDate = appliedDate; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getInterviewId() { return interviewId; }
    public void setInterviewId(String interviewId) { this.interviewId = interviewId; }

    public String getRecruiterNote() { return recruiterNote; }
    public void setRecruiterNote(String recruiterNote) { this.recruiterNote = recruiterNote; }

    public String toFileString() {
        return applicationId + "|" + studentId + "|" + jobId + "|" + companyName
                + "|" + jobTitle + "|" + status.name() + "|" + appliedDate
                + "|" + coverLetter.replace("|", ";") + "|" + interviewId + "|" + recruiterNote.replace("|", ";");
    }

    public static Application fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) return null;
        Application a = new Application(parts[0], parts[1], parts[2], parts[3], parts[4]);
        a.setStatus(Status.valueOf(parts[5]));
        a.setAppliedDate(Long.parseLong(parts[6]));
        if (parts.length > 7) a.setCoverLetter(parts[7]);
        if (parts.length > 8) a.setInterviewId(parts[8]);
        if (parts.length > 9) a.setRecruiterNote(parts[9]);
        return a;
    }

    public String toJson() {
        return "{" +
                "\"applicationId\":\"" + applicationId + "\"," +
                "\"studentId\":\"" + studentId + "\"," +
                "\"jobId\":\"" + jobId + "\"," +
                "\"companyName\":\"" + companyName + "\"," +
                "\"jobTitle\":\"" + jobTitle + "\"," +
                "\"status\":\"" + status.name() + "\"," +
                "\"appliedDate\":" + appliedDate + "," +
                "\"coverLetter\":\"" + coverLetter.replace("\"", "'") + "\"," +
                "\"interviewId\":\"" + interviewId + "\"," +
                "\"recruiterNote\":\"" + recruiterNote.replace("\"", "'") + "\"" +
                "}";
    }
}
