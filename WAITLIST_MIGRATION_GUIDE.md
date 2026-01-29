# Waitlist Migration - Old System to New System

## Problem Identified
Your application was using the **OLD waitlist system** where waitlisted users had bookings with `status = 'WAITLISTED'`. The new waitlist auto-confirmation feature uses a **separate `waitlist` table**, so the two systems were incompatible.

**Result:** When someone cancelled a booking, the system looked for entries in the `waitlist` table but found none, because users were still being added as WAITLISTED bookings.

## Changes Made

### 1. Modified Booking Creation Logic
**File:** `BookingServiceImpl.java` - `createBooking()` method

**Before:**
```java
if (session is full) {
    Create booking with status = WAITLISTED
}
```

**After:**
```java
if (session is full) {
    Add user to waitlist table using waitlistService.joinWaitlist()
    Throw exception to inform user they were waitlisted
}
```

**Impact:** 
- New booking attempts on full sessions will automatically use the new waitlist table
- Users are properly added to the waitlist with positions tracked
- Auto-confirmation will work when seats become available

### 2. Added Migration Logic for Old WAITLISTED Bookings
**File:** `BookingServiceImpl.java` - New method `migrateOldWaitlistedBookings()`

When a booking is cancelled, the system now:
1. Processes new waitlist table (main logic)
2. **Automatically migrates** any old WAITLISTED bookings to the new waitlist table
3. Deletes the migrated WAITLISTED booking records

**Impact:**
- Existing WAITLISTED bookings will be automatically migrated when someone cancels
- No manual intervention needed for migration (happens automatically)
- Maintains FIFO order based on original booking date

### 3. Created Manual Migration Script
**File:** `migrate-waitlisted-bookings.sql`

For immediate migration of all existing WAITLISTED bookings without waiting for cancellations.

## Deployment Steps

### Step 1: Rebuild Application
```bash
cd c:\reni\Genc-App\global-learning-calendar
mvn clean package
```

### Step 2: Run Database Migration (if not done already)
```sql
-- Execute db-migration-waitlist.sql to create the waitlist table
source db-migration-waitlist.sql;
```

### Step 3: Restart Application
Restart your Spring Boot application to load the new code.

### Step 4: Migrate Existing WAITLISTED Bookings (Optional but Recommended)
```sql
-- Execute the migration script to move all existing WAITLISTED bookings
source migrate-waitlisted-bookings.sql;
```

**Note:** If you skip Step 4, the migration will happen automatically when someone cancels a booking for that session.

## Testing the Fix

### Test 1: Verify Existing WAITLISTED Booking Migration
```sql
-- Check if you have old WAITLISTED bookings
SELECT * FROM bookings WHERE status = 'WAITLISTED';

-- If yes, run the migration script then check again
SELECT * FROM bookings WHERE status = 'WAITLISTED';
-- Should be 0

-- Verify they're in the new waitlist table
SELECT * FROM waitlist WHERE status = 'WAITING';
```

### Test 2: Test New Waitlist Flow
1. Create a session with 1 seat
2. User A books the seat (session becomes full)
3. User B tries to book → Should get error "Session is full. You have been added to the waitlist..."
4. Check database:
   ```sql
   SELECT * FROM waitlist WHERE session_id = <SESSION_ID>;
   -- Should show User B with position = 1, status = 'WAITING'
   ```
5. User A cancels booking
6. Check database:
   ```sql
   -- User B should now have a booking
   SELECT * FROM bookings WHERE user_id = <USER_B_ID> AND learning_session_id = <SESSION_ID>;
   -- Status should be PENDING_APPROVAL (if has manager) or CONFIRMED (if no manager)
   
   -- User B's waitlist entry should be marked as CONFIRMED
   SELECT * FROM waitlist WHERE user_id = <USER_B_ID> AND session_id = <SESSION_ID>;
   -- Status should be 'CONFIRMED'
   ```

### Test 3: Test Auto-Migration on Cancellation
If you have old WAITLISTED bookings and didn't run the manual migration:
1. Find a session with WAITLISTED bookings
2. Cancel any CONFIRMED booking for that session
3. Check logs - you should see migration messages
4. Verify WAITLISTED bookings were moved to waitlist table and deleted from bookings

## Expected Behavior After Fix

### For New Booking Attempts on Full Sessions:
✅ User tries to book full session → Added to `waitlist` table → Receives error message → Frontend can display "You're on the waitlist"

### For Cancellations:
✅ User cancels booking → Seat available → First person in `waitlist` table → Gets booking with PENDING_APPROVAL (if has manager) or CONFIRMED (if no manager) → Auto-confirmation working!

### For Old WAITLISTED Bookings:
✅ Automatically migrated to new `waitlist` table when anyone cancels on that session
✅ Or can be bulk migrated using the SQL script

## Key Differences: Old vs New System

| Aspect | Old System (WAITLISTED Bookings) | New System (Waitlist Table) |
|--------|----------------------------------|----------------------------|
| **Storage** | Bookings table with status='WAITLISTED' | Separate waitlist table |
| **Position Tracking** | waitlist_position column in bookings | position column in waitlist |
| **Auto-Confirmation** | ❌ Not working | ✅ Working |
| **Manager Approval** | ❌ Bypassed | ✅ Respected |
| **FIFO Guarantee** | ⚠️ Unreliable | ✅ Guaranteed |
| **Separate from Bookings** | ❌ Mixed with actual bookings | ✅ Clean separation |

## Troubleshooting

### Issue: "Session is full. You have been added to the waitlist..."
**This is CORRECT behavior!** The user has been added to the waitlist. When someone cancels, they'll be automatically promoted.

### Issue: Still seeing WAITLISTED bookings in UI
**Solution:** Run the migration script `migrate-waitlisted-bookings.sql` to convert all old bookings at once.

### Issue: Auto-confirmation not working
**Check:**
1. Application was rebuilt and restarted ✓
2. Waitlist table exists in database ✓
3. User is in `waitlist` table (not `bookings` with WAITLISTED status) ✓
4. Application logs show "Processing waitlist for session X" when cancelling ✓

## Application Logs to Monitor

When cancelling a booking, you should see:
```
Processing waitlist for session 123 after booking cancellation. Available seats: 1
Session 123 has 1 available seats and 50 total capacity
Found 1 people in waitlist for session 123. Attempting to fill 1 seats
User 456 has a manager. Booking from waitlist will require manager approval
User 456 moved from waitlist position 1 to PENDING_APPROVAL for session 123 (requires manager approval)
```

If you also have old WAITLISTED bookings:
```
Found 2 old WAITLISTED bookings for session 123. Migrating to new waitlist table...
Migrated user 789 from WAITLISTED booking 999 to new waitlist table
```

---

**Status:** Ready for deployment and testing
**Created:** December 4, 2025
