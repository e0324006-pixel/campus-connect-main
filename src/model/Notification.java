package model;

public class Notification {
    public enum Type { JOB_POSTED, STATUS_UPDATE, INTERVIEW_SCHEDULED, GENERAL, APPROVAL }

    private String notificationId;
    private String userId;       // recipient
    private String message;
    private Type type;
    private boolean isRead;
    private long timestamp;
    private String referenceId; // jobId or applicationId

    public Notification(String notificationId, String userId, String message, Type type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
        this.referenceId = "";
    }

    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public Type getType() { return type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getTimestamp() { return timestamp; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String toFileString() {
        return notificationId + "|" + userId + "|" + message.replace("|", ";") + "|"
                + type.name() + "|" + isRead + "|" + timestamp + "|" + referenceId;
    }

    public static Notification fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return null;
        Notification n = new Notification(parts[0], parts[1], parts[2], Type.valueOf(parts[3]));
        n.setRead(Boolean.parseBoolean(parts[4]));
        n.timestamp = Long.parseLong(parts[5]);
        if (parts.length > 6) n.setReferenceId(parts[6]);
        return n;
    }

    public String toJson() {
        return "{" +
                "\"notificationId\":\"" + notificationId + "\"," +
                "\"userId\":\"" + userId + "\"," +
                "\"message\":\"" + message.replace("\"", "'") + "\"," +
                "\"type\":\"" + type.name() + "\"," +
                "\"isRead\":" + isRead + "," +
                "\"timestamp\":" + timestamp + "," +
                "\"referenceId\":\"" + referenceId + "\"" +
                "}";
    }
}
