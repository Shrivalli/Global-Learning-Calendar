-- Migration script for Bulk Nomination feature
-- Creates nominations table for manager-to-employee training nominations

-- Create nominations table
CREATE TABLE IF NOT EXISTS nominations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    nominee_user_id BIGINT NOT NULL COMMENT 'Team member being nominated',
    nominator_user_id BIGINT NOT NULL COMMENT 'Manager who nominated',
    nomination_type ENUM('RECOMMENDED', 'MANDATORY') NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    booking_id BIGINT NULL COMMENT 'Link to created booking if accepted or mandatory',
    notes TEXT COMMENT 'Manager notes or reason for nomination',
    nominated_at DATETIME NOT NULL,
    responded_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES learning_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (nominee_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (nominator_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_nominee_user_id (nominee_user_id),
    INDEX idx_nominator_user_id (nominator_user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_nomination_type (nomination_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Note: Duplicate prevention will be handled in application logic
-- MySQL doesn't support filtered unique indexes like PostgreSQL
