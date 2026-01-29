package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.BookingDTO;
import com.learning.globallearningcalendar.entity.*;
import com.learning.globallearningcalendar.exception.BadRequestException;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.BookingRepository;
import com.learning.globallearningcalendar.repository.LearningSessionRepository;
import com.learning.globallearningcalendar.repository.NominationRepository;
import com.learning.globallearningcalendar.repository.UserRepository;
import com.learning.globallearningcalendar.service.IBookingService;
import com.learning.globallearningcalendar.service.IWaitlistService;
import com.learning.globallearningcalendar.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final NominationRepository nominationRepository;
    private final IWaitlistService waitlistService;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return toDTO(booking);
    }

    @Override
    public BookingDTO getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingReference", bookingReference));
        return toDTO(booking);
    }

    @Override
    public BookingDTO createBooking(BookingDTO dto) {
        log.debug("createBooking called with DTO: {}", dto);

        if (dto == null) {
            log.error("BookingDTO is null");
            throw new BadRequestException("Booking data is required");
        }

        if (dto.getUserId() == null) {
            log.error("UserId is null in DTO: {}", dto);
            throw new BadRequestException("User ID is required");
        }

        if (dto.getSessionId() == null) {
            log.error("SessionId is null in DTO: {}", dto);
            throw new BadRequestException("Session ID is required");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));
        log.debug("Found user: {}", user.getId());

        LearningSession session = learningSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", dto.getSessionId()));
        log.debug("Found session: {} (availableSeats={}, totalSeats={})", session.getId(), session.getAvailableSeats(), session.getTotalSeats());

        // Check if user is eligible to book this session (location and role)
        if (!isUserEligibleForSession(user, session)) {
            log.error("User {} is not eligible to book session {}", dto.getUserId(), dto.getSessionId());
            throw new BadRequestException("You are not eligible to book this session based on location or role restrictions");
        }

        // Check for an active booking (we treat CANCELLED as non-active)
        if (bookingRepository.findByUserIdAndSessionId(dto.getUserId(), dto.getSessionId()).isPresent()) {
            // fetch any booking (including cancelled) to provide helpful debugging info
            bookingRepository.findAnyByUserIdAndSessionId(dto.getUserId(), dto.getSessionId()).ifPresent(existing -> {
                String msg = String.format("User already has a booking (id=%d, status=%s) for session %d", existing.getId(), existing.getStatus(), session.getId());
                log.error(msg);
                throw new BadRequestException(msg);
            });

            // fallback generic message
            log.error("User {} already has a booking for session {}", dto.getUserId(), dto.getSessionId());
            throw new BadRequestException("User already has a booking for this session");
        }

        if (session.getStatus() != LearningSession.SessionStatus.SCHEDULED) {
            log.error("Cannot book session {} because status is {}", session.getId(), session.getStatus());
            throw new BadRequestException("Cannot book a session that is not scheduled");
        }

        if (session.getStartDateTime().isBefore(LocalDateTime.now())) {
            log.error("Cannot book session {} because it has already started at {}", session.getId(), session.getStartDateTime());
            throw new BadRequestException("Cannot book a session that has already started");
        }

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setUser(user);
        booking.setLearningSession(session);
        booking.setBookingDate(LocalDateTime.now());
        booking.setAttendanceStatus(Booking.AttendanceStatus.NOT_MARKED);
        booking.setCompletionStatus(Booking.CompletionStatus.NOT_STARTED);
        booking.setNotes(dto.getNotes());

        // Validate and assign seat number if provided
        if (dto.getSeatNumber() != null) {
            // Check if the seat is already taken (including PENDING_APPROVAL bookings)
            Optional<Booking> existingSeatBooking = bookingRepository.findBySessionIdAndSeatNumber(
                    dto.getSessionId(), dto.getSeatNumber());
            
            if (existingSeatBooking.isPresent()) {
                Booking existingBooking = existingSeatBooking.get();
                String seatStatus = existingBooking.getStatus().toString();
                log.error("Seat {} is already booked for session {} (Booking ID: {}, Status: {})", 
                         dto.getSeatNumber(), dto.getSessionId(), existingBooking.getId(), seatStatus);
                throw new BadRequestException(String.format(
                    "Seat %d is already booked. Please select a different seat.", dto.getSeatNumber()));
            }
            
            // Validate seat number is within range
            if (dto.getSeatNumber() < 1 || dto.getSeatNumber() > session.getTotalSeats()) {
                log.error("Invalid seat number {} for session {} (total seats: {})", 
                         dto.getSeatNumber(), dto.getSessionId(), session.getTotalSeats());
                throw new BadRequestException(String.format(
                    "Invalid seat number. Please choose between 1 and %d.", session.getTotalSeats()));
            }
            
            booking.setSeatNumber(dto.getSeatNumber());
            log.debug("Assigned seat number {} to booking", dto.getSeatNumber());
        }

        // Check if employee has a manager
        if (user.getManager() == null) {
            log.warn("User {} does not have a manager assigned. Booking will be auto-approved.", user.getId());
        }

        if (session.hasAvailableSeats()) {
            log.debug("Session has available seats ({}). Setting booking to PENDING_APPROVAL status.", session.getAvailableSeats());

            // If employee has a manager, require approval
            if (user.getManager() != null) {
                booking.setStatus(Booking.BookingStatus.PENDING_APPROVAL);
                booking.setManagerNotified(false);
                // IMPORTANT: Seat is locked immediately when booking is created (PENDING_APPROVAL)
                // This prevents race conditions where multiple employees try to book the same seat
                // Seat will be released if booking is REJECTED or CANCELLED
                session.decrementAvailableSeats();
                learningSessionRepository.save(session);
                log.debug("Seat decremented for PENDING_APPROVAL booking. Available seats: {}", session.getAvailableSeats());
            } else {
                // No manager - auto-approve
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                booking.setConfirmationDate(LocalDateTime.now());
                session.decrementAvailableSeats();
                learningSessionRepository.save(session);
            }
            
            Booking saved = bookingRepository.save(booking);
            log.debug("Booking saved with id {} and status {}", saved.getId(), saved.getStatus());
            
            // Send notification for auto-approved bookings
            if (saved.getStatus() == Booking.BookingStatus.CONFIRMED) {
                try {
                    notificationService.notifyBookingConfirmed(saved);
                    log.info("Notification sent for auto-confirmed booking {}", saved.getId());
                } catch (Exception e) {
                    log.error("Failed to send notification for auto-confirmed booking {}: {}", saved.getId(), e.getMessage());
                }
            }
            return toDTO(saved);
        } else {
            // Session is full - add user to the new waitlist table instead of creating WAITLISTED booking
            log.info("Session {} is full. Adding user {} to waitlist instead of creating WAITLISTED booking", 
                    session.getId(), user.getId());
            
            try {
                // Use the new waitlist service to add user to waitlist
                waitlistService.joinWaitlist(session.getId(), user.getId(), dto.getNotes());
                log.info("User {} successfully added to waitlist for session {}", user.getId(), session.getId());
                
                // Send waitlist notification
                try {
                    Booking tempBooking = Booking.builder()
                            .user(user)
                            .learningSession(session)
                            .build();
                    notificationService.notifyBookingWaitlisted(tempBooking);
                    log.info("Notification sent for waitlist addition for user {} session {}", user.getId(), session.getId());
                } catch (Exception e) {
                    log.error("Failed to send waitlist notification: {}", e.getMessage());
                }
                
                // Throw exception to indicate to frontend that user was waitlisted
                throw new BadRequestException("Session is full. You have been added to the waitlist and will be notified when a seat becomes available.");
            } catch (BadRequestException e) {
                // Re-throw BadRequestException from waitlistService (e.g., already in waitlist)
                throw e;
            } catch (Exception e) {
                log.error("Error adding user {} to waitlist for session {}: {}", user.getId(), session.getId(), e.getMessage(), e);
                throw new BadRequestException("Session is full and could not add to waitlist: " + e.getMessage());
            }
        }
    }

    @Override
    public BookingDTO updateBooking(Long id, BookingDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setNotes(dto.getNotes());

        Booking updated = bookingRepository.save(booking);
        return toDTO(updated);
    }

    @Override
    public BookingDTO cancelBooking(Long id, String cancellationReason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed booking");
        }

        LearningSession session = booking.getLearningSession();
        boolean shouldReleaseSeats = booking.getStatus() == Booking.BookingStatus.CONFIRMED || 
                                     booking.getStatus() == Booking.BookingStatus.PENDING_APPROVAL;

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationDate(LocalDateTime.now());
        booking.setCancellationReason(cancellationReason);

        if (shouldReleaseSeats) {
            session.incrementAvailableSeats();
            session = learningSessionRepository.saveAndFlush(session);
            log.debug("Seat released due to cancellation. Available seats: {}", session.getAvailableSeats());

            // NEW: Process waitlist using the new waitlist table
            // This will automatically confirm the first person in the waitlist if available
            log.info("Processing waitlist for session {} after booking cancellation. Available seats: {}", 
                    session.getId(), session.getAvailableSeats());
            waitlistService.processWaitlistForCancellation(session.getId());
            
            // LEGACY: Also check for old WAITLISTED bookings and migrate them
            migrateOldWaitlistedBookings(session.getId());
        }

        Booking updated = bookingRepository.save(booking);
        log.info("Booking {} cancelled. Reason: {}", id, cancellationReason);
        
        // Send cancellation notification
        try {
            notificationService.notifyBookingCancelled(updated, cancellationReason);
            log.info("Notification sent for cancelled booking {}", id);
        } catch (Exception e) {
            log.error("Failed to send cancellation notification for booking {}: {}", id, e.getMessage());
        }
        
        return toDTO(updated);
    }

    @Override
    public List<BookingDTO> getBookingsByUser(Long userId) {
        // Include all bookings including CANCELLED so frontend can display them in reports
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDTO> getBookingsByUser(Long userId, Pageable pageable) {
        // Include all bookings including CANCELLED so frontend can display them in reports
        return bookingRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    @Override
    public List<BookingDTO> getBookingsBySession(Long sessionId) {
        return bookingRepository.findBySessionIdExcludingStatus(sessionId, Booking.BookingStatus.CANCELLED).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO getBookingByUserAndSession(Long userId, Long sessionId) {
        Booking booking = bookingRepository.findByUserIdAndSessionId(userId, sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "userId and sessionId", userId + ", " + sessionId));
        return toDTO(booking);
    }

    @Override
    public BookingDTO getAnyBookingByUserAndSession(Long userId, Long sessionId) {
        return bookingRepository.findAnyByUserIdAndSessionId(userId, sessionId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public List<BookingDTO> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByUserAndStatus(Long userId, Booking.BookingStatus status) {
        return bookingRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getUpcomingBookingsByUser(Long userId) {
        return bookingRepository.findUpcomingBookingsByUserExcludingStatus(userId, LocalDateTime.now(), Booking.BookingStatus.CANCELLED).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getCompletedBookingsByUser(Long userId) {
        return bookingRepository.findCompletedBookingsByUser(userId, LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getWaitlistedBookingsBySession(Long sessionId) {
        return bookingRepository.findWaitlistedBookingsBySession(sessionId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO confirmBooking(Long id, Long approvedById) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() != Booking.BookingStatus.PENDING &&
            booking.getStatus() != Booking.BookingStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only pending bookings can be confirmed");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approvedById));

        // Verify that the approver is the employee's manager
        if (booking.getUser().getManager() != null &&
            !booking.getUser().getManager().getId().equals(approvedById)) {
            throw new BadRequestException("Only the employee's direct manager can approve this booking");
        }

        // Check if session still has available seats (should not happen as seat was already decremented)
        LearningSession session = booking.getLearningSession();
        if (!session.hasAvailableSeats() && booking.getStatus() == Booking.BookingStatus.PENDING) {
            // Only throw error for PENDING bookings (not PENDING_APPROVAL which already decremented seat)
            throw new BadRequestException("Session no longer has available seats");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmationDate(LocalDateTime.now());
        booking.setApprovedBy(approvedBy);
        booking.setApprovalDate(LocalDateTime.now());

        // Note: Seat was already decremented when booking was created with PENDING_APPROVAL status
        // No need to decrement again here

        Booking updated = bookingRepository.save(booking);
        
        // Send booking approval notification
        try {
            notificationService.notifyBookingApproved(updated);
            log.info("Notification sent for approved booking {}", id);
        } catch (Exception e) {
            log.error("Failed to send approval notification for booking {}: {}", id, e.getMessage());
        }
        
        return toDTO(updated);
    }

    @Override
    public BookingDTO markAttendance(Long id, Booking.AttendanceStatus attendanceStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setAttendanceStatus(attendanceStatus);
        booking.setAttendanceMarkedAt(LocalDateTime.now());

        if (attendanceStatus == Booking.AttendanceStatus.ABSENT) {
            booking.setStatus(Booking.BookingStatus.NO_SHOW);
        }

        Booking updated = bookingRepository.save(booking);
        return toDTO(updated);
    }

    @Override
    public BookingDTO markCompletion(Long id, Booking.CompletionStatus completionStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setCompletionStatus(completionStatus);

        if (completionStatus == Booking.CompletionStatus.COMPLETED) {
            booking.setCompletionDate(LocalDateTime.now());
            booking.setStatus(Booking.BookingStatus.COMPLETED);
        }

        Booking updated = bookingRepository.save(booking);
        return toDTO(updated);
    }

    @Override
    public BookingDTO submitFeedback(Long id, Integer rating, String comments) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        booking.setFeedbackRating(rating);
        booking.setFeedbackComments(comments);

        Booking updated = bookingRepository.save(booking);
        return toDTO(updated);
    }

    @Override
    public Long countConfirmedBookingsByUser(Long userId) {
        return bookingRepository.countConfirmedBookingsByUser(userId);
    }

    @Override
    public Long countCompletedBookingsByUser(Long userId) {
        return bookingRepository.countCompletedBookingsByUser(userId);
    }

    @Override
    public List<BookingDTO> getBookingsByBusinessUnitAndDateRange(Long buId, LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByBusinessUnitAndDateRange(buId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByLocationAndDateRange(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByLocationAndDateRange(locationId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getTeamBookings(Long managerId) {
        return bookingRepository.findTeamBookings(managerId, LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Booking.AttendanceStatus, Long> getAttendanceStatsBySession(Long sessionId) {
        List<Object[]> stats = bookingRepository.getAttendanceStatsBySession(sessionId);
        Map<Booking.AttendanceStatus, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            if (stat[0] != null) {
                result.put((Booking.AttendanceStatus) stat[0], (Long) stat[1]);
            }
        }
        return result;
    }

    @Override
    public Double getAverageFeedbackRatingByProgram(Long programId) {
        return bookingRepository.getAverageFeedbackRatingByProgram(programId);
    }

    @Override
    public BookingDTO rejectBooking(Long id, Long rejectedById, String rejectionReason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() != Booking.BookingStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only pending approval bookings can be rejected");
        }

        User rejectedBy = userRepository.findById(rejectedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", rejectedById));

        // Verify that the rejector is the employee's manager
        if (booking.getUser().getManager() != null &&
            !booking.getUser().getManager().getId().equals(rejectedById)) {
            throw new BadRequestException("Only the employee's direct manager can reject this booking");
        }

        booking.setStatus(Booking.BookingStatus.REJECTED);
        booking.setRejectedBy(rejectedBy);
        booking.setRejectionDate(LocalDateTime.now());
        booking.setRejectionReason(rejectionReason);

        // Release the seat back to available pool
        LearningSession session = booking.getLearningSession();
        session.incrementAvailableSeats();
        learningSessionRepository.save(session);
        log.debug("Seat released due to rejection. Available seats: {}", session.getAvailableSeats());

        Booking updated = bookingRepository.save(booking);
        
        // Send booking rejection notification
        try {
            notificationService.notifyBookingRejected(updated, rejectionReason);
            log.info("Notification sent for rejected booking {}", id);
        } catch (Exception e) {
            log.error("Failed to send rejection notification for booking {}: {}", id, e.getMessage());
        }
        
        return toDTO(updated);
    }

    @Override
    public List<BookingDTO> getPendingApprovalsByManager(Long managerId) {
        return bookingRepository.findPendingApprovalsByManager(managerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getAllPendingApprovals() {
        return bookingRepository.findByStatus(Booking.BookingStatus.PENDING_APPROVAL).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO markManagerNotified(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        booking.setManagerNotified(true);
        booking.setManagerNotifiedDate(LocalDateTime.now());

        Booking updated = bookingRepository.save(booking);
        return toDTO(updated);
    }

    @Override
    public BookingDTO getRejectedBookingByUserAndSession(Long userId, Long sessionId) {
        return bookingRepository.findRejectedBookingByUserAndSession(userId, sessionId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public boolean canUserRebookSession(Long userId, Long sessionId) {
        // User can rebook if:
        // 1. They have a REJECTED booking for this session, OR
        // 2. They have no active booking (no PENDING_APPROVAL, CONFIRMED, or WAITLISTED)
        
        Optional<Booking> activeBooking = bookingRepository.findByUserIdAndSessionId(userId, sessionId);
        
        // If there's an active booking, they cannot rebook
        if (activeBooking.isPresent()) {
            return false;
        }
        
        // If there's no active booking, they can book/rebook
        return true;
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingDTO toDTO(Booking booking) {
        BookingDTO dto = BookingDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus())
                .seatNumber(booking.getSeatNumber())
                .waitlistPosition(booking.getWaitlistPosition())
                .bookingDate(booking.getBookingDate())
                .confirmationDate(booking.getConfirmationDate())
                .cancellationDate(booking.getCancellationDate())
                .cancellationReason(booking.getCancellationReason())
                .attendanceStatus(booking.getAttendanceStatus())
                .attendanceMarkedAt(booking.getAttendanceMarkedAt())
                .completionStatus(booking.getCompletionStatus())
                .completionDate(booking.getCompletionDate())
                .feedbackRating(booking.getFeedbackRating())
                .feedbackComments(booking.getFeedbackComments())
                .approvalDate(booking.getApprovalDate())
                .rejectionDate(booking.getRejectionDate())
                .rejectionReason(booking.getRejectionReason())
                .managerNotified(booking.getManagerNotified())
                .managerNotifiedDate(booking.getManagerNotifiedDate())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserName(booking.getUser().getFullName());
            dto.setUserEmail(booking.getUser().getEmail());
            dto.setUserEmployeeId(booking.getUser().getEmployeeId());

            // Include business unit information
            if (booking.getUser().getBusinessUnit() != null) {
                dto.setUserBusinessUnitId(booking.getUser().getBusinessUnit().getId());
                dto.setUserBusinessUnitName(booking.getUser().getBusinessUnit().getName());
            }

            // Include manager information
            if (booking.getUser().getManager() != null) {
                dto.setManagerId(booking.getUser().getManager().getId());
                dto.setManagerName(booking.getUser().getManager().getFullName());
                dto.setManagerEmail(booking.getUser().getManager().getEmail());
            }
        }

        if (booking.getLearningSession() != null) {
            LearningSession session = booking.getLearningSession();
            dto.setSessionId(session.getId());
            dto.setSessionCode(session.getSessionCode());
            dto.setSessionStartDateTime(session.getStartDateTime());
            dto.setSessionEndDateTime(session.getEndDateTime());

            // Calculate session duration in hours
            if (session.getStartDateTime() != null && session.getEndDateTime() != null) {
                long minutes = java.time.Duration.between(
                    session.getStartDateTime(),
                    session.getEndDateTime()
                ).toMinutes();
                dto.setSessionDurationHours(minutes / 60.0);
            }

            if (session.getLearningProgram() != null) {
                dto.setProgramName(session.getLearningProgram().getName());
                dto.setProgramCode(session.getLearningProgram().getCode());
            }

            if (session.getLocation() != null) {
                dto.setSessionLocationName(session.getLocation().getName());
            }
        }

        if (booking.getApprovedBy() != null) {
            dto.setApprovedById(booking.getApprovedBy().getId());
            dto.setApprovedByName(booking.getApprovedBy().getFullName());
        }

        if (booking.getRejectedBy() != null) {
            dto.setRejectedById(booking.getRejectedBy().getId());
            dto.setRejectedByName(booking.getRejectedBy().getFullName());
        }

        // Check if this booking came from a nomination
        Optional<Nomination> nomination = nominationRepository.findByBookingId(booking.getId());
        if (nomination.isPresent()) {
            dto.setNominationType(nomination.get().getNominationType().toString());
        }

        return dto;
    }

    /**
     * MIGRATION HELPER: Convert old WAITLISTED bookings to new waitlist table entries
     * This ensures backward compatibility with existing WAITLISTED bookings
     */
    private void migrateOldWaitlistedBookings(Long sessionId) {
        try {
            List<Booking> oldWaitlistedBookings = bookingRepository.findWaitlistedBookingsBySession(sessionId);
            
            if (oldWaitlistedBookings.isEmpty()) {
                return;
            }
            
            log.info("Found {} old WAITLISTED bookings for session {}. Migrating to new waitlist table...", 
                    oldWaitlistedBookings.size(), sessionId);
            
            for (Booking waitlistedBooking : oldWaitlistedBookings) {
                try {
                    // Check if user is already in new waitlist
                    if (!waitlistService.isUserInWaitlist(sessionId, waitlistedBooking.getUser().getId())) {
                        // Add to new waitlist table
                        waitlistService.joinWaitlist(
                            sessionId, 
                            waitlistedBooking.getUser().getId(), 
                            "Migrated from old WAITLISTED booking #" + waitlistedBooking.getId()
                        );
                        
                        // Delete the old WAITLISTED booking
                        bookingRepository.delete(waitlistedBooking);
                        
                        log.info("Migrated user {} from WAITLISTED booking {} to new waitlist table", 
                                waitlistedBooking.getUser().getId(), waitlistedBooking.getId());
                    }
                } catch (Exception e) {
                    log.error("Error migrating WAITLISTED booking {} to new waitlist: {}", 
                            waitlistedBooking.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during waitlist migration for session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void cancelBookingsBySession(Long sessionId, String cancellationReason) {
        // Find all active bookings for this session
        List<Booking> activeBookings = bookingRepository.findBySessionId(sessionId).stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED 
                        || booking.getStatus() == Booking.BookingStatus.PENDING_APPROVAL 
                        || booking.getStatus() == Booking.BookingStatus.WAITLISTED)
                .collect(Collectors.toList());

        log.info("Cancelling {} active bookings for session {}", activeBookings.size(), sessionId);

        // Cancel each booking
        for (Booking booking : activeBookings) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setCancellationDate(LocalDateTime.now());
            booking.setCancellationReason(cancellationReason != null 
                    ? cancellationReason 
                    : "Session cancelled");
            bookingRepository.save(booking);
            
            log.info("Cancelled booking {} for user {}", booking.getId(), booking.getUser().getId());
            
            // TODO: Send email notification to user about booking cancellation
            // notificationService.notifyBookingCancelled(booking);
        }

        // Cancel any waitlist entries for this session
        try {
            waitlistService.cancelWaitlistBySession(sessionId);
        } catch (Exception e) {
            log.error("Error cancelling waitlist for session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public List<Integer> getBookedSeatsForSession(Long sessionId) {
        log.debug("Getting booked seats for session: {}", sessionId);
        
        // Get all bookings that are not CANCELLED or REJECTED
        // This includes PENDING_APPROVAL, CONFIRMED, and COMPLETED bookings
        List<Booking> activeBookings = bookingRepository.findBySessionId(sessionId).stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED 
                          && b.getStatus() != Booking.BookingStatus.REJECTED)
                .collect(Collectors.toList());
        
        // Extract seat numbers (filter out null values)
        List<Integer> bookedSeats = activeBookings.stream()
                .map(Booking::getSeatNumber)
                .filter(seatNumber -> seatNumber != null)
                .collect(Collectors.toList());
        
        log.debug("Found {} booked seats for session {}: {}", bookedSeats.size(), sessionId, bookedSeats);
        return bookedSeats;
    }

    @Override
    public Map<Integer, String> getSeatsStatusForSession(Long sessionId) {
        log.debug("Getting seat status map for session: {}", sessionId);
        
        // Get all active bookings (not CANCELLED or REJECTED)
        List<Booking> activeBookings = bookingRepository.findBySessionId(sessionId).stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED 
                          && b.getStatus() != Booking.BookingStatus.REJECTED)
                .collect(Collectors.toList());
        
        // Create a map of seat number to booking status
        Map<Integer, String> seatStatusMap = activeBookings.stream()
                .filter(b -> b.getSeatNumber() != null)
                .collect(Collectors.toMap(
                    Booking::getSeatNumber,
                    b -> b.getStatus().toString(),
                    (existing, replacement) -> existing // Keep first if duplicate
                ));
        
        log.debug("Found {} seats with status for session {}", seatStatusMap.size(), sessionId);
        return seatStatusMap;
    }
    
    /**
     * Check if a user is eligible to book a session based on location and role
     */
    private boolean isUserEligibleForSession(User user, LearningSession session) {
        // Check role eligibility - compare role types, not role IDs (handles multiple roles with same type)
        if (session.getTargetRoles() != null && !session.getTargetRoles().isEmpty()) {
            boolean hasRole = session.getTargetRoles().stream()
                .anyMatch(role -> role.getRoleType().equals(user.getRole().getRoleType()));
            if (!hasRole) {
                log.debug("User {} does not have required role for session {}", user.getId(), session.getId());
                return false;
            }
        }
        
        // Check location eligibility based on delivery mode
        DeliveryMode mode = session.getDeliveryMode() != null ? session.getDeliveryMode() : DeliveryMode.OFFLINE;
        
        if (mode == DeliveryMode.OFFLINE) {
            // Offline sessions: only users in the same location
            if (session.getLocation() != null && user.getLocation() != null) {
                boolean sameLocation = session.getLocation().getId().equals(user.getLocation().getId());
                if (!sameLocation) {
                    log.debug("User location {} != session location {} for offline session", 
                        user.getLocation().getId(), session.getLocation().getId());
                }
                return sameLocation;
            }
            // If session or user has no location, allow (backward compatibility)
            return true;
        } else {
            // Online sessions: check location scope
            LocationScope scope = session.getLocationScope() != null ? session.getLocationScope() : LocationScope.ALL_LOCATIONS;
            
            if (scope == LocationScope.ALL_LOCATIONS) {
                return true;
            } else {
                // SPECIFIC_LOCATION: check if user's location is in session's target locations
                if (session.getTargetLocations() != null && !session.getTargetLocations().isEmpty()) {
                    // User must be in one of the target locations
                    if (user.getLocation() != null) {
                        boolean inTargetLocation = session.getTargetLocations().stream()
                            .anyMatch(location -> location.getId().equals(user.getLocation().getId()));
                        if (!inTargetLocation) {
                            log.debug("User location {} not in target locations for location-specific online session", 
                                user.getLocation().getId());
                        }
                        return inTargetLocation;
                    }
                    // User has no location, cannot access specific location sessions
                    log.debug("User has no location, cannot access location-specific online session");
                    return false;
                } else {
                    // Fallback: if no target locations specified, check session's primary location
                    if (session.getLocation() != null && user.getLocation() != null) {
                        boolean sameLocation = session.getLocation().getId().equals(user.getLocation().getId());
                        if (!sameLocation) {
                            log.debug("User location {} != session location {} for location-specific online session", 
                                user.getLocation().getId(), session.getLocation().getId());
                        }
                        return sameLocation;
                    }
                    return true;
                }
            }
        }
    }

    @Override
    @Transactional
    public BookingDTO selectSeat(Long bookingId, Integer seatNumber) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Can only select seat for PENDING bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BadRequestException("Can only select seat for PENDING bookings. Current status: " + booking.getStatus());
        }

        LearningSession session = booking.getLearningSession();

        // Validate seat number
        if (seatNumber < 1 || seatNumber > session.getTotalSeats()) {
            throw new BadRequestException("Invalid seat number. Must be between 1 and " + session.getTotalSeats());
        }

        // Check if seat is already taken
        List<Integer> bookedSeats = getBookedSeatsForSession(session.getId());
        if (bookedSeats.contains(seatNumber)) {
            throw new BadRequestException("Seat " + seatNumber + " is already taken");
        }

        // Assign seat and confirm booking
        booking.setSeatNumber(seatNumber);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmationDate(LocalDateTime.now());

        // Decrement available seats
        session.decrementAvailableSeats();
        learningSessionRepository.save(session);

        booking = bookingRepository.save(booking);

        log.info("User {} selected seat {} for session {} - booking confirmed", 
                booking.getUser().getId(), seatNumber, session.getId());

        return toDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO changeSeat(Long bookingId, Integer newSeatNumber) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Can only change seat for CONFIRMED bookings
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Can only change seat for CONFIRMED bookings. Current status: " + booking.getStatus());
        }

        LearningSession session = booking.getLearningSession();

        // Validate seat number
        if (newSeatNumber < 1 || newSeatNumber > session.getTotalSeats()) {
            throw new BadRequestException("Invalid seat number. Must be between 1 and " + session.getTotalSeats());
        }

        // Check if new seat is already taken (excluding current booking)
        List<Booking> sessionBookings = bookingRepository.findBySessionId(session.getId());
        for (Booking b : sessionBookings) {
            if (!b.getId().equals(bookingId) && 
                b.getSeatNumber() != null && 
                b.getSeatNumber().equals(newSeatNumber) &&
                (b.getStatus() == Booking.BookingStatus.CONFIRMED || 
                 b.getStatus() == Booking.BookingStatus.PENDING_APPROVAL)) {
                throw new BadRequestException("Seat " + newSeatNumber + " is already taken");
            }
        }

        Integer oldSeatNumber = booking.getSeatNumber();
        booking.setSeatNumber(newSeatNumber);
        booking = bookingRepository.save(booking);

        log.info("User {} changed seat from {} to {} for session {}", 
                booking.getUser().getId(), oldSeatNumber, newSeatNumber, session.getId());

        return toDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO requestCancellation(Long bookingId, Long userId, String cancellationReason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("This booking does not belong to you");
        }

        // Can only request cancellation for CONFIRMED bookings
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Can only cancel CONFIRMED bookings. Current status: " + booking.getStatus());
        }

        // Check if this booking is from a MANDATORY nomination
        Optional<Nomination> nominationOpt = nominationRepository.findByBookingId(bookingId);
        
        if (nominationOpt.isPresent() && nominationOpt.get().getNominationType() == Nomination.NominationType.MANDATORY) {
            // Require manager approval for MANDATORY nominations
            User manager = booking.getUser().getManager();
            if (manager == null) {
                throw new BadRequestException("Cannot request cancellation: No manager assigned");
            }

            // Clear any previous rejection fields before requesting cancellation
            booking.setRejectedBy(null);
            booking.setRejectionDate(null);
            booking.setRejectionReason(null);
            
            booking.setStatus(Booking.BookingStatus.PENDING_CANCELLATION);
            booking.setCancellationReason(cancellationReason);
            booking.setCancellationDate(LocalDateTime.now());

            booking = bookingRepository.saveAndFlush(booking);
            log.info("Cancellation request created for MANDATORY booking {} by user {}. Requires manager {} approval.", 
                    bookingId, userId, manager.getId());
            
            return toDTO(booking);
        } else {
            // Regular cancellation (existing flow) for non-MANDATORY or RECOMMENDED bookings
            log.info("Direct cancellation for non-MANDATORY booking {} by user {}", bookingId, userId);
            return cancelBooking(bookingId, cancellationReason);
        }
    }

    @Override
    @Transactional
    public BookingDTO approveCancellation(Long bookingId, Long managerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Validate booking is in PENDING_CANCELLATION status
        if (booking.getStatus() != Booking.BookingStatus.PENDING_CANCELLATION) {
            throw new BadRequestException("Booking is not pending cancellation. Current status: " + booking.getStatus());
        }

        // Validate manager relationship
        User manager = booking.getUser().getManager();
        if (manager == null || !manager.getId().equals(managerId)) {
            throw new BadRequestException("You are not authorized to approve this cancellation request");
        }

        // Cancel the booking (using existing cancelBooking logic)
        LearningSession session = booking.getLearningSession();
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationDate(LocalDateTime.now());
        // Keep the original cancellation reason from the request
        
        // Release seat
        session.incrementAvailableSeats();
        session = learningSessionRepository.saveAndFlush(session);
        log.debug("Seat released due to approved cancellation. Available seats: {}", session.getAvailableSeats());

        // Process waitlist
        log.info("Processing waitlist for session {} after approved cancellation. Available seats: {}", 
                session.getId(), session.getAvailableSeats());
        waitlistService.processWaitlistForCancellation(session.getId());
        
        // Migrate old waitlisted bookings
        migrateOldWaitlistedBookings(session.getId());

        booking = bookingRepository.save(booking);
        log.info("Manager {} approved cancellation request for booking {}", managerId, bookingId);
        
        // Send cancellation approval notification
        try {
            notificationService.notifyCancellationApproved(booking);
            log.info("Notification sent for approved cancellation of booking {}", bookingId);
        } catch (Exception e) {
            log.error("Failed to send cancellation approval notification for booking {}: {}", bookingId, e.getMessage());
        }
        
        return toDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO rejectCancellationRequest(Long bookingId, Long managerId, String rejectionReason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Validate booking is in PENDING_CANCELLATION status
        if (booking.getStatus() != Booking.BookingStatus.PENDING_CANCELLATION) {
            throw new BadRequestException("Booking is not pending cancellation. Current status: " + booking.getStatus());
        }

        // Validate manager relationship
        User manager = booking.getUser().getManager();
        if (manager == null || !manager.getId().equals(managerId)) {
            throw new BadRequestException("You are not authorized to reject this cancellation request");
        }

        // Reject the cancellation - keep booking as CONFIRMED
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setRejectionReason(rejectionReason);
        booking.setRejectionDate(LocalDateTime.now());
        booking.setRejectedBy(manager);
        booking.setCancellationReason(null); // Clear the cancellation request reason
        booking.setCancellationDate(null); // Clear the cancellation date

        booking = bookingRepository.save(booking);
        log.info("Manager {} rejected cancellation request for booking {}. Reason: {}", 
                managerId, bookingId, rejectionReason);
        
        // Send cancellation rejection notification
        try {
            notificationService.notifyCancellationRejected(booking, rejectionReason);
            log.info("Notification sent for rejected cancellation of booking {}", bookingId);
        } catch (Exception e) {
            log.error("Failed to send cancellation rejection notification for booking {}: {}", bookingId, e.getMessage());
        }
        
        return toDTO(booking);
    }

    @Override
    public List<BookingDTO> getPendingCancellationsByManager(Long managerId) {
        return bookingRepository.findPendingCancellationsByManager(managerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
