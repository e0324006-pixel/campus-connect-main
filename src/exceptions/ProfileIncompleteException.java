package exceptions;

import java.util.List;

/**
 * Thrown when a student's profile is incomplete for job application
 */
public class ProfileIncompleteException extends Exception {
    private List<String> missingFields;

    public ProfileIncompleteException(String message) {
        super(message);
    }

    public ProfileIncompleteException(String message, List<String> missingFields) {
        super(message);
        this.missingFields = missingFields;
    }

    public List<String> getMissingFields() { return missingFields; }

    public String getMissingFieldsSummary() {
        if (missingFields == null || missingFields.isEmpty()) return "Unknown fields";
        return String.join(", ", missingFields);
    }
}
