package service;

import interfaces.Schedulable;
import model.Interview;
import storage.FileStorageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Interview scheduling service - IMPLEMENTS Schedulable interface.
 */
public class InterviewService implements Schedulable {
    private List<Interview> interviews;
    private final FileStorageManager storage;

    public InterviewService(FileStorageManager storage) {
        this.storage = storage;
        this.interviews = storage.loadInterviews();
    }

    @Override
    public Interview scheduleInterview(String applicationId, String studentId, String jobId,
                                       String dateTime, String venue, String interviewTypeStr) {
        String id = "INT" + System.currentTimeMillis();
        Interview.InterviewType type;
        try { type = Interview.InterviewType.valueOf(interviewTypeStr.toUpperCase()); }
        catch (Exception e) { type = Interview.InterviewType.ONLINE; }

        // recruiterId will be set externally
        Interview interview = new Interview(id, applicationId, studentId, jobId, "", dateTime, venue, type);
        interviews.add(interview);
        storage.saveInterviews(interviews);
        return interview;
    }

    public Interview scheduleInterviewFull(String applicationId, String studentId, String jobId,
                                            String recruiterId, String dateTime, String venue,
                                            String interviewTypeStr, String meetingLink, String notes) {
        String id = "INT" + System.currentTimeMillis();
        Interview.InterviewType type;
        try { type = Interview.InterviewType.valueOf(interviewTypeStr.toUpperCase()); }
        catch (Exception e) { type = Interview.InterviewType.ONLINE; }

        Interview interview = new Interview(id, applicationId, studentId, jobId, recruiterId, dateTime, venue, type);
        interview.setMeetingLink(meetingLink != null ? meetingLink : "");
        interview.setNotes(notes != null ? notes : "");
        interviews.add(interview);
        storage.saveInterviews(interviews);
        return interview;
    }

    @Override
    public boolean cancelInterview(String interviewId) {
        for (Interview i : interviews) {
            if (i.getInterviewId().equals(interviewId)) {
                i.setStatus(Interview.InterviewStatus.CANCELLED);
                storage.saveInterviews(interviews);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean rescheduleInterview(String interviewId, String newDateTime) {
        for (Interview i : interviews) {
            if (i.getInterviewId().equals(interviewId)) {
                i.setDateTime(newDateTime);
                i.setStatus(Interview.InterviewStatus.RESCHEDULED);
                storage.saveInterviews(interviews);
                return true;
            }
        }
        return false;
    }

    @Override
    public Interview getInterviewDetails(String interviewId) {
        for (Interview i : interviews) {
            if (i.getInterviewId().equals(interviewId)) return i;
        }
        return null;
    }

    public List<Interview> getInterviewsByStudent(String studentId) {
        List<Interview> result = new ArrayList<>();
        for (Interview i : interviews) {
            if (i.getStudentId().equals(studentId)) result.add(i);
        }
        return result;
    }

    public List<Interview> getInterviewsByRecruiter(String recruiterId) {
        List<Interview> result = new ArrayList<>();
        for (Interview i : interviews) {
            if (i.getRecruiterId().equals(recruiterId)) result.add(i);
        }
        return result;
    }

    public List<Interview> getAllInterviews() { return interviews; }
}
