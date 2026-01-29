package com.learning.globallearningcalendar.repository;

import com.learning.globallearningcalendar.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.learningSession.id = :sessionId")
    List<Booking> findBySessionId(@Param("sessionId") Long sessionId);

    // Modified to ignore CANCELLED and REJECTED bookings when checking active existence
    // This allows employees to rebook after their booking was rejected by manager
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.id = :sessionId AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    Optional<Booking> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    // New: find any booking (including CANCELLED and REJECTED) for a user+session
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.id = :sessionId")
    Optional<Booking> findAnyByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status = :status ORDER BY b.waitlistPosition")
    List<Booking> findBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.startDateTime >= :startDate ORDER BY b.learningSession.startDateTime")
    List<Booking> findUpcomingBookingsByUser(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.startDateTime > :startDate AND b.status NOT IN ('CANCELLED', 'REJECTED') ORDER BY b.learningSession.startDateTime")
    List<Booking> findUpcomingBookingsByUserExcludingStatus(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("excludedStatus") Booking.BookingStatus excludedStatus);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.startDateTime < :endDate AND b.status = 'COMPLETED' ORDER BY b.learningSession.startDateTime DESC")
    List<Booking> findCompletedBookingsByUser(@Param("userId") Long userId, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.learningSession ls LEFT JOIN FETCH ls.learningProgram WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status = 'WAITLISTED' ORDER BY b.waitlistPosition ASC")
    List<Booking> findWaitlistedBookingsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = 'COMPLETED'")
    Long countCompletedBookingsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status IN ('CONFIRMED', 'COMPLETED')")
    Long countActiveBookingsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT b FROM Booking b WHERE b.user.businessUnit.id = :buId AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    List<Booking> findByBusinessUnitAndDateRange(@Param("buId") Long buId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.user.location.id = :locationId AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    List<Booking> findByLocationAndDateRange(@Param("locationId") Long locationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.user.manager.id = :managerId ORDER BY b.learningSession.startDateTime DESC")
    List<Booking> findTeamBookings(@Param("managerId") Long managerId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT b.attendanceStatus, COUNT(b) FROM Booking b WHERE b.learningSession.id = :sessionId GROUP BY b.attendanceStatus")
    List<Object[]> getAttendanceStatsBySession(@Param("sessionId") Long sessionId);

    @Query("SELECT AVG(b.feedbackRating) FROM Booking b WHERE b.learningSession.learningProgram.id = :programId AND b.feedbackRating IS NOT NULL")
    Double getAverageFeedbackRatingByProgram(@Param("programId") Long programId);

    // Use method name derivation for these 'not' queries to ensure Spring Data handles paging/count queries properly
    List<Booking> findByUserIdAndStatusNot(Long userId, Booking.BookingStatus excludedStatus);

    Page<Booking> findByUserIdAndStatusNot(Long userId, Booking.BookingStatus excludedStatus, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.learningSession.id = :sessionId AND b.status != :excludedStatus ORDER BY b.waitlistPosition")
    List<Booking> findBySessionIdExcludingStatus(@Param("sessionId") Long sessionId, @Param("excludedStatus") Booking.BookingStatus excludedStatus);

    @Query("SELECT b FROM Booking b WHERE b.user.manager.id = :managerId AND b.status = 'PENDING_APPROVAL' ORDER BY b.bookingDate ASC")
    List<Booking> findPendingApprovalsByManager(@Param("managerId") Long managerId);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.user u LEFT JOIN FETCH b.learningSession ls LEFT JOIN FETCH ls.learningProgram WHERE b.user.manager.id = :managerId AND b.status = 'PENDING_CANCELLATION' ORDER BY b.cancellationDate ASC")
    List<Booking> findPendingCancellationsByManager(@Param("managerId") Long managerId);

    // Check if user has a rejected booking for this session (useful for showing "Rebook" option)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.id = :sessionId AND b.status = 'REJECTED'")
    Optional<Booking> findRejectedBookingByUserAndSession(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    // Check if a specific seat is already booked (excluding CANCELLED and REJECTED)
    @Query("SELECT b FROM Booking b WHERE b.learningSession.id = :sessionId AND b.seatNumber = :seatNumber AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    Optional<Booking> findBySessionIdAndSeatNumber(@Param("sessionId") Long sessionId, @Param("seatNumber") Integer seatNumber);

    // Get all booked seat numbers for a session (excluding CANCELLED and REJECTED)
    @Query("SELECT b.seatNumber FROM Booking b WHERE b.learningSession.id = :sessionId AND b.seatNumber IS NOT NULL AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    List<Integer> findBookedSeatsBySessionId(@Param("sessionId") Long sessionId);
}
