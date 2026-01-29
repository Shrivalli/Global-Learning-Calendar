package com.learning.globallearningcalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Entity
// Remove the table-level unique constraint that enforced uniqueness for (user_id, session_id)
// at the DB level. This constraint prevents creating a new booking if a previous booking
// exists (even if it was cancelled). We will enforce uniqueness for active bookings
// in application logic instead.
@Table(name = "bookings")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession learningSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BookingStatus status;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "confirmation_date")
    private LocalDateTime confirmationDate;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "attendance_status")
    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;

    @Column(name = "attendance_marked_at")
    private LocalDateTime attendanceMarkedAt;

    @Column(name = "completion_status")
    @Enumerated(EnumType.STRING)
    private CompletionStatus completionStatus;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "feedback_rating")
    private Integer feedbackRating;

    @Column(name = "feedback_comments", columnDefinition = "TEXT")
    private String feedbackComments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "manager_notified")
    private Boolean managerNotified = false;

    @Column(name = "manager_notified_date")
    private LocalDateTime managerNotifiedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BookingStatus {
        PENDING_APPROVAL,
        PENDING,
        CONFIRMED,
        PENDING_CANCELLATION,
        WAITLISTED,
        CANCELLED,
        REJECTED,
        NO_SHOW,
        COMPLETED
    }

    public enum AttendanceStatus {
        NOT_MARKED,
        PRESENT,
        ABSENT,
        PARTIAL
    }

    public enum CompletionStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        INCOMPLETE
    }
}
