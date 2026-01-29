# 🚀 Quick Start - Database Fix

## Problem
When you cancel a booking and try to rebook, you get: **"User already has a booking for this session"**

## Solution (5 Minutes)

### Step 1: Open MySQL Command Line
```bash
mysql -u reni -p
# Enter password: password
```

### Step 2: Backup Bookings
```sql
USE learning_calendar_db;
CREATE TABLE bookings_backup AS SELECT * FROM bookings;
```

### Step 3: Stop Your Spring Boot App
- Press `Ctrl+C` in the terminal where app is running
- Or close the IDE run window

### Step 4: Drop Bookings Table
```sql
DROP TABLE bookings;
exit;
```

### Step 5: Start Spring Boot App
```bash
mvn spring-boot:run
```

Wait for: `Started GlobalLearningCalendarApplication`

### Step 6: Restore Data
Open MySQL again:
```sql
USE learning_calendar_db;
INSERT INTO bookings SELECT * FROM bookings_backup;
```

### Step 7: Verify
```sql
-- Check the constraint is gone
SHOW INDEX FROM bookings WHERE Key_name LIKE 'UK%';
```

Should show NO unique constraint on (user_id, session_id).

### Step 8: Test
1. Open frontend
2. Book a session
3. Cancel it
4. Rebook the same session ✅ Should work!

## Done! 🎉

Your application now allows:
- ✅ Rebooking after cancellation
- ✅ Rebooking after manager rejection
- ✅ Accurate booking counts in frontend

## Rollback (If Needed)
```sql
DROP TABLE bookings;
CREATE TABLE bookings AS SELECT * FROM bookings_backup;
```

---
**Need help?** See `DATABASE_FIX_INSTRUCTIONS.md` for detailed guide.

