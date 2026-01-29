-- Migration Script: Add Manager Approval Workflow to Bookings
-- Date: 2025-12-02
-- Description: Adds columns to support manager approval workflow for training session bookings

-- Add new columns to bookings table
ALTER TABLE bookings
ADD COLUMN rejected_by BIGINT DEFAULT NULL,
ADD COLUMN rejection_date DATETIME(6) DEFAULT NULL,
ADD COLUMN rejection_reason TEXT DEFAULT NULL,
ADD COLUMN manager_notified BOOLEAN DEFAULT FALSE,
ADD COLUMN manager_notified_date DATETIME(6) DEFAULT NULL;

-- Add foreign key constraint for rejected_by column
ALTER TABLE bookings
ADD CONSTRAINT fk_bookings_rejected_by
FOREIGN KEY (rejected_by) REFERENCES users(id);

-- Update existing PENDING bookings to PENDING_APPROVAL if user has a manager
-- (This is optional - only run if you want to convert existing pending bookings)
-- UPDATE bookings b
-- JOIN users u ON b.user_id = u.id
-- SET b.status = 'PENDING_APPROVAL'
-- WHERE b.status = 'PENDING' AND u.manager_id IS NOT NULL;

-- Add index on manager_id and status for efficient queries
CREATE INDEX idx_bookings_user_manager_status
ON bookings(user_id, status);

-- Add index on manager_notified for efficient notification queries
CREATE INDEX idx_bookings_manager_notified
ON bookings(manager_notified, status);

COMMIT;

