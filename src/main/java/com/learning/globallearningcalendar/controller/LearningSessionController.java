package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.LearningSessionDTO;
import com.learning.globallearningcalendar.entity.LearningSession;
import com.learning.globallearningcalendar.service.ILearningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-sessions")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Learning Session", description = "Learning Session management APIs")
public class LearningSessionController {

    private final ILearningSessionService learningSessionService;

    @GetMapping
    @Operation(summary = "Get all learning sessions")
    public ResponseEntity<List<LearningSessionDTO>> getAllSessions() {
        return ResponseEntity.ok(learningSessionService.getAllSessions());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all learning sessions with pagination")
    public ResponseEntity<Page<LearningSessionDTO>> getAllSessionsPaged(Pageable pageable) {
        return ResponseEntity.ok(learningSessionService.getAllSessions(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active learning sessions")
    public ResponseEntity<List<LearningSessionDTO>> getActiveSessions() {
        return ResponseEntity.ok(learningSessionService.getActiveSessions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get learning session by ID")
    public ResponseEntity<LearningSessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(learningSessionService.getSessionById(id));
    }

    @GetMapping("/code/{sessionCode}")
    @Operation(summary = "Get learning session by code")
    public ResponseEntity<LearningSessionDTO> getSessionByCode(@PathVariable String sessionCode) {
        return ResponseEntity.ok(learningSessionService.getSessionByCode(sessionCode));
    }

    @PostMapping
    @Operation(summary = "Create a new learning session")
    public ResponseEntity<LearningSessionDTO> createSession(@Valid @RequestBody LearningSessionDTO dto) {
        return new ResponseEntity<>(learningSessionService.createSession(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a learning session")
    public ResponseEntity<LearningSessionDTO> updateSession(@PathVariable Long id, @Valid @RequestBody LearningSessionDTO dto) {
        return ResponseEntity.ok(learningSessionService.updateSession(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a learning session (soft delete)")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        learningSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a learning session")
    public ResponseEntity<Void> cancelSession(@PathVariable Long id) {
        learningSessionService.cancelSession(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/program/{programId}")
    @Operation(summary = "Get learning sessions by program")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(learningSessionService.getSessionsByProgram(programId));
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get learning sessions by location")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(learningSessionService.getSessionsByLocation(locationId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get learning sessions by status")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByStatus(@PathVariable LearningSession.SessionStatus status) {
        return ResponseEntity.ok(learningSessionService.getSessionsByStatus(status));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get learning sessions by date range")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(learningSessionService.getSessionsByDateRange(startDate, endDate));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming learning sessions")
    public ResponseEntity<List<LearningSessionDTO>> getUpcomingSessions() {
        return ResponseEntity.ok(learningSessionService.getUpcomingSessions());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get sessions visible to a specific user (filtered by role and location)")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(learningSessionService.getAllSessionsForUser(userId));
    }
    
    @GetMapping("/user/{userId}/including-subordinates")
    @Operation(summary = "Get sessions visible to a user including their subordinates' sessions (for managers)")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsForUserIncludingSubordinates(@PathVariable Long userId) {
        return ResponseEntity.ok(learningSessionService.getAllSessionsForUserIncludingSubordinates(userId));
    }

    @GetMapping("/by-roles")
    @Operation(summary = "Get sessions filtered by role IDs")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByRoles(@RequestParam(required = false) List<Long> roleIds) {
        return ResponseEntity.ok(learningSessionService.getAllSessionsByRoles(roleIds));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available learning sessions (with open seats)")
    public ResponseEntity<List<LearningSessionDTO>> getAvailableSessions() {
        return ResponseEntity.ok(learningSessionService.getAvailableSessions());
    }

    @GetMapping("/program/{programId}/available")
    @Operation(summary = "Get available learning sessions by program")
    public ResponseEntity<List<LearningSessionDTO>> getAvailableSessionsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(learningSessionService.getAvailableSessionsByProgram(programId));
    }

    @GetMapping("/location/{locationId}/date-range")
    @Operation(summary = "Get learning sessions by location and date range")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByLocationAndDateRange(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(learningSessionService.getSessionsByLocationAndDateRange(locationId, startDate, endDate));
    }

    @GetMapping("/instructor/{instructorEmail}")
    @Operation(summary = "Get learning sessions by instructor email")
    public ResponseEntity<List<LearningSessionDTO>> getSessionsByInstructor(@PathVariable String instructorEmail) {
        return ResponseEntity.ok(learningSessionService.getSessionsByInstructor(instructorEmail));
    }

    @GetMapping("/skill/{skillId}/upcoming")
    @Operation(summary = "Get upcoming learning sessions by skill")
    public ResponseEntity<List<LearningSessionDTO>> getUpcomingSessionsBySkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(learningSessionService.getUpcomingSessionsBySkill(skillId));
    }

    @GetMapping("/{id}/confirmed-bookings-count")
    @Operation(summary = "Get confirmed bookings count for a session")
    public ResponseEntity<Long> countConfirmedBookings(@PathVariable Long id) {
        return ResponseEntity.ok(learningSessionService.countConfirmedBookings(id));
    }

    @GetMapping("/{id}/waitlisted-bookings-count")
    @Operation(summary = "Get waitlisted bookings count for a session")
    public ResponseEntity<Long> countWaitlistedBookings(@PathVariable Long id) {
        return ResponseEntity.ok(learningSessionService.countWaitlistedBookings(id));
    }
}
