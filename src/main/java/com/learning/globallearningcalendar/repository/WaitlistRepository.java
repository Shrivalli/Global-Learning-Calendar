package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Waitlist;
import com.learning.globallearningcalendar.entity.Waitlist.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Waitlist operations
 */
@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    /**
     * Find all waitlist entries for a session with a specific status, ordered by position
     */
    @Query("SELECT w FROM Waitlist w WHERE w.learningSession.id = :sessionId AND w.status = :status ORDER BY w.position ASC")
    List<Waitlist> findBySessionIdAndStatusOrderByPosition(@Param("sessionId") Long sessionId, @Param("status") WaitlistStatus status);

    /**
     * Find all waitlist entries for a session with a specific status
     */
    List<Waitlist> findByLearningSessionIdAndStatus(Long learningSessionId, WaitlistStatus status);

    /**
     * Find a specific user's waitlist entry for a session with a specific status
     */
    @Query("SELECT w FROM Waitlist w WHERE w.learningSession.id = :sessionId AND w.user.id = :userId AND w.status = :status")
    Optional<Waitlist> findBySessionIdAndUserIdAndStatus(@Param("sessionId") Long sessionId, @Param("userId") Long userId, @Param("status") WaitlistStatus status);

    /**
     * Check if a user is already in the waitlist for a session with a specific status
     */
    boolean existsByLearningSessionIdAndUserIdAndStatus(Long sessionId, Long userId, WaitlistStatus status);

    /**
     * Check if a user is already in the waitlist for a session (any status)
     */
    boolean existsByLearningSessionIdAndUserId(Long sessionId, Long userId);

    /**
     * Count waiting entries for a session
     */
    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.learningSession.id = :sessionId AND w.status = 'WAITING'")
    Integer countWaitingBySession(@Param("sessionId") Long sessionId);

    /**
     * Find all active (WAITING) waitlist entries for a user
     */
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId AND w.status = 'WAITING' ORDER BY w.joinedAt DESC")
    List<Waitlist> findActiveWaitlistsByUser(@Param("userId") Long userId);

    /**
     * Find all waitlist entries for a user (any status)
     */
    @Query("SELECT w FROM Waitlist w WHERE w.user.id = :userId ORDER BY w.joinedAt DESC")
    List<Waitlist> findAllByUserId(@Param("userId") Long userId);

    /**
     * Find waitlist entry with details (with session and user eagerly loaded)
     */
    @Query("SELECT w FROM Waitlist w LEFT JOIN FETCH w.learningSession LEFT JOIN FETCH w.user WHERE w.id = :id")
    Optional<Waitlist> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all waiting entries for a session with details
     */
    @Query("SELECT w FROM Waitlist w LEFT JOIN FETCH w.user u WHERE w.learningSession.id = :sessionId AND w.status = 'WAITING' ORDER BY w.position ASC")
    List<Waitlist> findWaitingBySessionWithDetails(@Param("sessionId") Long sessionId);

    /**
     * Get the maximum position for a session (used to add new entries to the end)
     */
    @Query("SELECT MAX(w.position) FROM Waitlist w WHERE w.learningSession.id = :sessionId AND w.status = 'WAITING'")
    Integer findMaxPositionBySession(@Param("sessionId") Long sessionId);
}
