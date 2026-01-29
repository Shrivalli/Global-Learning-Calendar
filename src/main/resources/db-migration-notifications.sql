-- Database migration for Notifications System
-- This script creates the notifications table for storing user notifications related to booking lifecycle events

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    booking_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_notification_user_id (user_id),
    INDEX idx_notification_is_read (is_read),
    INDEX idx_notification_created_at (created_at),
    INDEX idx_notification_type (type),
    INDEX idx_notification_user_unread (user_id, is_read, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment to table
ALTER TABLE notifications COMMENT = 'Stores user notifications for booking lifecycle events including confirmations, cancellations, waitlist updates, and manager approvals';

-- Valid notification types (for reference):
-- BOOKING_CONFIRMED - Booking has been confirmed
-- BOOKING_CANCELLED - Booking has been cancelled
-- BOOKING_WAITLISTED - User added to waitlist
-- WAITLIST_PROMOTED - Waitlist booking promoted to confirmed
-- BOOKING_APPROVED - Manager approved the booking
-- BOOKING_REJECTED - Manager rejected the booking
-- CANCELLATION_APPROVED - Manager approved cancellation request
-- CANCELLATION_REJECTED - Manager rejected cancellation request
-- NOMINATION_RECEIVED - User received a session nomination
-- SESSION_REMINDER - Upcoming session reminder
