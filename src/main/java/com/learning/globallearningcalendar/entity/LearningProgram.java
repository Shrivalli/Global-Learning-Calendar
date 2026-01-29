package com.learning.globallearningcalendar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "learning_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type")
    private ProgramType programType;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode")
    private DeliveryMode deliveryMode;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @ManyToMany
    @JoinTable(
        name = "program_skills",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "program_target_roles",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> targetRoles = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "program_target_business_units",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "business_unit_id")
    )
    private Set<BusinessUnit> targetBusinessUnits = new HashSet<>();

    @OneToMany(mappedBy = "learningProgram", cascade = CascadeType.ALL)
    private Set<LearningSession> sessions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

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

    public enum ProgramType {
        TECHNICAL,
        SOFT_SKILLS,
        LEADERSHIP,
        COMPLIANCE,
        ONBOARDING,
        CERTIFICATION,
        WORKSHOP,
        SEMINAR
    }

    public enum DeliveryMode {
        IN_PERSON,
        VIRTUAL,
        HYBRID,
        SELF_PACED
    }
}
