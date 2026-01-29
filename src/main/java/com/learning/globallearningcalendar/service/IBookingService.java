package com.learning.globallearningcalendar.service;

import com.learning.globallearningcalendar.dto.BookingDTO;
import com.learning.globallearningcalendar.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IBookingService {

    List<BookingDTO> getAllBookings();

    Page<BookingDTO> getAllBookings(Pageable pageable);

    BookingDTO getBookingById(Long id);

    BookingDTO getBookingByReference(String bookingReference);

    BookingDTO createBooking(BookingDTO dto);

    BookingDTO updateBooking(Long id, BookingDTO dto);

    BookingDTO cancelBooking(Long id, String cancellationReason);

    List<BookingDTO> getBookingsByUser(Long userId);

    Page<BookingDTO> getBookingsByUser(Long userId, Pageable pageable);

    List<BookingDTO> getBookingsBySession(Long sessionId);

    BookingDTO getBookingByUserAndSession(Long userId, Long sessionId);

    BookingDTO getAnyBookingByUserAndSession(Long userId, Long sessionId);

    List<BookingDTO> getBookingsByStatus(Booking.BookingStatus status);

    List<BookingDTO> getBookingsByUserAndStatus(Long userId, Booking.BookingStatus status);

    List<BookingDTO> getUpcomingBookingsByUser(Long userId);

    List<BookingDTO> getCompletedBookingsByUser(Long userId);

    List<BookingDTO> getWaitlistedBookingsBySession(Long sessionId);

    BookingDTO confirmBooking(Long id, Long approvedById);

    BookingDTO markAttendance(Long id, Booking.AttendanceStatus attendanceStatus);

    BookingDTO markCompletion(Long id, Booking.CompletionStatus completionStatus);

    BookingDTO submitFeedback(Long id, Integer rating, String comments);

    Long countConfirmedBookingsByUser(Long userId);

    Long countCompletedBookingsByUser(Long userId);

    List<BookingDTO> getBookingsByBusinessUnitAndDateRange(Long buId, LocalDateTime startDate, LocalDateTime endDate);

    List<BookingDTO> getBookingsByLocationAndDateRange(Long locationId, LocalDateTime startDate, LocalDateTime endDate);

    List<BookingDTO> getTeamBookings(Long managerId);

    Map<Booking.AttendanceStatus, Long> getAttendanceStatsBySession(Long sessionId);

    Double getAverageFeedbackRatingByProgram(Long programId);

    BookingDTO rejectBooking(Long id, Long rejectedById, String rejectionReason);

    List<BookingDTO> getPendingApprovalsByManager(Long managerId);

    List<BookingDTO> getAllPendingApprovals();

    BookingDTO markManagerNotified(Long id);

    BookingDTO getRejectedBookingByUserAndSession(Long userId, Long sessionId);

    boolean canUserRebookSession(Long userId, Long sessionId);

    void cancelBookingsBySession(Long sessionId, String cancellationReason);

    List<Integer> getBookedSeatsForSession(Long sessionId);

    Map<Integer, String> getSeatsStatusForSession(Long sessionId);

    BookingDTO selectSeat(Long bookingId, Integer seatNumber);

    BookingDTO changeSeat(Long bookingId, Integer newSeatNumber);

    BookingDTO requestCancellation(Long bookingId, Long userId, String cancellationReason);

    BookingDTO approveCancellation(Long bookingId, Long managerId);

    BookingDTO rejectCancellationRequest(Long bookingId, Long managerId, String rejectionReason);

    List<BookingDTO> getPendingCancellationsByManager(Long managerId);
}
