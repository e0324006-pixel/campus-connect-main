package exceptions;

/**
 * Thrown when a student tries to apply for a job they've already applied to
 */
public class DuplicateApplicationException extends Exception {
    private String studentId;
    private String jobId;
    private String existingApplicationId;

    public DuplicateApplicationException(String message, String studentId, String jobId) {
        super(message);
        this.studentId = studentId;
        this.jobId = jobId;
    }

    public DuplicateApplicationException(String message, String studentId, String jobId,
                                          String existingApplicationId) {
        super(message);
        this.studentId = studentId;
        this.jobId = jobId;
        this.existingApplicationId = existingApplicationId;
    }

    public String getStudentId() { return studentId; }
    public String getJobId() { return jobId; }
    public String getExistingApplicationId() { return existingApplicationId; }
}
