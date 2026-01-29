# Database Fix Instructions

## Issue
Users cannot rebook sessions after cancellation or rejection due to a unique constraint on `(user_id, session_id)` in the `bookings` table.

## Quick Fix (Recommended)

### Step 1: Backup Your Data
```sql
USE learning_calendar_db;
CREATE TABLE bookings_backup_20251202 AS SELECT * FROM bookings;
```

### Step 2: Stop Your Spring Boot Application
Close or terminate the running Spring Boot application.

### Step 3: Drop the Bookings Table
```sql
USE learning_calendar_db;
DROP TABLE bookings;
```

### Step 4: Start Your Spring Boot Application
Run your Spring Boot application:
```bash
mvn spring-boot:run
```

Hibernate will automatically recreate the `bookings` table WITHOUT the unique constraint because it was removed from the `Booking.java` entity.

### Step 5: Restore Your Data
After the application starts and creates the new table structure:

```sql
USE learning_calendar_db;

INSERT INTO bookings (
    id, booking_reference, user_id, session_id, status, seat_number, 
    waitlist_position, booking_date, confirmation_date, cancellation_date,
    cancellation_reason, attendance_status, attendance_marked_at,
    completion_status, completion_date, feedback_rating, feedback_comments,
    approved_by, approval_date, rejected_by, rejection_date, rejection_reason,
    manager_notified, manager_notified_date, notes, created_at, updated_at
)
SELECT 
    id, booking_reference, user_id, session_id, status, seat_number,
    waitlist_position, booking_date, confirmation_date, cancellation_date,
    cancellation_reason, attendance_status, attendance_marked_at,
    completion_status, completion_date, feedback_rating, feedback_comments,
    approved_by, approval_date, rejected_by, rejection_date, rejection_reason,
    manager_notified, manager_notified_date, notes, created_at, updated_at
FROM bookings_backup_20251202;
```

### Step 6: Verify the Fix
```sql
-- Check that the unique constraint is gone
SELECT INDEX_NAME, NON_UNIQUE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'learning_calendar_db' AND TABLE_NAME = 'bookings'
GROUP BY INDEX_NAME, NON_UNIQUE;
```

Expected result: `UKmf500easgpetwi4ncvrripowj` should NOT be in the list.

### Step 7: Restart Application and Test
1. Restart your Spring Boot application
2. Test the booking flow:
   - Book a session
   - Cancel it
   - Rebook the same session ✓ Should work now!

### Step 8: Clean Up (After Verification)
```sql
USE learning_calendar_db;
DROP TABLE bookings_backup_20251202;
```

## Troubleshooting

### If Step 3 Fails with FK Error
If you get a foreign key constraint error when dropping the table:

```sql
-- Find all foreign keys referencing bookings
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'bookings'
AND TABLE_SCHEMA = 'learning_calendar_db';

-- Drop each foreign key constraint (replace FK_NAME and TABLE_NAME)
ALTER TABLE [TABLE_NAME] DROP FOREIGN KEY [FK_NAME];

-- Then drop the bookings table
DROP TABLE bookings;
```

### Alternative: Manual ALTER TABLE Approach
If you want to avoid recreating the table:

```sql
USE learning_calendar_db;

-- Try to drop the index directly
ALTER TABLE bookings DROP INDEX UKmf500easgpetwi4ncvrripowj;
```

If this gives error 1553 (Cannot drop index needed in FK), you need to identify which FK depends on it and recreate it without the unique constraint dependency.

## Verification Queries

### Check Current Bookings
```sql
SELECT id, user_id, session_id, status, booking_reference 
FROM bookings 
ORDER BY id DESC 
LIMIT 10;
```

### Test Duplicate (User, Session) with Different Statuses
```sql
-- This should now be allowed after fix
SELECT user_id, session_id, status, COUNT(*) as count
FROM bookings
GROUP BY user_id, session_id
HAVING count > 1;
```

After the fix, you should see results where users have multiple bookings for the same session with different statuses (e.g., one CANCELLED and one CONFIRMED).

## Summary
- ✅ Unique constraint removed from entity code
- ⏳ Database needs to be updated (follow steps above)
- ✅ Application logic handles active bookings correctly
- ✅ Users can rebook after cancellation or rejection

