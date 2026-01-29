package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.BookingDTO;
import com.learning.globallearningcalendar.entity.Booking;
import com.learning.globallearningcalendar.service.IBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Booking", description = "Booking management APIs")
public class BookingController {

    private final IBookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @GetMapping
    @Operation(summary = "Get all bookings")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get all bookings with pagination")
    public ResponseEntity<Page<BookingDTO>> getAllBookingsPaged(Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/reference/{bookingReference}")
    @Operation(summary = "Get booking by reference")
    public ResponseEntity<BookingDTO> getBookingByReference(@PathVariable String bookingReference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(bookingReference));
    }

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingDTO dto) {
        try {
            log.debug("Create booking request received: {}", dto);
            BookingDTO created = bookingService.createBooking(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("Error creating booking: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a booking")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.updateBooking(id, dto));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingDTO> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String cancellationReason) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, cancellationReason));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @GetMapping("/user/{userId}/paged")
    @Operation(summary = "Get bookings by user with pagination")
    public ResponseEntity<Page<BookingDTO>> getBookingsByUserPaged(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId, pageable));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get bookings by session")
    public ResponseEntity<List<BookingDTO>> getBookingsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getBookingsBySession(sessionId));
    }

    @GetMapping("/session/{sessionId}/booked-seats")
    @Operation(summary = "Get booked seat numbers for a session (includes pending approvals)")
    public ResponseEntity<List<Integer>> getBookedSeatsForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getBookedSeatsForSession(sessionId));
    }

    @GetMapping("/session/{sessionId}/seats-status")
    @Operation(summary = "Get seat status map for a session (seat number to booking status)")
    public ResponseEntity<Map<Integer, String>> getSeatsStatusForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getSeatsStatusForSession(sessionId));
    }

    @GetMapping("/user/{userId}/session/{sessionId}")
    @Operation(summary = "Get booking by user and session")
    public ResponseEntity<BookingDTO> getBookingByUserAndSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getBookingByUserAndSession(userId, sessionId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status")
    public ResponseEntity<List<BookingDTO>> getBookingsByStatus(@PathVariable Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get bookings by user and status")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByUserAndStatus(userId, status));
    }

    @GetMapping("/user/{userId}/upcoming")
    @Operation(summary = "Get upcoming bookings by user")
    public ResponseEntity<List<BookingDTO>> getUpcomingBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUpcomingBookingsByUser(userId));
    }

    @GetMapping("/user/{userId}/completed")
    @Operation(summary = "Get completed bookings by user")
    public ResponseEntity<List<BookingDTO>> getCompletedBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getCompletedBookingsByUser(userId));
    }

    @GetMapping("/session/{sessionId}/waitlisted")
    @Operation(summary = "Get waitlisted bookings by session")
    public ResponseEntity<List<BookingDTO>> getWaitlistedBookingsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getWaitlistedBookingsBySession(sessionId));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking (Manager approval)")
    public ResponseEntity<BookingDTO> confirmBooking(
            @PathVariable Long id,
            @RequestParam Long approvedById) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, approvedById));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a booking (Manager rejection)")
    public ResponseEntity<BookingDTO> rejectBooking(
            @PathVariable Long id,
            @RequestParam Long rejectedById,
            @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, rejectedById, rejectionReason));
    }

    @GetMapping("/manager/{managerId}/pending-approvals")
    @Operation(summary = "Get pending approval requests for a manager")
    public ResponseEntity<List<BookingDTO>> getPendingApprovalsByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(bookingService.getPendingApprovalsByManager(managerId));
    }

    @GetMapping("/pending-approvals")
    @Operation(summary = "Get all pending approval requests")
    public ResponseEntity<List<BookingDTO>> getAllPendingApprovals() {
        return ResponseEntity.ok(bookingService.getAllPendingApprovals());
    }

    @PostMapping("/{id}/notify-manager")
    @Operation(summary = "Mark manager as notified for a booking")
    public ResponseEntity<BookingDTO> markManagerNotified(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.markManagerNotified(id));
    }

    @GetMapping("/user/{userId}/session/{sessionId}/can-rebook")
    @Operation(summary = "Check if user can rebook a session (after rejection)")
    public ResponseEntity<Map<String, Boolean>> canUserRebookSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        boolean canRebook = bookingService.canUserRebookSession(userId, sessionId);
        return ResponseEntity.ok(Map.of("canRebook", canRebook));
    }

    @GetMapping("/user/{userId}/session/{sessionId}/rejected")
    @Operation(summary = "Get rejected booking details for rebooking")
    public ResponseEntity<BookingDTO> getRejectedBooking(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        BookingDTO rejected = bookingService.getRejectedBookingByUserAndSession(userId, sessionId);
        if (rejected == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rejected);
    }

    @PostMapping("/{id}/attendance")
    @Operation(summary = "Mark attendance for a booking")
    public ResponseEntity<BookingDTO> markAttendance(
            @PathVariable Long id,
            @RequestParam Booking.AttendanceStatus attendanceStatus) {
        return ResponseEntity.ok(bookingService.markAttendance(id, attendanceStatus));
    }

    @PostMapping("/{id}/completion")
    @Operation(summary = "Mark completion for a booking")
    public ResponseEntity<BookingDTO> markCompletion(
            @PathVariable Long id,
            @RequestParam Booking.CompletionStatus completionStatus) {
        return ResponseEntity.ok(bookingService.markCompletion(id, completionStatus));
    }

    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit feedback for a booking")
    public ResponseEntity<BookingDTO> submitFeedback(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(bookingService.submitFeedback(id, rating, comments));
    }

    @GetMapping("/user/{userId}/confirmed-count")
    @Operation(summary = "Get confirmed bookings count by user")
    public ResponseEntity<Long> countConfirmedBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.countConfirmedBookingsByUser(userId));
    }

    @GetMapping("/user/{userId}/completed-count")
    @Operation(summary = "Get completed bookings count by user")
    public ResponseEntity<Long> countCompletedBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.countCompletedBookingsByUser(userId));
    }

    @GetMapping("/business-unit/{buId}/date-range")
    @Operation(summary = "Get bookings by business unit and date range")
    public ResponseEntity<List<BookingDTO>> getBookingsByBusinessUnitAndDateRange(
            @PathVariable Long buId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(bookingService.getBookingsByBusinessUnitAndDateRange(buId, startDate, endDate));
    }

    @GetMapping("/location/{locationId}/date-range")
    @Operation(summary = "Get bookings by location and date range")
    public ResponseEntity<List<BookingDTO>> getBookingsByLocationAndDateRange(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(bookingService.getBookingsByLocationAndDateRange(locationId, startDate, endDate));
    }

    @GetMapping("/manager/{managerId}/team")
    @Operation(summary = "Get team bookings for a manager")
    public ResponseEntity<List<BookingDTO>> getTeamBookings(@PathVariable Long managerId) {
        return ResponseEntity.ok(bookingService.getTeamBookings(managerId));
    }

    @GetMapping("/session/{sessionId}/attendance-stats")
    @Operation(summary = "Get attendance statistics for a session")
    public ResponseEntity<Map<Booking.AttendanceStatus, Long>> getAttendanceStatsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getAttendanceStatsBySession(sessionId));
    }

    @GetMapping("/program/{programId}/average-rating")
    @Operation(summary = "Get average feedback rating for a program")
    public ResponseEntity<Double> getAverageFeedbackRatingByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(bookingService.getAverageFeedbackRatingByProgram(programId));
    }

    @PostMapping("/{id}/select-seat")
    @Operation(summary = "Select a seat for a PENDING booking (RECOMMENDED nominations)")
    public ResponseEntity<BookingDTO> selectSeat(
            @PathVariable Long id,
            @RequestParam Integer seatNumber) {
        return ResponseEntity.ok(bookingService.selectSeat(id, seatNumber));
    }

    @PostMapping("/{id}/change-seat")
    @Operation(summary = "Change seat for a CONFIRMED booking (MANDATORY nominations)")
    public ResponseEntity<BookingDTO> changeSeat(
            @PathVariable Long id,
            @RequestParam Integer newSeatNumber) {
        return ResponseEntity.ok(bookingService.changeSeat(id, newSeatNumber));
    }

    @PostMapping("/{id}/request-cancellation")
    @Operation(summary = "Request cancellation for a MANDATORY nomination booking (requires manager approval)")
    public ResponseEntity<BookingDTO> requestCancellation(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String cancellationReason) {
        return ResponseEntity.ok(bookingService.requestCancellation(id, userId, cancellationReason));
    }

    @PostMapping("/{id}/approve-cancellation")
    @Operation(summary = "Manager approves a cancellation request")
    public ResponseEntity<BookingDTO> approveCancellation(
            @PathVariable Long id,
            @RequestParam Long managerId) {
        return ResponseEntity.ok(bookingService.approveCancellation(id, managerId));
    }

    @PostMapping("/{id}/reject-cancellation")
    @Operation(summary = "Manager rejects a cancellation request")
    public ResponseEntity<BookingDTO> rejectCancellation(
            @PathVariable Long id,
            @RequestParam Long managerId,
            @RequestParam String rejectionReason) {
        return ResponseEntity.ok(bookingService.rejectCancellationRequest(id, managerId, rejectionReason));
    }

    @GetMapping("/manager/{managerId}/pending-cancellations")
    @Operation(summary = "Get pending cancellation requests for a manager")
    public ResponseEntity<List<BookingDTO>> getPendingCancellationsByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(bookingService.getPendingCancellationsByManager(managerId));
    }

    @GetMapping("/diagnostic/user/{userId}/session/{sessionId}")
    @Operation(summary = "Diagnostic: get any booking (including cancelled) for user+session")
    public ResponseEntity<?> getAnyBookingByUserAndSession(@PathVariable Long userId, @PathVariable Long sessionId) {
        try {
            // service doesn't have a dedicated method yet, call repository via existing getBookingByUserAndSession
            // fallback: try to get active booking first, then try to fetch any via a small service call
            BookingDTO active = null;
            try {
                active = bookingService.getBookingByUserAndSession(userId, sessionId);
            } catch (Exception ignore) {
                // ignore - may not exist or may be cancelled
            }

            // call repository through a small workaround: use getAllBookings and filter — not ideal but avoids exposing repository directly
            List<BookingDTO> all = bookingService.getBookingsByUser(userId);

            // find any that match sessionId
            for (BookingDTO b : all) {
                if (b.getSessionId() != null && b.getSessionId().equals(sessionId)) {
                    return ResponseEntity.ok(b);
                }
            }

            // If not found in active list, return a helpful message instructing to query DB directly
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "No active booking found for user+session. If you need to see cancelled bookings, run the SQL diagnostic described in the response."));
        } catch (Exception ex) {
            log.error("Error in diagnostic endpoint: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }
}
