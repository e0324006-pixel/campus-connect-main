package exceptions;

/**
 * Thrown when login credentials are invalid
 */
public class InvalidLoginException extends Exception {
    private String attemptedEmail;

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(String message, String attemptedEmail) {
        super(message);
        this.attemptedEmail = attemptedEmail;
    }

    public String getAttemptedEmail() { return attemptedEmail; }
}
