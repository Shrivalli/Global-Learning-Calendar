package com.learning.globallearningcalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "learning_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private LearningProgram learningProgram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "waitlist_capacity")
    private Integer waitlistCapacity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode")
    private DeliveryMode deliveryMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_scope")
    private LocationScope locationScope;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "session_target_roles",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> targetRoles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "session_target_locations",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<Location> targetLocations = new HashSet<>();

    @Column(name = "instructor_name")
    private String instructorName;

    @Column(name = "instructor_email")
    private String instructorEmail;

    @Column(name = "virtual_meeting_link")
    private String virtualMeetingLink;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "learningSession", cascade = CascadeType.ALL)
    private Set<Booking> bookings = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean hasAvailableSeats() {
        return availableSeats != null && availableSeats > 0;
    }

    public void decrementAvailableSeats() {
        if (availableSeats == null) {
            // if null, initialize from totalSeats (prefer defensive fallback)
            availableSeats = (totalSeats != null) ? Math.max(0, totalSeats - 1) : 0;
        } else if (availableSeats > 0) {
            availableSeats--;
        }
    }

    public void incrementAvailableSeats() {
        if (availableSeats == null) {
            availableSeats = (totalSeats != null) ? Math.min(totalSeats, 1) : 1;
        } else if (totalSeats == null) {
            availableSeats++;
        } else if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    public enum SessionStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        POSTPONED
    }
}
