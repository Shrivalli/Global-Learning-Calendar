package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.NominationDTO;
import com.learning.globallearningcalendar.entity.*;
import com.learning.globallearningcalendar.exception.BadRequestException;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.*;
import com.learning.globallearningcalendar.service.INominationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NominationServiceImpl implements INominationService {

    private final NominationRepository nominationRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public NominationDTO.BulkNominationResponse nominateEmployees(
            NominationDTO.BulkNominationRequest request, 
            Long nominatorId) {
        
        log.info("Processing bulk nomination: {} employees for session {}", 
                request.getNomineeIds().size(), request.getSessionId());

        // Validate nominator
        User nominator = userRepository.findById(nominatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", nominatorId));

        // Validate session
        LearningSession session = learningSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", request.getSessionId()));

        // Check if session is in the past
        if (session.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot nominate for past sessions");
        }

        // Check if session is cancelled
        if (session.getStatus() == LearningSession.SessionStatus.CANCELLED) {
            throw new BadRequestException("Cannot nominate for cancelled sessions");
        }

        List<NominationDTO> successfulNominations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successful = 0;
        int failed = 0;

        for (Long nomineeId : request.getNomineeIds()) {
            try {
                // Validate nominee
                User nominee = userRepository.findById(nomineeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", nomineeId));

                // Check if nominee is in nominator's team
                if (nominee.getManager() == null || !nominee.getManager().getId().equals(nominatorId)) {
                    errors.add(String.format("%s %s is not in your team", 
                            nominee.getFirstName(), nominee.getLastName()));
                    failed++;
                    continue;
                }

                // Check for existing active nomination
                Optional<Nomination> existingNomination = nominationRepository
                        .findActiveNominationBySessionAndNominee(session.getId(), nomineeId);
                
                if (existingNomination.isPresent()) {
                    errors.add(String.format("%s %s already has an active nomination for this session", 
                            nominee.getFirstName(), nominee.getLastName()));
                    failed++;
                    continue;
                }

                // Check if user already has a booking for this session
                Optional<Booking> existingBooking = bookingRepository
                        .findByUserIdAndSessionId(nomineeId, session.getId());
                
                if (existingBooking.isPresent()) {
                    errors.add(String.format("%s %s already has a booking for this session", 
                            nominee.getFirstName(), nominee.getLastName()));
                    failed++;
                    continue;
                }

                // Create nomination
                Nomination nomination = Nomination.builder()
                        .session(session)
                        .nominee(nominee)
                        .nominator(nominator)
                        .nominationType(request.getNominationType())
                        .notes(request.getNotes())
                        .nominatedAt(LocalDateTime.now())
                        .build();

                // If MANDATORY, auto-create booking
                if (request.getNominationType() == Nomination.NominationType.MANDATORY) {
                    // Check seat availability
                    if (!session.hasAvailableSeats()) {
                        errors.add(String.format("No seats available for %s %s", 
                                nominee.getFirstName(), nominee.getLastName()));
                        failed++;
                        continue;
                    }

                    // Auto-assign seat
                    Integer assignedSeat = autoAssignSeat(session.getId(), session.getTotalSeats());

                    // Create booking with assigned seat
                    Booking booking = Booking.builder()
                            .user(nominee)
                            .learningSession(session)
                            .status(Booking.BookingStatus.CONFIRMED)
                            .bookingDate(LocalDateTime.now())
                            .bookingReference(generateBookingReference())
                            .seatNumber(assignedSeat)
                            .confirmationDate(LocalDateTime.now())
                            .build();

                    booking = bookingRepository.save(booking);

                    // Decrement available seats
                    session.decrementAvailableSeats();
                    learningSessionRepository.save(session);

                    // Link booking to nomination
                    nomination.setBooking(booking);
                    nomination.setStatus(Nomination.NominationStatus.COMPLETED);
                    
                    log.info("Created MANDATORY nomination and auto-booked seat {} for user {} in session {}", 
                            assignedSeat, nomineeId, session.getId());
                } else {
                    // RECOMMENDED - set status to PENDING
                    nomination.setStatus(Nomination.NominationStatus.PENDING);
                    log.info("Created RECOMMENDED nomination for user {} in session {}", 
                            nomineeId, session.getId());
                }

                nomination = nominationRepository.save(nomination);
                successfulNominations.add(toDTO(nomination));
                successful++;

            } catch (Exception e) {
                log.error("Error nominating user {}: {}", nomineeId, e.getMessage());
                errors.add(String.format("Failed to nominate user ID %d: %s", nomineeId, e.getMessage()));
                failed++;
            }
        }

        log.info("Bulk nomination completed: {} successful, {} failed", successful, failed);

        return NominationDTO.BulkNominationResponse.builder()
                .totalNominations(request.getNomineeIds().size())
                .successful(successful)
                .failed(failed)
                .errors(errors)
                .nominations(successfulNominations)
                .build();
    }

    @Override
    public List<NominationDTO> getNominationsForEmployee(Long employeeId) {
        return nominationRepository.findByNomineeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NominationDTO> getPendingNominationsForEmployee(Long employeeId) {
        return nominationRepository.findPendingByNomineeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getPendingNominationsCount(Long employeeId) {
        return nominationRepository.countPendingByNomineeId(employeeId);
    }

    @Override
    public List<NominationDTO> getActionableNominationsForEmployee(Long employeeId) {
        // Show PENDING nominations and MANDATORY nominations from last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return nominationRepository.findActionableByNomineeId(employeeId, sevenDaysAgo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getActionableNominationsCount(Long employeeId) {
        // Count PENDING nominations and MANDATORY nominations from last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return nominationRepository.countActionableByNomineeId(employeeId, sevenDaysAgo);
    }

    @Override
    public List<NominationDTO> getNominationsByManager(Long managerId) {
        return nominationRepository.findByNominatorId(managerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NominationDTO acceptNomination(Long nominationId, Long employeeId) {
        Nomination nomination = nominationRepository.findByIdWithDetails(nominationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nomination", "id", nominationId));

        // Verify nomination belongs to this employee
        if (!nomination.getNominee().getId().equals(employeeId)) {
            throw new BadRequestException("This nomination does not belong to you");
        }

        // Can only accept PENDING recommendations
        if (nomination.getStatus() != Nomination.NominationStatus.PENDING) {
            throw new BadRequestException("This nomination cannot be accepted (status: " + nomination.getStatus() + ")");
        }

        if (nomination.getNominationType() != Nomination.NominationType.RECOMMENDED) {
            throw new BadRequestException("Only RECOMMENDED nominations can be accepted");
        }

        LearningSession session = nomination.getSession();

        // Check if session still has seats
        if (!session.hasAvailableSeats()) {
            throw new BadRequestException("No seats available for this session");
        }

        // Check if user already has a booking
        Optional<Booking> existingBooking = bookingRepository
                .findByUserIdAndSessionId(employeeId, session.getId());
        
        if (existingBooking.isPresent()) {
            throw new BadRequestException("You already have a booking for this session");
        }

        // Create booking with PENDING status for seat selection
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .user(nomination.getNominee())
                .learningSession(session)
                .status(Booking.BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);

        // Note: Do NOT decrement available seats yet - will be done when user selects seat

        // Update nomination
        nomination.setStatus(Nomination.NominationStatus.ACCEPTED);
        nomination.setBooking(booking);
        nomination.setRespondedAt(LocalDateTime.now());
        nomination = nominationRepository.save(nomination);

        log.info("Employee {} accepted nomination {} and created PENDING booking {} - awaiting seat selection", 
                employeeId, nominationId, booking.getId());

        return toDTO(nomination);
    }

    @Override
    @Transactional
    public NominationDTO declineNomination(Long nominationId, Long employeeId) {
        Nomination nomination = nominationRepository.findByIdWithDetails(nominationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nomination", "id", nominationId));

        // Verify nomination belongs to this employee
        if (!nomination.getNominee().getId().equals(employeeId)) {
            throw new BadRequestException("This nomination does not belong to you");
        }

        // Can only decline PENDING recommendations
        if (nomination.getStatus() != Nomination.NominationStatus.PENDING) {
            throw new BadRequestException("This nomination cannot be declined (status: " + nomination.getStatus() + ")");
        }

        if (nomination.getNominationType() != Nomination.NominationType.RECOMMENDED) {
            throw new BadRequestException("Only RECOMMENDED nominations can be declined");
        }

        // Update nomination
        nomination.setStatus(Nomination.NominationStatus.DECLINED);
        nomination.setRespondedAt(LocalDateTime.now());
        nomination = nominationRepository.save(nomination);

        log.info("Employee {} declined nomination {}", employeeId, nominationId);

        return toDTO(nomination);
    }

    @Override
    @Transactional
    public NominationDTO acknowledgeNomination(Long nominationId, Long employeeId) {
        Nomination nomination = nominationRepository.findByIdWithDetails(nominationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nomination", "id", nominationId));

        // Verify nomination belongs to this employee
        if (!nomination.getNominee().getId().equals(employeeId)) {
            throw new BadRequestException("This nomination does not belong to you");
        }

        // Can only acknowledge MANDATORY COMPLETED nominations
        if (nomination.getNominationType() != Nomination.NominationType.MANDATORY) {
            throw new BadRequestException("Only MANDATORY nominations can be acknowledged");
        }

        if (nomination.getStatus() != Nomination.NominationStatus.COMPLETED) {
            throw new BadRequestException("This nomination is not completed");
        }

        // Mark as acknowledged
        nomination.setAcknowledgedAt(LocalDateTime.now());
        nomination = nominationRepository.save(nomination);

        log.info("Employee {} acknowledged MANDATORY nomination {}", employeeId, nominationId);

        return toDTO(nomination);
    }

    @Override
    public NominationDTO getNominationById(Long id) {
        Nomination nomination = nominationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nomination", "id", id));
        return toDTO(nomination);
    }

    private NominationDTO toDTO(Nomination nomination) {
        NominationDTO dto = NominationDTO.builder()
                .id(nomination.getId())
                .sessionId(nomination.getSession().getId())
                .nomineeId(nomination.getNominee().getId())
                .nominatorId(nomination.getNominator().getId())
                .nominationType(nomination.getNominationType())
                .status(nomination.getStatus())
                .notes(nomination.getNotes())
                .nominatedAt(nomination.getNominatedAt())
                .respondedAt(nomination.getRespondedAt())
                .acknowledgedAt(nomination.getAcknowledgedAt())
                .createdAt(nomination.getCreatedAt())
                .updatedAt(nomination.getUpdatedAt())
                .build();

        // Session details
        if (nomination.getSession() != null) {
            LearningSession session = nomination.getSession();
            dto.setSessionCode(session.getSessionCode());
            dto.setSessionStartDateTime(session.getStartDateTime());
            dto.setSessionEndDateTime(session.getEndDateTime());
            
            if (session.getLocation() != null) {
                dto.setSessionLocationName(session.getLocation().getName());
            }
            
            // Program name
            if (session.getLearningProgram() != null) {
                dto.setSessionName(session.getLearningProgram().getName());
            }
            
            // Instructor/Facilitator name
            dto.setSessionFacilitatorName(session.getInstructorName());
            
            // Calculate duration
            if (session.getStartDateTime() != null && session.getEndDateTime() != null) {
                long hours = java.time.Duration.between(session.getStartDateTime(), session.getEndDateTime()).toHours();
                long minutes = java.time.Duration.between(session.getStartDateTime(), session.getEndDateTime()).toMinutesPart();
                if (hours > 0 && minutes > 0) {
                    dto.setSessionDuration(hours + " hours " + minutes + " minutes");
                } else if (hours > 0) {
                    dto.setSessionDuration(hours + " hours");
                } else if (minutes > 0) {
                    dto.setSessionDuration(minutes + " minutes");
                }
            }
        }

        // Nominee details
        if (nomination.getNominee() != null) {
            User nominee = nomination.getNominee();
            dto.setNomineeFirstName(nominee.getFirstName());
            dto.setNomineeLastName(nominee.getLastName());
            dto.setNomineeEmail(nominee.getEmail());
        }

        // Nominator details
        if (nomination.getNominator() != null) {
            User nominator = nomination.getNominator();
            dto.setNominatorFirstName(nominator.getFirstName());
            dto.setNominatorLastName(nominator.getLastName());
        }

        // Booking details
        if (nomination.getBooking() != null) {
            dto.setBookingId(nomination.getBooking().getId());
        }

        return dto;
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Auto-assign the next available seat for a session
     */
    private Integer autoAssignSeat(Long sessionId, int totalSeats) {
        // Get all booked seats for this session
        List<Integer> bookedSeats = bookingRepository.findBookedSeatsBySessionId(sessionId);
        
        // Find first available seat
        for (int seatNumber = 1; seatNumber <= totalSeats; seatNumber++) {
            if (!bookedSeats.contains(seatNumber)) {
                return seatNumber;
            }
        }
        
        // No seat available (shouldn't happen if we check hasAvailableSeats first)
        throw new BadRequestException("No seats available for auto-assignment");
    }
}
