package com.learning.globallearningcalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nominations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nomination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private LearningSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nominee_user_id", nullable = false)
    private User nominee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nominator_user_id", nullable = false)
    private User nominator;

    @Enumerated(EnumType.STRING)
    @Column(name = "nomination_type", nullable = false)
    private NominationType nominationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NominationStatus status = NominationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "nominated_at", nullable = false)
    private LocalDateTime nominatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nominatedAt == null) {
            nominatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum NominationType {
        RECOMMENDED,
        MANDATORY
    }

    public enum NominationStatus {
        PENDING,      // Waiting for employee response (RECOMMENDED only)
        ACCEPTED,     // Employee accepted the recommendation
        DECLINED,     // Employee declined the recommendation
        COMPLETED     // Mandatory nomination with auto-created booking
    }
}
