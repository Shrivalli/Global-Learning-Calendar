package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.WaitlistDTO;
import com.learning.globallearningcalendar.entity.Booking;
import com.learning.globallearningcalendar.entity.LearningSession;
import com.learning.globallearningcalendar.entity.User;
import com.learning.globallearningcalendar.entity.Waitlist;
import com.learning.globallearningcalendar.entity.Waitlist.WaitlistStatus;
import com.learning.globallearningcalendar.exception.BadRequestException;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.BookingRepository;
import com.learning.globallearningcalendar.repository.LearningSessionRepository;
import com.learning.globallearningcalendar.repository.UserRepository;
import com.learning.globallearningcalendar.repository.WaitlistRepository;
import com.learning.globallearningcalendar.service.IWaitlistService;
import com.learning.globallearningcalendar.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Waitlist operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WaitlistServiceImpl implements IWaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    @Override
    public WaitlistDTO joinWaitlist(Long sessionId, Long userId, String notes) {
        log.info("User {} attempting to join waitlist for session {}", userId, sessionId);

        // Validate session exists
        LearningSession session = learningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", sessionId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if already in waitlist (any status - WAITING, CONFIRMED, EXPIRED)
        if (waitlistRepository.existsByLearningSessionIdAndUserId(sessionId, userId)) {
            log.error("User {} is already in waitlist for session {}", userId, sessionId);
            throw new BadRequestException("You are already in the waitlist for this session");
        }

        // Check if already has a confirmed booking
        if (bookingRepository.findByUserIdAndSessionId(userId, sessionId).isPresent()) {
            log.error("User {} already has a booking for session {}", userId, sessionId);
            throw new BadRequestException("You already have a booking for this session");
        }

        // Check if session is full (if not, they should book directly instead)
        if (session.hasAvailableSeats()) {
            log.warn("User {} trying to join waitlist but session {} has available seats", userId, sessionId);
            throw new BadRequestException("This session has available seats. Please book directly instead of joining the waitlist");
        }

        // Get next position in waitlist
        Integer maxPosition = waitlistRepository.findMaxPositionBySession(sessionId);
        Integer nextPosition = (maxPosition == null) ? 1 : maxPosition + 1;

        // Create waitlist entry
        Waitlist waitlist = Waitlist.builder()
                .learningSession(session)
                .user(user)
                .position(nextPosition)
                .status(WaitlistStatus.WAITING)
                .joinedAt(LocalDateTime.now())
                .notes(notes)
                .build();

        waitlist = waitlistRepository.save(waitlist);
        log.info("User {} joined waitlist for session {} at position {}", userId, sessionId, nextPosition);

        return toDTO(waitlist);
    }

    @Override
    public void removeFromWaitlist(Long waitlistId, Long userId) {
        log.info("User {} attempting to remove waitlist entry {}", userId, waitlistId);

        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist", "id", waitlistId));

        // Verify the user owns this waitlist entry
        if (!waitlist.getUser().getId().equals(userId)) {
            log.error("User {} attempted to remove waitlist entry {} belonging to user {}", 
                    userId, waitlistId, waitlist.getUser().getId());
            throw new BadRequestException("You are not authorized to remove this waitlist entry");
        }

        // Update status to REMOVED
        waitlist.setStatus(WaitlistStatus.REMOVED);
        waitlistRepository.save(waitlist);

        // Reorder remaining waitlist entries
        reorderWaitlist(waitlist.getLearningSession().getId());

        log.info("User {} removed from waitlist for session {}", userId, waitlist.getLearningSession().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistDTO> getSessionWaitlist(Long sessionId, WaitlistStatus status) {
        return waitlistRepository.findBySessionIdAndStatusOrderByPosition(sessionId, status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistDTO> getActiveSessionWaitlist(Long sessionId) {
        return getSessionWaitlist(sessionId, WaitlistStatus.WAITING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistDTO> getUserWaitlists(Long userId) {
        return waitlistRepository.findActiveWaitlistsByUser(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getWaitlistPosition(Long sessionId, Long userId) {
        return waitlistRepository.findBySessionIdAndUserIdAndStatus(sessionId, userId, WaitlistStatus.WAITING)
                .map(Waitlist::getPosition)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInWaitlist(Long sessionId, Long userId) {
        return waitlistRepository.existsByLearningSessionIdAndUserIdAndStatus(sessionId, userId, WaitlistStatus.WAITING);
    }

    @Override
    public void processWaitlistForCancellation(Long sessionId) {
        log.info("Processing waitlist for session {}", sessionId);

        // Refresh session to get latest data including incremented available seats
        LearningSession session = learningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", sessionId));

        log.info("Session {} has {} available seats and {} total capacity", 
                sessionId, session.getAvailableSeats(), session.getTotalSeats());

        // Check if there are available seats
        if (session.getAvailableSeats() == null || session.getAvailableSeats() <= 0) {
            log.info("No available seats for session {}. Available seats: {}. Waitlist processing skipped", 
                    sessionId, session.getAvailableSeats());
            return;
        }

        // Get waitlist entries in order
        List<Waitlist> waitingList = waitlistRepository.findBySessionIdAndStatusOrderByPosition(sessionId, WaitlistStatus.WAITING);

        if (waitingList.isEmpty()) {
            log.info("No waitlist entries for session {}", sessionId);
            return;
        }

        int seatsToFill = Math.min(session.getAvailableSeats(), waitingList.size());
        log.info("Found {} people in waitlist for session {}. Attempting to fill {} seats", 
                waitingList.size(), sessionId, seatsToFill);

        for (int i = 0; i < seatsToFill; i++) {
            Waitlist waitlistEntry = waitingList.get(i);
            
            try {
                User user = waitlistEntry.getUser();
                
                // Check if user has a manager - if yes, require approval
                Booking.BookingStatus bookingStatus;
                LocalDateTime confirmationDate = null;
                
                if (user.getManager() != null) {
                    // User has a manager - require manager approval
                    bookingStatus = Booking.BookingStatus.PENDING_APPROVAL;
                    log.info("User {} has a manager. Booking from waitlist will require manager approval", user.getId());
                } else {
                    // No manager - auto-confirm
                    bookingStatus = Booking.BookingStatus.CONFIRMED;
                    confirmationDate = LocalDateTime.now();
                    log.info("User {} has no manager. Booking from waitlist will be auto-confirmed", user.getId());
                }
                
                // Create booking for the waitlisted user
                Booking booking = Booking.builder()
                        .bookingReference(generateBookingReference())
                        .user(user)
                        .learningSession(session)
                        .status(bookingStatus)
                        .bookingDate(LocalDateTime.now())
                        .confirmationDate(confirmationDate)
                        .attendanceStatus(Booking.AttendanceStatus.NOT_MARKED)
                        .completionStatus(Booking.CompletionStatus.NOT_STARTED)
                        .managerNotified(false)
                        .notes("Promoted from waitlist position " + waitlistEntry.getPosition())
                        .build();

                bookingRepository.save(booking);

                // Delete waitlist entry after successful booking creation
                waitlistRepository.delete(waitlistEntry);
                log.info("Deleted waitlist entry {} for user {} after creating booking", 
                        waitlistEntry.getId(), user.getId());

                // CRITICAL: Decrement seat immediately for BOTH confirmed and pending approval bookings
                // This ensures the seat is locked and not shown as available to others
                session.decrementAvailableSeats();
                learningSessionRepository.save(session);
                
                if (bookingStatus == Booking.BookingStatus.CONFIRMED) {
                    log.info("User {} auto-confirmed from waitlist position {} for session {} (no manager)", 
                            user.getId(), waitlistEntry.getPosition(), sessionId);
                    
                    // Send waitlist promotion notification for auto-confirmed bookings
                    try {
                        notificationService.notifyWaitlistPromoted(booking);
                        log.info("Notification sent for waitlist promotion to user {}", user.getId());
                    } catch (Exception notifEx) {
                        log.error("Failed to send waitlist promotion notification for user {}: {}", 
                                user.getId(), notifEx.getMessage());
                    }
                } else {
                    // Seat is decremented immediately - locked while awaiting manager approval
                    log.info("User {} moved from waitlist position {} to PENDING_APPROVAL for session {} (seat locked, requires manager approval)", 
                            user.getId(), waitlistEntry.getPosition(), sessionId);
                }
                
            } catch (Exception e) {
                log.error("Error processing waitlist user {} for session {}: {}", 
                        waitlistEntry.getUser().getId(), sessionId, e.getMessage(), e);
            }
        }

        // Reorder remaining waitlist
        if (seatsToFill > 0) {
            reorderWaitlist(sessionId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WaitlistDTO getWaitlistById(Long id) {
        Waitlist waitlist = waitlistRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist", "id", id));
        return toDTO(waitlist);
    }

    /**
     * Reorder waitlist positions after removal or confirmation
     */
    private void reorderWaitlist(Long sessionId) {
        List<Waitlist> waitingList = waitlistRepository.findBySessionIdAndStatusOrderByPosition(sessionId, WaitlistStatus.WAITING);

        for (int i = 0; i < waitingList.size(); i++) {
            Waitlist entry = waitingList.get(i);
            entry.setPosition(i + 1);
            waitlistRepository.save(entry);
        }

        log.info("Reordered {} waitlist entries for session {}", waitingList.size(), sessionId);
    }

    /**
     * Generate a unique booking reference
     */
    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Convert Waitlist entity to DTO
     */
    private WaitlistDTO toDTO(Waitlist waitlist) {
        WaitlistDTO dto = new WaitlistDTO();
        dto.setId(waitlist.getId());
        dto.setSessionId(waitlist.getLearningSession().getId());
        dto.setSessionCode(waitlist.getLearningSession().getSessionCode());
        dto.setProgramName(waitlist.getLearningSession().getLearningProgram().getName());
        dto.setSessionStartDateTime(waitlist.getLearningSession().getStartDateTime());
        dto.setSessionEndDateTime(waitlist.getLearningSession().getEndDateTime());
        dto.setSessionLocationName(waitlist.getLearningSession().getLocation() != null 
                ? waitlist.getLearningSession().getLocation().getName() 
                : null);
        dto.setUserId(waitlist.getUser().getId());
        dto.setUserName(waitlist.getUser().getFullName());
        dto.setUserEmail(waitlist.getUser().getEmail());
        dto.setUserEmployeeId(waitlist.getUser().getEmployeeId());
        dto.setPosition(waitlist.getPosition());
        dto.setStatus(waitlist.getStatus());
        dto.setJoinedAt(waitlist.getJoinedAt());
        dto.setNotifiedAt(waitlist.getNotifiedAt());
        dto.setNotes(waitlist.getNotes());
        dto.setCreatedAt(waitlist.getCreatedAt());
        dto.setUpdatedAt(waitlist.getUpdatedAt());
        return dto;
    }

    @Override
    public void cancelWaitlistBySession(Long sessionId) {
        log.info("Cancelling all waitlist entries for session {}", sessionId);

        // Find all active (WAITING) waitlist entries for this session
        List<Waitlist> activeWaitlists = waitlistRepository
                .findByLearningSessionIdAndStatus(sessionId, WaitlistStatus.WAITING);

        // Cancel each waitlist entry
        for (Waitlist waitlist : activeWaitlists) {
            waitlist.setStatus(WaitlistStatus.CANCELLED);
            waitlistRepository.save(waitlist);
            
            log.info("Cancelled waitlist entry {} for user {}", 
                    waitlist.getId(), waitlist.getUser().getId());
            
            // TODO: Send email notification to user about waitlist cancellation
            // notificationService.notifyWaitlistCancelled(waitlist);
        }

        log.info("Successfully cancelled {} waitlist entries for session {}", 
                activeWaitlists.size(), sessionId);
    }
}
