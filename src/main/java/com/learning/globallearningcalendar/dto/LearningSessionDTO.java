package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.DeliveryMode;
import com.learning.globallearningcalendar.entity.LearningSession;
import com.learning.globallearningcalendar.entity.LocationScope;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningSessionDTO {

    private Long id;

    private String sessionCode;

    @NotNull(message = "Learning program ID is required")
    private Long learningProgramId;

    private String learningProgramName;

    private String learningProgramCode;

    private java.util.List<Long> skillIds;

    private String programType;

    private DeliveryMode sessionDeliveryMode;

    private LocationScope sessionLocationScope;

    private Set<Long> targetRoleIds;

    private List<Long> targetLocationIds;

    private java.util.List<Long> targetBusinessUnitIds;

    private Long locationId;

    private String locationName;

    private String locationCity;

    private String locationCountry;

    @NotNull(message = "Start date time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    private LocalDateTime endDateTime;

    @NotNull(message = "Total seats is required")
    private Integer totalSeats;

    private Integer availableSeats;

    private Integer waitlistCapacity;

    private LearningSession.SessionStatus status;

    private String instructorName;

    private String instructorEmail;

    private String virtualMeetingLink;

    private String roomNumber;

    private String notes;

    private Long createdById;

    private String createdByName;

    private Long confirmedBookingsCount;

    private Long waitlistedBookingsCount;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
