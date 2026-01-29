-- Add acknowledged_at column to nominations table
-- This allows employees to acknowledge MANDATORY nominations and remove them from their notification list

ALTER TABLE nominations 
ADD COLUMN acknowledged_at DATETIME DEFAULT NULL 
AFTER responded_at;

-- Add index for better query performance when filtering by acknowledged_at
CREATE INDEX idx_nominations_acknowledged_at ON nominations(acknowledged_at);

-- Update comment
ALTER TABLE nominations 
MODIFY COLUMN acknowledged_at DATETIME DEFAULT NULL 
COMMENT 'Timestamp when employee acknowledged a MANDATORY nomination';
