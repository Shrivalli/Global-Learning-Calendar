package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.NominationDTO;
import com.learning.globallearningcalendar.service.INominationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nominations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class NominationController {

    private final INominationService nominationService;

    /**
     * Manager nominates employees for a session (bulk or single)
     * POST /api/nominations/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<NominationDTO.BulkNominationResponse> bulkNominate(
            @Valid @RequestBody NominationDTO.BulkNominationRequest request,
            @RequestParam Long managerId) {
        
        log.info("Manager {} nominating {} employees for session {}", 
                managerId, request.getNomineeIds().size(), request.getSessionId());
        
        NominationDTO.BulkNominationResponse response = nominationService.nominateEmployees(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all nominations for the current employee
     * GET /api/nominations/my-nominations?employeeId={id}
     */
    @GetMapping("/my-nominations")
    public ResponseEntity<List<NominationDTO>> getMyNominations(@RequestParam Long employeeId) {
        log.info("Fetching all nominations for employee {}", employeeId);
        List<NominationDTO> nominations = nominationService.getNominationsForEmployee(employeeId);
        return ResponseEntity.ok(nominations);
    }

    /**
     * Get pending nominations for the current employee
     * GET /api/nominations/my-nominations/pending?employeeId={id}
     */
    @GetMapping("/my-nominations/pending")
    public ResponseEntity<List<NominationDTO>> getPendingNominations(@RequestParam Long employeeId) {
        log.info("Fetching pending nominations for employee {}", employeeId);
        List<NominationDTO> nominations = nominationService.getPendingNominationsForEmployee(employeeId);
        return ResponseEntity.ok(nominations);
    }

    /**
     * Get count of pending nominations (for badge counter)
     * GET /api/nominations/my-nominations/count?employeeId={id}
     */
    @GetMapping("/my-nominations/count")
    public ResponseEntity<NominationDTO.PendingNominationsCount> getPendingCount(@RequestParam Long employeeId) {
        log.info("Fetching pending nominations count for employee {}", employeeId);
        Long count = nominationService.getPendingNominationsCount(employeeId);
        return ResponseEntity.ok(new NominationDTO.PendingNominationsCount(count));
    }

    /**
     * Get actionable nominations (PENDING + recent MANDATORY) for display
     * GET /api/nominations/my-nominations/actionable?employeeId={id}
     */
    @GetMapping("/my-nominations/actionable")
    public ResponseEntity<List<NominationDTO>> getActionableNominations(@RequestParam Long employeeId) {
        log.info("Fetching actionable nominations for employee {}", employeeId);
        List<NominationDTO> nominations = nominationService.getActionableNominationsForEmployee(employeeId);
        return ResponseEntity.ok(nominations);
    }

    /**
     * Get count of actionable nominations (for badge counter)
     * GET /api/nominations/my-nominations/actionable-count?employeeId={id}
     */
    @GetMapping("/my-nominations/actionable-count")
    public ResponseEntity<NominationDTO.PendingNominationsCount> getActionableCount(@RequestParam Long employeeId) {
        log.info("Fetching actionable nominations count for employee {}", employeeId);
        Long count = nominationService.getActionableNominationsCount(employeeId);
        return ResponseEntity.ok(new NominationDTO.PendingNominationsCount(count));
    }

    /**
     * Get all nominations sent by a manager
     * GET /api/nominations/manager/{managerId}
     */
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<NominationDTO>> getNominationsByManager(@PathVariable Long managerId) {
        log.info("Fetching nominations sent by manager {}", managerId);
        List<NominationDTO> nominations = nominationService.getNominationsByManager(managerId);
        return ResponseEntity.ok(nominations);
    }

    /**
     * Employee accepts a recommendation
     * POST /api/nominations/{id}/accept
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<NominationDTO> acceptNomination(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        
        log.info("Employee {} accepting nomination {}", employeeId, id);
        NominationDTO nomination = nominationService.acceptNomination(id, employeeId);
        return ResponseEntity.ok(nomination);
    }

    /**
     * Employee declines a recommendation
     * POST /api/nominations/{id}/decline
     */
    @PostMapping("/{id}/decline")
    public ResponseEntity<NominationDTO> declineNomination(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        
        log.info("Employee {} declining nomination {}", employeeId, id);
        NominationDTO nomination = nominationService.declineNomination(id, employeeId);
        return ResponseEntity.ok(nomination);
    }

    /**
     * Employee acknowledges a MANDATORY nomination
     * POST /api/nominations/{id}/acknowledge
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<NominationDTO> acknowledgeNomination(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        
        log.info("Employee {} acknowledging nomination {}", employeeId, id);
        NominationDTO nomination = nominationService.acknowledgeNomination(id, employeeId);
        return ResponseEntity.ok(nomination);
    }

    /**
     * Get nomination by ID
     * GET /api/nominations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NominationDTO> getNominationById(@PathVariable Long id) {
        log.info("Fetching nomination {}", id);
        NominationDTO nomination = nominationService.getNominationById(id);
        return ResponseEntity.ok(nomination);
    }
}
