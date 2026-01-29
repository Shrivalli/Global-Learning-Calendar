package com.learning.globallearningcalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Waitlist Entity
 * Represents an employee's position in the waitlist for a full learning session.
 * When a booking is cancelled, the first person in the waitlist is automatically confirmed.
 */
@Entity
@Table(name = "waitlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession learningSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = WaitlistStatus.WAITING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Waitlist Status Enum
     * WAITING - User is currently in the waitlist
     * CONFIRMED - User was automatically moved from waitlist to confirmed booking
     * CANCELLED - Waitlist entry cancelled (session was cancelled)
     * EXPIRED - Waitlist entry expired (session started or was cancelled)
     * REMOVED - User manually removed themselves from waitlist
     */
    public enum WaitlistStatus {
        WAITING,
        CONFIRMED,
        CANCELLED,
        EXPIRED,
        REMOVED
    }
}
