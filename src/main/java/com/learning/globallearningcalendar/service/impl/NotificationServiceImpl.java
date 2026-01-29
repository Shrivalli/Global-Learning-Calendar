package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.NotificationDTO;
import com.learning.globallearningcalendar.entity.Booking;
import com.learning.globallearningcalendar.entity.Notification;
import com.learning.globallearningcalendar.entity.User;
import com.learning.globallearningcalendar.repository.NotificationRepository;
import com.learning.globallearningcalendar.repository.UserRepository;
import com.learning.globallearningcalendar.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(Long userId, String title, String message,
                                             Notification.NotificationType type, Long bookingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        if (bookingId != null) {
            Booking booking = new Booking();
            booking.setId(bookingId);
            notification.setBooking(booking);
        }

        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(userId)) {
                notificationRepository.delete(notification);
            }
        });
    }

    @Override
    @Transactional
    public void notifyBookingConfirmed(Booking booking) {
        String title = "Booking Confirmed";
        String message = String.format("Your booking for '%s' has been confirmed. Session date: %s",
                booking.getLearningSession().getLearningProgram().getName(),
                formatDateTime(booking.getLearningSession().getStartDateTime()));
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.BOOKING_CONFIRMED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyBookingCancelled(Booking booking, String reason) {
        String title = "Booking Cancelled";
        String message = String.format("Your booking for '%s' has been cancelled.%s",
                booking.getLearningSession().getLearningProgram().getName(),
                reason != null ? " Reason: " + reason : "");
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.BOOKING_CANCELLED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyBookingWaitlisted(Booking booking) {
        String title = "Added to Waitlist";
        String message = String.format("You've been added to the waitlist for '%s'. You'll be notified if a seat becomes available.",
                booking.getLearningSession().getLearningProgram().getName());
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.BOOKING_WAITLISTED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyWaitlistPromoted(Booking booking) {
        String title = "Waitlist Promoted!";
        String message = String.format("Great news! Your waitlist booking for '%s' has been confirmed. Session date: %s",
                booking.getLearningSession().getLearningProgram().getName(),
                formatDateTime(booking.getLearningSession().getStartDateTime()));
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.WAITLIST_PROMOTED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyBookingApproved(Booking booking) {
        String title = "Booking Approved";
        String message = String.format("Your manager has approved your booking for '%s'. Session date: %s",
                booking.getLearningSession().getLearningProgram().getName(),
                formatDateTime(booking.getLearningSession().getStartDateTime()));
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.BOOKING_APPROVED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyBookingRejected(Booking booking, String reason) {
        String title = "Booking Rejected";
        String message = String.format("Your booking request for '%s' has been rejected by your manager.%s",
                booking.getLearningSession().getLearningProgram().getName(),
                reason != null ? " Reason: " + reason : "");
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.BOOKING_REJECTED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyCancellationApproved(Booking booking) {
        String title = "Cancellation Approved";
        String message = String.format("Your cancellation request for '%s' has been approved.",
                booking.getLearningSession().getLearningProgram().getName());
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.CANCELLATION_APPROVED, booking.getId());
    }

    @Override
    @Transactional
    public void notifyCancellationRejected(Booking booking, String reason) {
        String title = "Cancellation Request Rejected";
        String message = String.format("Your cancellation request for '%s' has been rejected by your manager.%s Your booking remains active.",
                booking.getLearningSession().getLearningProgram().getName(),
                reason != null ? " Reason: " + reason : "");
        
        createNotification(booking.getUser().getId(), title, message,
                Notification.NotificationType.CANCELLATION_REJECTED, booking.getId());
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .bookingId(notification.getBooking() != null ? notification.getBooking().getId() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "TBD";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        return dateTime.format(formatter);
    }
}
