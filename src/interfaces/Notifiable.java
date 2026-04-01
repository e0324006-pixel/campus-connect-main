package interfaces;

import model.Notification;
import java.util.List;

/**
 * INTERFACE: Notifiable - for notification delivery capabilities
 */
public interface Notifiable {
    void sendNotification(String userId, String message, String type);
    void sendBroadcast(String message, String targetRole);
    List<Notification> getNotificationsForUser(String userId);
    boolean markAsRead(String notificationId);
    int getUnreadCount(String userId);
}
