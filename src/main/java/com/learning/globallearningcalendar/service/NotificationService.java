package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.NotificationDTO;
import com.learning.globallearningcalendar.entity.Booking;
import com.learning.globallearningcalendar.entity.Notification;

import java.util.List;

public interface NotificationService {
    
    NotificationDTO createNotification(Long userId, String title, String message, 
                                      Notification.NotificationType type, Long bookingId);
    
    List<NotificationDTO> getUserNotifications(Long userId);
    
    List<NotificationDTO> getUnreadNotifications(Long userId);
    
    Long getUnreadCount(Long userId);
    
    void markAsRead(Long notificationId, Long userId);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long notificationId, Long userId);
    
    // Helper methods to create notifications for specific events
    void notifyBookingConfirmed(Booking booking);
    
    void notifyBookingCancelled(Booking booking, String reason);
    
    void notifyBookingWaitlisted(Booking booking);
    
    void notifyWaitlistPromoted(Booking booking);
    
    void notifyBookingApproved(Booking booking);
    
    void notifyBookingRejected(Booking booking, String reason);
    
    void notifyCancellationApproved(Booking booking);
    
    void notifyCancellationRejected(Booking booking, String reason);
}
