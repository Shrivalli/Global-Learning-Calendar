package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Nomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NominationRepository extends JpaRepository<Nomination, Long> {

    // Find all nominations for a specific employee
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.session s " +
           "LEFT JOIN FETCH n.nominator " +
           "LEFT JOIN FETCH s.location " +
           "WHERE n.nominee.id = :nomineeId " +
           "ORDER BY n.nominatedAt DESC")
    List<Nomination> findByNomineeId(@Param("nomineeId") Long nomineeId);

    // Find pending nominations for an employee
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.session s " +
           "LEFT JOIN FETCH n.nominator " +
           "LEFT JOIN FETCH s.location " +
           "WHERE n.nominee.id = :nomineeId " +
           "AND n.status = 'PENDING' " +
           "ORDER BY n.nominatedAt DESC")
    List<Nomination> findPendingByNomineeId(@Param("nomineeId") Long nomineeId);

    // Count pending nominations for an employee (for badge counter)
    @Query("SELECT COUNT(n) FROM Nomination n " +
           "WHERE n.nominee.id = :nomineeId " +
           "AND n.status = 'PENDING'")
    Long countPendingByNomineeId(@Param("nomineeId") Long nomineeId);

    // Find actionable nominations - PENDING (needs response) or recent MANDATORY (for notification)
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.session s " +
           "LEFT JOIN FETCH n.nominator " +
           "LEFT JOIN FETCH s.location " +
           "WHERE n.nominee.id = :nomineeId " +
           "AND (n.status = 'PENDING' OR " +
           "     (n.status = 'COMPLETED' AND n.nominationType = 'MANDATORY' AND n.nominatedAt >= :since AND n.acknowledgedAt IS NULL)) " +
           "ORDER BY n.nominatedAt DESC")
    List<Nomination> findActionableByNomineeId(@Param("nomineeId") Long nomineeId, @Param("since") java.time.LocalDateTime since);

    // Count actionable nominations - PENDING or recent unacknowledged MANDATORY
    @Query("SELECT COUNT(n) FROM Nomination n " +
           "WHERE n.nominee.id = :nomineeId " +
           "AND (n.status = 'PENDING' OR " +
           "     (n.status = 'COMPLETED' AND n.nominationType = 'MANDATORY' AND n.nominatedAt >= :since AND n.acknowledgedAt IS NULL))")
    Long countActionableByNomineeId(@Param("nomineeId") Long nomineeId, @Param("since") java.time.LocalDateTime since);

    // Find all nominations sent by a manager
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.session s " +
           "LEFT JOIN FETCH n.nominee " +
           "LEFT JOIN FETCH s.location " +
           "WHERE n.nominator.id = :nominatorId " +
           "ORDER BY n.nominatedAt DESC")
    List<Nomination> findByNominatorId(@Param("nominatorId") Long nominatorId);

    // Find nominations for a specific session
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.nominee " +
           "LEFT JOIN FETCH n.nominator " +
           "WHERE n.session.id = :sessionId " +
           "ORDER BY n.nominatedAt DESC")
    List<Nomination> findBySessionId(@Param("sessionId") Long sessionId);

    // Check if user already has active nomination for a session
    // Only PENDING nominations are considered "active" - they need user response
    // ACCEPTED/DECLINED/COMPLETED are historical records and don't block new nominations
    @Query("SELECT n FROM Nomination n " +
           "WHERE n.session.id = :sessionId " +
           "AND n.nominee.id = :nomineeId " +
           "AND n.status = 'PENDING'")
    Optional<Nomination> findActiveNominationBySessionAndNominee(
        @Param("sessionId") Long sessionId,
        @Param("nomineeId") Long nomineeId
    );

    // Find nomination by booking ID
    @Query("SELECT n FROM Nomination n WHERE n.booking.id = :bookingId")
    Optional<Nomination> findByBookingId(@Param("bookingId") Long bookingId);

    // Find nomination by ID with all relationships fetched
    @Query("SELECT n FROM Nomination n " +
           "LEFT JOIN FETCH n.session s " +
           "LEFT JOIN FETCH n.nominee " +
           "LEFT JOIN FETCH n.nominator " +
           "LEFT JOIN FETCH n.booking " +
           "LEFT JOIN FETCH s.location " +
           "WHERE n.id = :id")
    Optional<Nomination> findByIdWithDetails(@Param("id") Long id);
}
