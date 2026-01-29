package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.WaitlistDTO;
import com.learning.globallearningcalendar.entity.Waitlist.WaitlistStatus;

import java.util.List;

/**
 * Service interface for Waitlist operations
 */
public interface IWaitlistService {

    /**
     * Add a user to the waitlist for a session
     */
    WaitlistDTO joinWaitlist(Long sessionId, Long userId, String notes);

    /**
     * Remove a user from the waitlist
     */
    void removeFromWaitlist(Long waitlistId, Long userId);

    /**
     * Get all waitlist entries for a session with a specific status
     */
    List<WaitlistDTO> getSessionWaitlist(Long sessionId, WaitlistStatus status);

    /**
     * Get all active (WAITING) waitlist entries for a session
     */
    List<WaitlistDTO> getActiveSessionWaitlist(Long sessionId);

    /**
     * Get all waitlist entries for a user
     */
    List<WaitlistDTO> getUserWaitlists(Long userId);

    /**
     * Get a user's position in the waitlist for a session
     */
    Integer getWaitlistPosition(Long sessionId, Long userId);

    /**
     * Check if a user is in the waitlist for a session
     */
    boolean isUserInWaitlist(Long sessionId, Long userId);

    /**
     * Process the waitlist when a booking is cancelled
     * Automatically confirms the first person in the waitlist if seats are available
     */
    void processWaitlistForCancellation(Long sessionId);

    /**
     * Get waitlist entry by ID
     */
    WaitlistDTO getWaitlistById(Long id);

    /**
     * Cancel all waitlist entries for a session when the session is cancelled
     */
    void cancelWaitlistBySession(Long sessionId);
}
