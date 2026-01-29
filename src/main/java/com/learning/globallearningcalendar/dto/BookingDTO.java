package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {

    private Long id;

    private String bookingReference;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String userName;

    private String userEmail;

    private String userEmployeeId;

    private Long userBusinessUnitId;

    private String userBusinessUnitName;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private String sessionCode;

    private String programName;

    private String programCode;

    private LocalDateTime sessionStartDateTime;

    private LocalDateTime sessionEndDateTime;

    private String sessionLocationName;

    private Double sessionDurationHours;  // Calculated duration in hours

    private Booking.BookingStatus status;

    private Integer seatNumber;

    private Integer waitlistPosition;

    private LocalDateTime bookingDate;

    private LocalDateTime confirmationDate;

    private LocalDateTime cancellationDate;

    private String cancellationReason;

    private Booking.AttendanceStatus attendanceStatus;

    private LocalDateTime attendanceMarkedAt;

    private Booking.CompletionStatus completionStatus;

    private LocalDateTime completionDate;

    private Integer feedbackRating;

    private String feedbackComments;

    private Long approvedById;

    private String approvedByName;

    private LocalDateTime approvalDate;

    private Long rejectedById;

    private String rejectedByName;

    private LocalDateTime rejectionDate;

    private String rejectionReason;

    private Boolean managerNotified;

    private LocalDateTime managerNotifiedDate;

    private Long managerId;

    private String managerName;

    private String managerEmail;

    private String notes;

    private String nominationType;  // MANDATORY or RECOMMENDED if from nomination

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
