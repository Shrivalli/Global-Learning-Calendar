package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.NominationDTO;

import java.util.List;

public interface INominationService {

    /**
     * Nominate employees for a session (bulk or single)
     */
    NominationDTO.BulkNominationResponse nominateEmployees(NominationDTO.BulkNominationRequest request, Long nominatorId);

    /**
     * Get all nominations for an employee
     */
    List<NominationDTO> getNominationsForEmployee(Long employeeId);

    /**
     * Get pending nominations for an employee
     */
    List<NominationDTO> getPendingNominationsForEmployee(Long employeeId);

    /**
     * Get count of pending nominations for an employee (for badge)
     */
    Long getPendingNominationsCount(Long employeeId);

    /**
     * Get actionable nominations (PENDING + recent MANDATORY) for an employee
     */
    List<NominationDTO> getActionableNominationsForEmployee(Long employeeId);

    /**
     * Get count of actionable nominations (PENDING + recent MANDATORY) for badge
     */
    Long getActionableNominationsCount(Long employeeId);

    /**
     * Get all nominations sent by a manager
     */
    List<NominationDTO> getNominationsByManager(Long managerId);

    /**
     * Accept a nomination (employee accepts recommendation)
     */
    NominationDTO acceptNomination(Long nominationId, Long employeeId);

    /**
     * Decline a nomination (employee declines recommendation)
     */
    NominationDTO declineNomination(Long nominationId, Long employeeId);

    /**
     * Acknowledge a MANDATORY nomination (mark as read/acknowledged)
     */
    NominationDTO acknowledgeNomination(Long nominationId, Long employeeId);

    /**
     * Get nomination by ID
     */
    NominationDTO getNominationById(Long id);
}
