package com.learning.globallearningcalendar.service.impl;

import com.learning.globallearningcalendar.dto.LearningSessionDTO;
import com.learning.globallearningcalendar.entity.*;
import com.learning.globallearningcalendar.exception.BadRequestException;
import com.learning.globallearningcalendar.exception.ResourceNotFoundException;
import com.learning.globallearningcalendar.repository.*;
import com.learning.globallearningcalendar.service.ILearningSessionService;
import com.learning.globallearningcalendar.service.IBookingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningSessionServiceImpl implements ILearningSessionService {

    private final LearningSessionRepository learningSessionRepository;
    private final LearningProgramRepository learningProgramRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final IBookingService bookingService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final Logger log = LoggerFactory.getLogger(LearningSessionServiceImpl.class);

    @Override
    public List<LearningSessionDTO> getAllSessions() {
        return learningSessionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LearningSessionDTO> getAllSessionsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                
        // Get active sessions with roles eagerly loaded, then filter by visibility
        return learningSessionRepository.findAllActiveWithRoles().stream()
                .filter(session -> Boolean.TRUE.equals(session.getIsActive()))
                .filter(session -> session.getStatus() == LearningSession.SessionStatus.SCHEDULED)
                .filter(session -> session.getStartDateTime().isAfter(LocalDateTime.now()))
                .filter(session -> isSessionVisibleToUser(session, user))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LearningSessionDTO> getAllSessionsForUserIncludingSubordinates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Get subordinate role types based on user's role hierarchy
        Set<Role.RoleType> visibleRoleTypes = getVisibleRoleTypesForUser(user.getRole().getRoleType());
        
        // Get active sessions with roles eagerly loaded, then filter
        return learningSessionRepository.findAllActiveWithRoles().stream()
                .filter(session -> Boolean.TRUE.equals(session.getIsActive()))
                .filter(session -> session.getStatus() == LearningSession.SessionStatus.SCHEDULED)
                .filter(session -> session.getStartDateTime().isAfter(LocalDateTime.now()))
                .filter(session -> isSessionVisibleToUserOrSubordinates(session, user, visibleRoleTypes))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get role types visible to a user (their own role + subordinate roles)
     */
    private Set<Role.RoleType> getVisibleRoleTypesForUser(Role.RoleType userRoleType) {
        Set<Role.RoleType> visibleRoles = new HashSet<>();
        visibleRoles.add(userRoleType); // User's own role
        
        // Add subordinate roles based on hierarchy
        switch (userRoleType) {
            case SYSTEM_ADMIN:
                visibleRoles.add(Role.RoleType.LD_ADMIN);
                // Fall through
            case LD_ADMIN:
                visibleRoles.add(Role.RoleType.LD_LEADER);
                // Fall through
            case LD_LEADER:
                visibleRoles.add(Role.RoleType.BU_LEADER);
                // Fall through
            case BU_LEADER:
                visibleRoles.add(Role.RoleType.MANAGER);
                // Fall through
            case MANAGER:
                visibleRoles.add(Role.RoleType.EMPLOYEE);
                break;
            case EMPLOYEE:
                // Employees only see their own role
                break;
        }
        
        return visibleRoles;
    }
    
    /**
     * Check if session is visible to user or their subordinates
     */
    private boolean isSessionVisibleToUserOrSubordinates(LearningSession session, User user, Set<Role.RoleType> visibleRoleTypes) {
        // Check if session targets any of the visible role types
        if (session.getTargetRoles() != null && !session.getTargetRoles().isEmpty()) {
            boolean hasVisibleRole = session.getTargetRoles().stream()
                .anyMatch(role -> visibleRoleTypes.contains(role.getRoleType()));
            if (!hasVisibleRole) {
                return false;
            }
        }
        
        // Check location eligibility (same logic as before)
        DeliveryMode mode = session.getDeliveryMode() != null ? session.getDeliveryMode() : DeliveryMode.OFFLINE;
        
        if (mode == DeliveryMode.OFFLINE) {
            if (session.getLocation() != null && user.getLocation() != null) {
                return session.getLocation().getId().equals(user.getLocation().getId());
            }
            return true;
        } else {
            LocationScope scope = session.getLocationScope() != null ? session.getLocationScope() : LocationScope.ALL_LOCATIONS;
            
            if (scope == LocationScope.ALL_LOCATIONS) {
                return true;
            } else {
                if (session.getTargetLocations() != null && !session.getTargetLocations().isEmpty()) {
                    if (user.getLocation() != null) {
                        return session.getTargetLocations().stream()
                            .anyMatch(location -> location.getId().equals(user.getLocation().getId()));
                    }
                    return false;
                } else {
                    if (session.getLocation() != null && user.getLocation() != null) {
                        return session.getLocation().getId().equals(user.getLocation().getId());
                    }
                    return true;
                }
            }
        }
    }
    
    @Override
    public List<LearningSessionDTO> getAllSessionsByRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            // If no roles provided, return all active scheduled future sessions
            return learningSessionRepository.findAll().stream()
                    .filter(session -> Boolean.TRUE.equals(session.getIsActive()))
                    .filter(session -> session.getStatus() == LearningSession.SessionStatus.SCHEDULED)
                    .filter(session -> session.getStartDateTime().isAfter(LocalDateTime.now()))
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        
        return learningSessionRepository.findActiveSessionsByRoles(roleIds, LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<LearningSessionDTO> getAllSessions(Pageable pageable) {
        return learningSessionRepository.findByIsActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    public List<LearningSessionDTO> getActiveSessions() {
        return learningSessionRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LearningSessionDTO getSessionById(Long id) {
        // Clear the entity manager cache to ensure we fetch fresh data
        entityManager.clear();
        
        LearningSession session = learningSessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", id));
        
        return toDTO(session);
    }

    @Override
    public LearningSessionDTO getSessionByCode(String sessionCode) {
        LearningSession session = learningSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "sessionCode", sessionCode));
        return toDTO(session);
    }

    @Override
    public LearningSessionDTO createSession(LearningSessionDTO dto) {
        if (dto.getStartDateTime().isAfter(dto.getEndDateTime())) {
            throw new BadRequestException("Start date time must be before end date time");
        }

        LearningProgram program = learningProgramRepository.findById(dto.getLearningProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("LearningProgram", "id", dto.getLearningProgramId()));

        LearningSession session = new LearningSession();
        session.setSessionCode(generateSessionCode());
        session.setLearningProgram(program);
        session.setStartDateTime(dto.getStartDateTime());
        session.setEndDateTime(dto.getEndDateTime());
        session.setTotalSeats(dto.getTotalSeats());
        session.setAvailableSeats(dto.getTotalSeats());
        session.setWaitlistCapacity(dto.getWaitlistCapacity() != null ? dto.getWaitlistCapacity() : 0);
        session.setStatus(LearningSession.SessionStatus.SCHEDULED);
        session.setInstructorName(dto.getInstructorName());
        session.setInstructorEmail(dto.getInstructorEmail());
        session.setVirtualMeetingLink(dto.getVirtualMeetingLink());
        session.setRoomNumber(dto.getRoomNumber());
        session.setNotes(dto.getNotes());
        session.setIsActive(true);
        
        // Set delivery mode (default to OFFLINE if not specified for backward compatibility)
        session.setDeliveryMode(dto.getSessionDeliveryMode() != null ? dto.getSessionDeliveryMode() : DeliveryMode.OFFLINE);
        
        // Set location scope (default to ALL_LOCATIONS if not specified)
        session.setLocationScope(dto.getSessionLocationScope() != null ? dto.getSessionLocationScope() : LocationScope.ALL_LOCATIONS);
        
        // Set target roles (default to all roles if not specified for backward compatibility)
        if (dto.getTargetRoleIds() != null && !dto.getTargetRoleIds().isEmpty()) {
            Set<Role> targetRoles = new HashSet<>();
            for (Long roleId : dto.getTargetRoleIds()) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
                targetRoles.add(role);
            }
            session.setTargetRoles(targetRoles);
        } else {
            // Default: all roles can see the session
            session.setTargetRoles(new HashSet<>(roleRepository.findAll()));
        }
        
        // Set target locations (for SPECIFIC_LOCATION scope)
        if (dto.getTargetLocationIds() != null && !dto.getTargetLocationIds().isEmpty()) {
            Set<Location> targetLocations = new HashSet<>();
            for (Long locationId : dto.getTargetLocationIds()) {
                Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
                targetLocations.add(location);
            }
            session.setTargetLocations(targetLocations);
        }

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", dto.getLocationId()));
            session.setLocation(location);
        }

        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));
            session.setCreatedBy(createdBy);
        }

        LearningSession saved = learningSessionRepository.save(session);
        return toDTO(saved);
    }

    @Override
    public LearningSessionDTO updateSession(Long id, LearningSessionDTO dto) {
        LearningSession session = learningSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", id));

        if (dto.getStartDateTime().isAfter(dto.getEndDateTime())) {
            throw new BadRequestException("Start date time must be before end date time");
        }

        session.setStartDateTime(dto.getStartDateTime());
        session.setEndDateTime(dto.getEndDateTime());
        session.setTotalSeats(dto.getTotalSeats());
        session.setWaitlistCapacity(dto.getWaitlistCapacity());
        session.setInstructorName(dto.getInstructorName());
        session.setInstructorEmail(dto.getInstructorEmail());
        session.setVirtualMeetingLink(dto.getVirtualMeetingLink());
        session.setRoomNumber(dto.getRoomNumber());
        session.setNotes(dto.getNotes());

        if (dto.getStatus() != null) {
            session.setStatus(dto.getStatus());
        }
        
        // Update delivery mode if provided
        if (dto.getSessionDeliveryMode() != null) {
            session.setDeliveryMode(dto.getSessionDeliveryMode());
        }
        
        // Update location scope if provided
        if (dto.getSessionLocationScope() != null) {
            session.setLocationScope(dto.getSessionLocationScope());
        }
        
        // Update target roles if provided
        if (dto.getTargetRoleIds() != null) {
            Set<Role> targetRoles = new HashSet<>();
            for (Long roleId : dto.getTargetRoleIds()) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
                targetRoles.add(role);
            }
            session.setTargetRoles(targetRoles);
        }
        
        // Update target locations if provided
        if (dto.getTargetLocationIds() != null) {
            Set<Location> targetLocations = new HashSet<>();
            for (Long locationId : dto.getTargetLocationIds()) {
                Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", locationId));
                targetLocations.add(location);
            }
            session.setTargetLocations(targetLocations);
        }

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", "id", dto.getLocationId()));
            session.setLocation(location);
        }

        LearningSession updated = learningSessionRepository.save(session);
        return toDTO(updated);
    }

    @Override
    public void deleteSession(Long id) {
        LearningSession session = learningSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", id));
        
        // Check if there are any active bookings for this session
        Long activeBookingCount = bookingRepository.countActiveBookingsBySession(id);
        if (activeBookingCount > 0) {
            throw new BadRequestException(
                "Cannot delete session with existing bookings. " +
                "This session has " + activeBookingCount + " active booking(s). " +
                "Please cancel the session instead, which will notify all affected users."
            );
        }
        
        log.info("Soft deleting session {} (no active bookings)", id);
        session.setIsActive(false);
        learningSessionRepository.save(session);
    }

    @Override
    public void cancelSession(Long id) {
        LearningSession session = learningSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", id));
        
        log.info("Cancelling session {} ({})", id, session.getSessionCode());
        
        // Update session status to CANCELLED
        session.setStatus(LearningSession.SessionStatus.CANCELLED);
        learningSessionRepository.save(session);
        
        // Cancel all active bookings for this session
        String cancellationReason = "Session " + session.getSessionCode() + " has been cancelled";
        bookingService.cancelBookingsBySession(id, cancellationReason);
        
        log.info("Session {} cancelled successfully with all related bookings", id);
    }

    @Override
    public List<LearningSessionDTO> getSessionsByProgram(Long programId) {
        return learningSessionRepository.findByLearningProgramId(programId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getSessionsByLocation(Long locationId) {
        return learningSessionRepository.findByLocationId(locationId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getSessionsByStatus(LearningSession.SessionStatus status) {
        return learningSessionRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return learningSessionRepository.findByDateRange(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getUpcomingSessions() {
        return learningSessionRepository.findUpcomingSessions(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getAvailableSessions() {
        return learningSessionRepository.findAvailableSessions(LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getAvailableSessionsByProgram(Long programId) {
        return learningSessionRepository.findAvailableSessionsByProgram(programId, LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getSessionsByLocationAndDateRange(Long locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return learningSessionRepository.findByLocationAndDateRange(locationId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a session is visible to a specific user based on:
     * - Delivery mode (ONLINE/OFFLINE)
     * - Location scope (for online sessions)
     * - User's location
     * - Target roles
     */
    private boolean isSessionVisibleToUser(LearningSession session, User user) {
        // Check role eligibility - compare role types, not role IDs (handles multiple roles with same type)
        if (session.getTargetRoles() != null && !session.getTargetRoles().isEmpty()) {
            boolean hasRole = session.getTargetRoles().stream()
                .anyMatch(role -> role.getRoleType().equals(user.getRole().getRoleType()));
            if (!hasRole) {
                return false;
            }
        }
        
        // Check location eligibility based on delivery mode
        DeliveryMode mode = session.getDeliveryMode() != null ? session.getDeliveryMode() : DeliveryMode.OFFLINE;
        
        if (mode == DeliveryMode.OFFLINE) {
            // Offline sessions: only visible to users in the same location
            if (session.getLocation() != null && user.getLocation() != null) {
                return session.getLocation().getId().equals(user.getLocation().getId());
            }
            // If session or user has no location, visible by default (backward compatibility)
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
                        return session.getTargetLocations().stream()
                            .anyMatch(location -> location.getId().equals(user.getLocation().getId()));
                    }
                    // User has no location, cannot access specific location sessions
                    return false;
                } else {
                    // Fallback: if no target locations specified, check session's primary location
                    if (session.getLocation() != null && user.getLocation() != null) {
                        return session.getLocation().getId().equals(user.getLocation().getId());
                    }
                    return true;
                }
            }
        }
    }
    
    /**
     * Check if a user can book a specific session
     */
    public boolean canUserBookSession(Long sessionId, Long userId) {
        LearningSession session = learningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningSession", "id", sessionId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                
        return isSessionVisibleToUser(session, user);
    }

    @Override
    public List<LearningSessionDTO> getSessionsByInstructor(String instructorEmail) {
        return learningSessionRepository.findByInstructorEmail(instructorEmail).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningSessionDTO> getUpcomingSessionsBySkill(Long skillId) {
        return learningSessionRepository.findUpcomingSessionsBySkill(skillId, LocalDateTime.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countConfirmedBookings(Long sessionId) {
        return learningSessionRepository.countConfirmedBookings(sessionId);
    }

    @Override
    public Long countWaitlistedBookings(Long sessionId) {
        return learningSessionRepository.countWaitlistedBookings(sessionId);
    }

    private String generateSessionCode() {
        return "SES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LearningSessionDTO toDTO(LearningSession session) {
        LearningSessionDTO dto = LearningSessionDTO.builder()
                .id(session.getId())
                .sessionCode(session.getSessionCode())
                .startDateTime(session.getStartDateTime())
                .endDateTime(session.getEndDateTime())
                .totalSeats(session.getTotalSeats())
                .availableSeats(session.getAvailableSeats())
                .waitlistCapacity(session.getWaitlistCapacity())
                .status(session.getStatus())
                .sessionDeliveryMode(session.getDeliveryMode())
                .sessionLocationScope(session.getLocationScope())
                .instructorName(session.getInstructorName())
                .instructorEmail(session.getInstructorEmail())
                .virtualMeetingLink(session.getVirtualMeetingLink())
                .roomNumber(session.getRoomNumber())
                .notes(session.getNotes())
                .isActive(session.getIsActive())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();

        // Add target role IDs
        if (session.getTargetRoles() != null && !session.getTargetRoles().isEmpty()) {
            dto.setTargetRoleIds(session.getTargetRoles().stream()
                .map(role -> role.getId())
                .collect(java.util.stream.Collectors.toSet()));
        }
        
        // Add target location IDs
        if (session.getTargetLocations() != null && !session.getTargetLocations().isEmpty()) {
            dto.setTargetLocationIds(session.getTargetLocations().stream()
                .map(location -> location.getId())
                .collect(java.util.stream.Collectors.toList()));
        }

        if (session.getLearningProgram() != null) {
            dto.setLearningProgramId(session.getLearningProgram().getId());
            dto.setLearningProgramName(session.getLearningProgram().getName());
            dto.setLearningProgramCode(session.getLearningProgram().getCode());
            
            // Add skill IDs from the learning program
            if (session.getLearningProgram().getSkills() != null) {
                dto.setSkillIds(session.getLearningProgram().getSkills().stream()
                    .map(skill -> skill.getId())
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            // Add program type
            if (session.getLearningProgram().getProgramType() != null) {
                dto.setProgramType(session.getLearningProgram().getProgramType().name());
            }
            // Note: Program delivery mode is legacy - we now use session-level deliveryMode
            
            // Add target business unit IDs
            if (session.getLearningProgram().getTargetBusinessUnits() != null) {
                dto.setTargetBusinessUnitIds(session.getLearningProgram().getTargetBusinessUnits().stream()
                    .map(bu -> bu.getId())
                    .collect(java.util.stream.Collectors.toList()));
            }
        }

        if (session.getLocation() != null) {
            dto.setLocationId(session.getLocation().getId());
            dto.setLocationName(session.getLocation().getName());
            dto.setLocationCity(session.getLocation().getCity());
            dto.setLocationCountry(session.getLocation().getCountry());
        }

        if (session.getCreatedBy() != null) {
            dto.setCreatedById(session.getCreatedBy().getId());
            dto.setCreatedByName(session.getCreatedBy().getFullName());
        }

        return dto;
    }
}
