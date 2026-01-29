package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.LearningSession;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {

    Optional<LearningSession> findBySessionCode(String sessionCode);

    List<LearningSession> findByIsActiveTrue();

    Page<LearningSession> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.learningProgram.id = :programId AND ls.isActive = true")
    List<LearningSession> findByLearningProgramId(@Param("programId") Long programId);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.location.id = :locationId AND ls.isActive = true")
    List<LearningSession> findByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.status = :status AND ls.isActive = true")
    List<LearningSession> findByStatus(@Param("status") LearningSession.SessionStatus status);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.startDateTime >= :startDate AND ls.startDateTime <= :endDate AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ls FROM LearningSession ls WHERE DATE(ls.startDateTime) >= CURRENT_DATE AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findUpcomingSessions(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.availableSeats > 0 AND ls.status = 'SCHEDULED' AND ls.startDateTime >= :now AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findAvailableSessions(@Param("now") LocalDateTime now);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.learningProgram.id = :programId AND ls.availableSeats > 0 AND ls.status = 'SCHEDULED' AND ls.startDateTime >= :now AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findAvailableSessionsByProgram(@Param("programId") Long programId, @Param("now") LocalDateTime now);

    @Query("SELECT ls FROM LearningSession ls WHERE ls.location.id = :locationId AND ls.startDateTime >= :startDate AND ls.startDateTime <= :endDate AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findByLocationAndDateRange(@Param("locationId") Long locationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT ls FROM LearningSession ls LEFT JOIN FETCH ls.learningProgram LEFT JOIN FETCH ls.location LEFT JOIN FETCH ls.targetRoles LEFT JOIN FETCH ls.targetLocations WHERE ls.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS"))
    Optional<LearningSession> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT ls FROM LearningSession ls LEFT JOIN FETCH ls.targetRoles LEFT JOIN FETCH ls.targetLocations WHERE ls.isActive = true")
    List<LearningSession> findAllActiveWithRoles();

    @Query("SELECT ls FROM LearningSession ls WHERE ls.instructorEmail = :email AND ls.isActive = true")
    List<LearningSession> findByInstructorEmail(@Param("email") String email);

    @Query("SELECT DISTINCT ls FROM LearningSession ls LEFT JOIN FETCH ls.targetRoles r LEFT JOIN FETCH ls.learningProgram WHERE ls.isActive = true AND ls.status = 'SCHEDULED' AND ls.startDateTime > :now AND (SIZE(ls.targetRoles) = 0 OR r.id IN :roleIds)")
    List<LearningSession> findActiveSessionsByRoles(@Param("roleIds") List<Long> roleIds, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookings(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status = 'WAITLISTED'")
    Long countWaitlistedBookings(@Param("sessionId") Long sessionId);

    @Query("SELECT ls FROM LearningSession ls JOIN ls.learningProgram lp JOIN lp.skills s WHERE s.id = :skillId AND ls.startDateTime >= :now AND ls.isActive = true ORDER BY ls.startDateTime")
    List<LearningSession> findUpcomingSessionsBySkill(@Param("skillId") Long skillId, @Param("now") LocalDateTime now);
}
