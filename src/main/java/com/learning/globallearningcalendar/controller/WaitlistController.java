package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.WaitlistDTO;
import com.learning.globallearningcalendar.entity.Waitlist.WaitlistStatus;
import com.learning.globallearningcalendar.service.IWaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Waitlist operations
 */
@RestController
@RequestMapping("/api/v1/waitlist")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Waitlist", description = "Waitlist management APIs for learning sessions")
public class WaitlistController {

    private final IWaitlistService waitlistService;
    private static final Logger log = LoggerFactory.getLogger(WaitlistController.class);

    @PostMapping("/join")
    @Operation(summary = "Join waitlist for a session")
    public ResponseEntity<Map<String, Object>> joinWaitlist(@RequestBody @Valid Map<String, Object> request) {
        try {
            Long sessionId = Long.valueOf(request.get("sessionId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String notes = request.get("notes") != null ? request.get("notes").toString() : null;

            log.info("API call: User {} joining waitlist for session {}", userId, sessionId);

            WaitlistDTO waitlist = waitlistService.joinWaitlist(sessionId, userId, notes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully joined waitlist");
            response.put("waitlistId", waitlist.getId());
            response.put("position", waitlist.getPosition());
            response.put("data", waitlist);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error joining waitlist: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{waitlistId}")
    @Operation(summary = "Remove from waitlist")
    public ResponseEntity<Map<String, Object>> removeFromWaitlist(
            @PathVariable Long waitlistId,
            @RequestParam Long userId) {
        try {
            log.info("API call: User {} removing waitlist entry {}", userId, waitlistId);

            waitlistService.removeFromWaitlist(waitlistId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully removed from waitlist");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing from waitlist: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get waitlist for a session")
    public ResponseEntity<List<WaitlistDTO>> getSessionWaitlist(
            @PathVariable Long sessionId,
            @RequestParam(required = false, defaultValue = "WAITING") String status) {
        try {
            WaitlistStatus waitlistStatus = WaitlistStatus.valueOf(status.toUpperCase());
            List<WaitlistDTO> waitlist = waitlistService.getSessionWaitlist(sessionId, waitlistStatus);
            return ResponseEntity.ok(waitlist);
        } catch (IllegalArgumentException e) {
            log.error("Invalid waitlist status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/session/{sessionId}/active")
    @Operation(summary = "Get active (WAITING) waitlist for a session")
    public ResponseEntity<List<WaitlistDTO>> getActiveSessionWaitlist(@PathVariable Long sessionId) {
        List<WaitlistDTO> waitlist = waitlistService.getActiveSessionWaitlist(sessionId);
        return ResponseEntity.ok(waitlist);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all waitlist entries for a user")
    public ResponseEntity<List<WaitlistDTO>> getUserWaitlists(@PathVariable Long userId) {
        List<WaitlistDTO> waitlists = waitlistService.getUserWaitlists(userId);
        return ResponseEntity.ok(waitlists);
    }

    @GetMapping("/position")
    @Operation(summary = "Get user's position in waitlist for a session")
    public ResponseEntity<Map<String, Object>> getWaitlistPosition(
            @RequestParam Long sessionId,
            @RequestParam Long userId) {
        Integer position = waitlistService.getWaitlistPosition(sessionId, userId);
        boolean inWaitlist = waitlistService.isUserInWaitlist(sessionId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("inWaitlist", inWaitlist);
        response.put("position", position);

        if (position != null) {
            response.put("message", "You are at position " + position + " in the waitlist");
        } else {
            response.put("message", "Not in waitlist");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waitlist entry by ID")
    public ResponseEntity<WaitlistDTO> getWaitlistById(@PathVariable Long id) {
        WaitlistDTO waitlist = waitlistService.getWaitlistById(id);
        return ResponseEntity.ok(waitlist);
    }

    @PostMapping("/session/{sessionId}/process")
    @Operation(summary = "Manually trigger waitlist processing (admin only)")
    public ResponseEntity<Map<String, Object>> processWaitlist(@PathVariable Long sessionId) {
        try {
            log.info("Manual waitlist processing triggered for session {}", sessionId);
            waitlistService.processWaitlistForCancellation(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Waitlist processed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing waitlist: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
