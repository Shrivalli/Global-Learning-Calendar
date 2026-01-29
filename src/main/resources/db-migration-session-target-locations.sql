-- Migration script for session target locations feature
-- Creates junction table for many-to-many relationship between sessions and locations

-- Create session_target_locations junction table
CREATE TABLE IF NOT EXISTS session_target_locations (
    session_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    PRIMARY KEY (session_id, location_id),
    FOREIGN KEY (session_id) REFERENCES learning_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add index for better query performance
CREATE INDEX idx_session_target_locations_session ON session_target_locations(session_id);
CREATE INDEX idx_session_target_locations_location ON session_target_locations(location_id);

-- No data migration needed - this is a new optional feature
-- Sessions without target locations will fall back to checking the primary location field
