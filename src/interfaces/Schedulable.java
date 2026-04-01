package interfaces;

import model.Interview;

/**
 * INTERFACE: Schedulable - for entities that support interview scheduling
 */
public interface Schedulable {
    Interview scheduleInterview(String applicationId, String studentId, String jobId,
                                String dateTime, String venue, String interviewType);
    boolean cancelInterview(String interviewId);
    boolean rescheduleInterview(String interviewId, String newDateTime);
    Interview getInterviewDetails(String interviewId);
}
