-- =====================================================
-- Waitlist Functionality Database Migration
-- =====================================================
-- This migration adds waitlist functionality to the Global Learning Calendar
-- When sessions are full, employees can join a waitlist and will be automatically
-- confirmed if a seat becomes available due to cancellations.
-- =====================================================

-- Create waitlist table
CREATE TABLE IF NOT EXISTS waitlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    position INT NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_waitlist_session FOREIGN KEY (session_id) 
        REFERENCES learning_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint: A user can only be in waitlist once per session
    CONSTRAINT uk_waitlist_session_user UNIQUE (session_id, user_id),
    
    -- Indexes for better query performance
    INDEX idx_waitlist_session_status (session_id, status),
    INDEX idx_waitlist_position (session_id, position),
    INDEX idx_waitlist_user (user_id, status)
);

-- =====================================================
-- Sample Data for Waitlist
-- =====================================================

-- Insert sample waitlist entries
-- Note: These are example entries. Adjust session_id and user_id based on your existing data
-- User 1 (John Doe) joins waitlist for Session 3 (which should be full)
INSERT INTO waitlist (session_id, user_id, position, status, joined_at, notes)
VALUES (3, 1, 1, 'WAITING', DATE_SUB(NOW(), INTERVAL 2 DAY), 'First in line for Project Management session')
ON DUPLICATE KEY UPDATE position = VALUES(position);

-- User 4 (Emily Chen) joins waitlist for Session 3
INSERT INTO waitlist (session_id, user_id, position, status, joined_at, notes)
VALUES (3, 4, 2, 'WAITING', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Waiting for availability')
ON DUPLICATE KEY UPDATE position = VALUES(position);

-- User 7 (Michael Johnson) joins waitlist for Session 5 (Leadership Development)
INSERT INTO waitlist (session_id, user_id, position, status, joined_at, notes)
VALUES (5, 7, 1, 'WAITING', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'Interested in leadership program')
ON DUPLICATE KEY UPDATE position = VALUES(position);

-- User 10 (Daniel Martinez) joins waitlist for Session 3
INSERT INTO waitlist (session_id, user_id, position, status, joined_at, notes)
VALUES (3, 10, 3, 'WAITING', DATE_SUB(NOW(), INTERVAL 12 HOUR), 'Third in line')
ON DUPLICATE KEY UPDATE position = VALUES(position);

-- Example of a confirmed waitlist entry (user was moved from waitlist to confirmed booking)
INSERT INTO waitlist (session_id, user_id, position, status, joined_at, notified_at, notes)
VALUES (4, 6, 1, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 'Auto-confirmed when seat became available')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- =====================================================
-- Verification Queries
-- =====================================================

-- View all waitlist entries
-- SELECT w.*, 
--        ls.session_code, 
--        u.first_name, u.last_name, u.email
-- FROM waitlist w
-- JOIN learning_sessions ls ON w.session_id = ls.id
-- JOIN users u ON w.user_id = u.id
-- ORDER BY w.session_id, w.position;

-- Count waitlist entries by session
-- SELECT ls.session_code, ls.id as session_id,
--        COUNT(w.id) as waitlist_count,
--        ls.total_seats,
--        ls.available_seats
-- FROM learning_sessions ls
-- LEFT JOIN waitlist w ON ls.id = w.session_id AND w.status = 'WAITING'
-- GROUP BY ls.id
-- HAVING waitlist_count > 0
-- ORDER BY waitlist_count DESC;
