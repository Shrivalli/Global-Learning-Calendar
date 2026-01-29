package com.learning.globallearningcalendar.dto;

import com.learning.globallearningcalendar.entity.Nomination;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NominationDTO {

    private Long id;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private String sessionCode;
    private String sessionName;
    private LocalDateTime sessionStartDateTime;
    private LocalDateTime sessionEndDateTime;
    private String sessionLocationName;
    private String sessionFacilitatorName;
    private String sessionDuration;

    private Long nomineeId;
    private String nomineeFirstName;
    private String nomineeLastName;
    private String nomineeEmail;

    private Long nominatorId;
    private String nominatorFirstName;
    private String nominatorLastName;

    @NotNull(message = "Nomination type is required")
    private Nomination.NominationType nominationType;

    private Nomination.NominationStatus status;

    private Long bookingId;

    private String notes;

    private LocalDateTime nominatedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Request DTO for bulk nominations
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkNominationRequest {
        
        @NotNull(message = "Session ID is required")
        private Long sessionId;

        @NotEmpty(message = "At least one nominee is required")
        private List<Long> nomineeIds;

        @NotNull(message = "Nomination type is required")
        private Nomination.NominationType nominationType;

        private String notes;
    }

    // Response DTO for bulk nominations
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkNominationResponse {
        private int totalNominations;
        private int successful;
        private int failed;
        private List<String> errors;
        private List<NominationDTO> nominations;
    }

    // Response DTO for pending nominations count
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingNominationsCount {
        private Long count;
    }
}
