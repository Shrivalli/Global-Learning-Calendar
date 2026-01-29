package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.LearningSessionDTO;
import com.learning.globallearningcalendar.entity.LearningSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ILearningSessionService {

    List<LearningSessionDTO> getAllSessions();
    
    List<LearningSessionDTO> getAllSessionsForUser(Long userId);
    
    List<LearningSessionDTO> getAllSessionsForUserIncludingSubordinates(Long userId);
    
    List<LearningSessionDTO> getAllSessionsByRoles(List<Long> roleIds);

    Page<LearningSessionDTO> getAllSessions(Pageable pageable);

    List<LearningSessionDTO> getActiveSessions();

    LearningSessionDTO getSessionById(Long id);

    LearningSessionDTO getSessionByCode(String sessionCode);

    LearningSessionDTO createSession(LearningSessionDTO dto);

    LearningSessionDTO updateSession(Long id, LearningSessionDTO dto);

    void deleteSession(Long id);

    void cancelSession(Long id);

    List<LearningSessionDTO> getSessionsByProgram(Long programId);

    List<LearningSessionDTO> getSessionsByLocation(Long locationId);

    List<LearningSessionDTO> getSessionsByStatus(LearningSession.SessionStatus status);

    List<LearningSessionDTO> getSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<LearningSessionDTO> getUpcomingSessions();

    List<LearningSessionDTO> getAvailableSessions();

    List<LearningSessionDTO> getAvailableSessionsByProgram(Long programId);

    List<LearningSessionDTO> getSessionsByLocationAndDateRange(Long locationId, LocalDateTime startDate, LocalDateTime endDate);

    List<LearningSessionDTO> getSessionsByInstructor(String instructorEmail);

    List<LearningSessionDTO> getUpcomingSessionsBySkill(Long skillId);

    Long countConfirmedBookings(Long sessionId);

    Long countWaitlistedBookings(Long sessionId);
    
    boolean canUserBookSession(Long sessionId, Long userId);
}
