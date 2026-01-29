-- Migration Script: Add PENDING_CANCELLATION status to bookings
-- Date: 2026-01-02
-- Description: Adds PENDING_CANCELLATION to the status ENUM to support manager approval for mandatory booking cancellations

-- Add PENDING_CANCELLATION to the status enum
ALTER TABLE bookings
MODIFY COLUMN status ENUM(
    'PENDING_APPROVAL',
    'PENDING',
    'CONFIRMED',
    'PENDING_CANCELLATION',
    'WAITLISTED',
    'CANCELLED',
    'REJECTED',
    'NO_SHOW',
    'COMPLETED'
) NOT NULL;

COMMIT;
