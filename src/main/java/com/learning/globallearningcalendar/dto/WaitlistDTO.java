package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.Waitlist.WaitlistStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Waitlist
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistDTO {

    private Long id;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private String sessionCode;

    private String programName;

    private LocalDateTime sessionStartDateTime;

    private LocalDateTime sessionEndDateTime;

    private Double sessionDurationHours;

    private String sessionLocationName;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String userName;

    private String userEmail;

    private String userEmployeeId;

    @NotNull(message = "Position is required")
    private Integer position;

    private WaitlistStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime notifiedAt;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
