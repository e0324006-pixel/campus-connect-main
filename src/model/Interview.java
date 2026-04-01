package model;

public class Interview {
    public enum InterviewType { ONLINE, OFFLINE, PHONE, GROUP_DISCUSSION }
    public enum InterviewStatus { SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED }

    private String interviewId;
    private String applicationId;
    private String studentId;
    private String jobId;
    private String recruiterId;
    private String dateTime;       // ISO format string
    private String venue;
    private InterviewType interviewType;
    private InterviewStatus status;
    private String notes;
    private String meetingLink;

    public Interview(String interviewId, String applicationId, String studentId,
                     String jobId, String recruiterId, String dateTime, String venue,
                     InterviewType interviewType) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.recruiterId = recruiterId;
        this.dateTime = dateTime;
        this.venue = venue;
        this.interviewType = interviewType;
        this.status = InterviewStatus.SCHEDULED;
        this.notes = "";
        this.meetingLink = "";
    }

    // Getters and Setters
    public String getInterviewId() { return interviewId; }
    public String getApplicationId() { return applicationId; }
    public String getStudentId() { return studentId; }
    public String getJobId() { return jobId; }
    public String getRecruiterId() { return recruiterId; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType interviewType) { this.interviewType = interviewType; }
    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String toFileString() {
        return interviewId + "|" + applicationId + "|" + studentId + "|" + jobId + "|"
                + recruiterId + "|" + dateTime + "|" + venue + "|" + interviewType.name()
                + "|" + status.name() + "|" + notes.replace("|", ";") + "|" + meetingLink;
    }

    public static Interview fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) return null;
        Interview i = new Interview(parts[0], parts[1], parts[2], parts[3], parts[4],
                parts[5], parts[6], InterviewType.valueOf(parts[7]));
        i.setStatus(InterviewStatus.valueOf(parts[8]));
        if (parts.length > 9) i.setNotes(parts[9]);
        if (parts.length > 10) i.setMeetingLink(parts[10]);
        return i;
    }

    public String toJson() {
        return "{" +
                "\"interviewId\":\"" + interviewId + "\"," +
                "\"applicationId\":\"" + applicationId + "\"," +
                "\"studentId\":\"" + studentId + "\"," +
                "\"jobId\":\"" + jobId + "\"," +
                "\"recruiterId\":\"" + recruiterId + "\"," +
                "\"dateTime\":\"" + dateTime + "\"," +
                "\"venue\":\"" + venue + "\"," +
                "\"interviewType\":\"" + interviewType.name() + "\"," +
                "\"status\":\"" + status.name() + "\"," +
                "\"notes\":\"" + notes.replace("\"", "'") + "\"," +
                "\"meetingLink\":\"" + meetingLink + "\"" +
                "}";
    }
}
