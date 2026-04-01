package service;

import interfaces.Notifiable;
import model.Notification;
import model.User;
import storage.FileStorageManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Notification service - IMPLEMENTS Notifiable interface.
 * Uses LinkedList<Notification> as required.
 */
public class NotificationService implements Notifiable {
    private LinkedList<Notification> notifications; // LinkedList as required
    private final FileStorageManager storage;

    public NotificationService(FileStorageManager storage) {
        this.storage = storage;
        this.notifications = storage.loadNotifications();
    }

    @Override
    public void sendNotification(String userId, String message, String type) {
        String id = "NOTIF" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        Notification.Type nType;
        try { nType = Notification.Type.valueOf(type); }
        catch (Exception e) { nType = Notification.Type.GENERAL; }

        Notification n = new Notification(id, userId, message, nType);
        notifications.addFirst(n); // LinkedList: add to front for recent-first order
        storage.saveNotifications(notifications);
    }

    @Override
    public void sendBroadcast(String message, String targetRole) {
        // Called from AdminService; stores with targetRole as userId prefix "BROADCAST_ROLE"
        String id = "NOTIF" + System.currentTimeMillis();
        Notification n = new Notification(id, "BROADCAST_" + targetRole, message, Notification.Type.GENERAL);
        notifications.addFirst(n);
        storage.saveNotifications(notifications);
    }

    public void sendBroadcastToUsers(String message, Map<String, ? extends User> users) {
        for (User u : users.values()) {
            sendNotification(u.getUserId(), message, "GENERAL");
        }
    }

    @Override
    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getUserId().equals(userId) || n.getUserId().equals("BROADCAST_ALL")
                    || n.getUserId().startsWith("BROADCAST_")) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public boolean markAsRead(String notificationId) {
        for (Notification n : notifications) {
            if (n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                storage.saveNotifications(notifications);
                return true;
            }
        }
        return false;
    }

    public void markAllAsRead(String userId) {
        for (Notification n : notifications) {
            if (n.getUserId().equals(userId)) n.setRead(true);
        }
        storage.saveNotifications(notifications);
    }

    @Override
    public int getUnreadCount(String userId) {
        int count = 0;
        for (Notification n : notifications) {
            if (n.getUserId().equals(userId) && !n.isRead()) count++;
        }
        return count;
    }

    public LinkedList<Notification> getAllNotifications() { return notifications; }
}
