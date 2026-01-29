package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private Boolean isRead;
    private Long bookingId;
    private LocalDateTime createdAt;
}
