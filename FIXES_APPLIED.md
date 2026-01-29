# FIXES APPLIED - Global Learning Calendar

## Date: December 2, 2025

## Summary of Issues Fixed

### 1. BookingRepository.java - Incomplete Method Signature ✅
**Problem**: Line 32 had a `@Query` annotation without a method signature, causing compilation error: "Query is not a repeatable annotation interface"

**Fix Applied**: Added missing method signature for `findByUserIdAndSessionId()`

```java
@Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.id = :sessionId AND b.status NOT IN ('CANCELLED', 'REJECTED')")
Optional<Booking> findByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);
```

### 2. BookingServiceImpl.java - Missing UUID Import ✅
**Problem**: `UUID.randomUUID()` was used but java.util.UUID was not imported

**Fix Applied**: Added missing import:
```java
import java.util.UUID;
```

### 3. Query Logic for Upcoming Bookings ✅
**Problem**: Count queries were including CANCELLED and REJECTED bookings

**Fix Applied**: Updated `findUpcomingBookingsByUserExcludingStatus` to explicitly exclude CANCELLED and REJECTED:
```java
@Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.learningSession.startDateTime > :startDate AND b.status NOT IN ('CANCELLED', 'REJECTED') ORDER BY b.learningSession.startDateTime")
```

### 4. Maven Compilation ✅
**Status**: BUILD SUCCESS
- All Java files compiled successfully
- Lombok annotation processing working correctly
- Only warnings about @Builder default values (non-critical)

## Remaining Issue: Database Constraint

### Problem
MySQL unique index `UKmf500easgpetwi4ncvrripowj` on `(user_id, session_id)` prevents users from rebooking after cancellation.

Error when trying to drop:
```
ERROR 1553 (HY000): Cannot drop index 'UKmf500easgpetwi4ncvrripowj': needed in a foreign key constraint
```

### Solution Steps

#### Option 1: Drop Index (Recommended if no FK dependency)
```sql
USE learning_calendar_db;

-- Drop the unique constraint
ALTER TABLE bookings DROP INDEX UKmf500easgpetwi4ncvrripowj;
```

#### Option 2: If Foreign Key Exists (Handle FK first)
```sql
USE learning_calendar_db;

-- Step 1: Find the foreign key name
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'learning_calendar_db'
AND TABLE_NAME = 'bookings'
AND CONSTRAINT_NAME LIKE 'FK%';

-- Step 2: Drop the foreign key (replace FK_NAME with actual name)
ALTER TABLE bookings DROP FOREIGN KEY FK_NAME;

-- Step 3: Drop the unique index
ALTER TABLE bookings DROP INDEX UKmf500easgpetwi4ncvrripowj;

-- Step 4: Recreate the foreign key without unique constraint (if needed)
ALTER TABLE bookings 
ADD CONSTRAINT FK_bookings_user_session 
FOREIGN KEY (user_id, session_id) 
REFERENCES ... ;  -- Adjust based on your actual FK
```

#### Option 3: Recreate Table (Nuclear option)
```sql
USE learning_calendar_db;

-- Backup existing data
CREATE TABLE bookings_backup AS SELECT * FROM bookings;

-- Drop and recreate without the unique constraint
-- The application.properties has `spring.jpa.hibernate.ddl-auto=update`
-- So after removing the @Table unique constraint annotation from Booking.java,
-- restart the application and Hibernate should handle it.

-- OR manually recreate the table structure without the constraint
```

### Verification After Fix
```sql
-- Check remaining indexes
SHOW INDEX FROM bookings WHERE Key_name = 'UKmf500easgpetwi4ncvrripowj';
-- Should return empty result

-- Verify other indexes are intact
SHOW INDEX FROM bookings;
```

## Application Behavior After Fixes

### Booking Flow
1. User books a session → Status: PENDING_APPROVAL (if manager exists) or CONFIRMED (no manager)
2. Manager approves → Status: CONFIRMED, seat decremented
3. Manager rejects → Status: REJECTED
4. User cancels → Status: CANCELLED, seat incremented (if was CONFIRMED)

### Rebooking Logic
- **findByUserIdAndSessionId()**: Returns active bookings only (excludes CANCELLED and REJECTED)
- **findAnyByUserIdAndSessionId()**: Returns any booking including CANCELLED/REJECTED
- Users CAN rebook sessions after cancellation or rejection once DB constraint is removed

### Count Queries
- **Total Bookings**: Excludes CANCELLED bookings
- **Upcoming Bookings**: Excludes CANCELLED and REJECTED bookings
- **Confirmed Bookings**: Only counts status=CONFIRMED
- **Completed Bookings**: Only counts status=COMPLETED

## Files Modified
1. `src/main/java/com/learning/globallearningcalendar/repository/BookingRepository.java`
2. `src/main/java/com/learning/globallearningcalendar/service/impl/BookingServiceImpl.java`
3. Created: `fix-db-constraint.sql` (SQL script for database fix)
4. Created: `FIXES_APPLIED.md` (this file)

## Next Steps
1. ✅ Code compilation fixed - DONE
2. ⏳ Run the SQL fix for database constraint - USER ACTION REQUIRED
3. ⏳ Restart Spring Boot application
4. ⏳ Test booking cancellation and rebooking from frontend

## Testing Checklist
After applying database fix:
- [ ] Book a session
- [ ] Cancel the booking
- [ ] Verify frontend updates (total bookings, upcoming bookings)
- [ ] Rebook the same session
- [ ] Have manager reject a booking
- [ ] Try to rebook the rejected session
- [ ] Verify counts are accurate (excludes cancelled/rejected)

## Notes
- All compilation errors resolved
- Lombok is working correctly (generates getters/setters/builders)
- Spring Boot 4.0.0 with Java 17
- MySQL connector configured correctly
- OpenAPI/Swagger available at `/swagger-ui.html`

