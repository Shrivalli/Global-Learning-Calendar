-- Migration to add delivery mode, location scope, and target roles to sessions
-- Date: 2025-12-18
-- Description: Add fields for online/offline mode, location scope, and role-based visibility

-- Add delivery_mode column (default OFFLINE for existing sessions)
ALTER TABLE learning_sessions
ADD COLUMN delivery_mode VARCHAR(20) DEFAULT 'OFFLINE';

-- Add location_scope column (default ALL_LOCATIONS for existing sessions)
ALTER TABLE learning_sessions
ADD COLUMN location_scope VARCHAR(50) DEFAULT 'ALL_LOCATIONS';

-- Create junction table for session target roles
CREATE TABLE IF NOT EXISTS session_target_roles (
    session_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (session_id, role_id),
    FOREIGN KEY (session_id) REFERENCES learning_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- For existing sessions, add all roles to make them visible to everyone (backward compatibility)
INSERT INTO session_target_roles (session_id, role_id)
SELECT ls.id, r.id
FROM learning_sessions ls
CROSS JOIN roles r
WHERE ls.id NOT IN (SELECT session_id FROM session_target_roles);

COMMIT;
