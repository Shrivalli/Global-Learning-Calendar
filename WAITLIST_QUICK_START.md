# Waitlist Feature - Quick Start Testing Guide

## 🚀 Quick Setup (5 minutes)

### Step 1: Database Setup
```bash
# Navigate to backend project
cd global-learning-calendar

# Run the migration script (update with your credentials)
mysql -u root -p learning_calendar < src/main/resources/db-migration-waitlist.sql
```

### Step 2: Start Backend
```bash
# In the backend directory
mvn spring-boot:run
```

### Step 3: Start Frontend
```bash
# In the frontend directory
cd ../global-learning-calendar-frontend
npm install  # if needed
npm run dev
```

### Step 4: Verify Backend is Running
```bash
# Test API endpoint
curl http://localhost:8080/api/v1/waitlist/session/1/active
```

---

## 🧪 Testing Scenarios

### Scenario 1: Join a Waitlist (30 seconds)

1. **Find a Full Session**
   - Go to calendar or sessions page
   - Look for a session with `0` available seats
   
2. **Join Waitlist**
   - Click on the session
   - You should see "Join Waitlist" button (instead of "Book Session")
   - Click "Join Waitlist"
   
3. **Verify**
   - You should see a badge showing "Waitlist Position #1" (or #2, #3, etc.)
   - Check database:
     ```sql
     SELECT * FROM waitlist WHERE status = 'WAITING';
     ```

### Scenario 2: Test Auto-Confirmation (1 minute)

1. **Setup**
   - Make sure you're in waitlist for a session (Scenario 1)
   - Another user has a confirmed booking for the same session
   
2. **Trigger Cancellation**
   - As the other user, cancel their booking
   - OR use API:
     ```bash
     curl -X POST http://localhost:8080/api/v1/bookings/{bookingId}/cancel
     ```

3. **Verify Auto-Confirmation**
   - Check that you now have a CONFIRMED booking:
     ```sql
     SELECT * FROM bookings WHERE user_id = YOUR_USER_ID AND session_id = SESSION_ID;
     ```
   - Check waitlist status changed to CONFIRMED:
     ```sql
     SELECT * FROM waitlist WHERE user_id = YOUR_USER_ID AND session_id = SESSION_ID;
     ```
   - Refresh frontend - you should see "Booked" instead of waitlist

### Scenario 3: Leave Waitlist (15 seconds)

1. **Join Waitlist** (if not already in one)
2. **Click "Leave Waitlist"** button
3. **Verify**
   - Badge disappears
   - "Join Waitlist" button returns
   - Database shows REMOVED status:
     ```sql
     SELECT status FROM waitlist WHERE id = YOUR_WAITLIST_ID;
     ```

---

## 🔍 Quick Verification Commands

### Check Waitlist Table
```sql
-- See all active waitlist entries
SELECT 
    w.id,
    w.position,
    u.first_name,
    u.last_name,
    ls.session_code,
    w.joined_at
FROM waitlist w
JOIN users u ON w.user_id = u.id
JOIN learning_sessions ls ON w.session_id = ls.id
WHERE w.status = 'WAITING'
ORDER BY ls.id, w.position;
```

### Check Auto-Confirmations
```sql
-- See bookings created from waitlist
SELECT 
    b.booking_reference,
    u.first_name,
    u.last_name,
    ls.session_code,
    b.notes
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN learning_sessions ls ON b.session_id = ls.id
WHERE b.notes LIKE '%waitlist%';
```

### Check Session Availability
```sql
-- See sessions with waitlists
SELECT 
    ls.session_code,
    ls.total_seats,
    ls.available_seats,
    COUNT(w.id) as waiting_count,
    COUNT(b.id) as confirmed_count
FROM learning_sessions ls
LEFT JOIN waitlist w ON ls.id = w.session_id AND w.status = 'WAITING'
LEFT JOIN bookings b ON ls.id = b.session_id AND b.status = 'CONFIRMED'
GROUP BY ls.id;
```

---

## 🎯 API Testing with cURL

### Join Waitlist
```bash
curl -X POST http://localhost:8080/api/v1/waitlist/join \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 3,
    "userId": 1,
    "notes": "Really interested in this session"
  }'
```

### Check Position
```bash
curl "http://localhost:8080/api/v1/waitlist/position?sessionId=3&userId=1"
```

### Get Session Waitlist
```bash
curl "http://localhost:8080/api/v1/waitlist/session/3/active"
```

### Leave Waitlist
```bash
curl -X DELETE "http://localhost:8080/api/v1/waitlist/1?userId=1"
```

### Manual Process Waitlist (Admin)
```bash
curl -X POST "http://localhost:8080/api/v1/waitlist/session/3/process"
```

---

## 📊 Sample Data Included

The migration script includes sample waitlist entries:
- User 1 (John Doe) - Position #1 for Session 3
- User 4 (Emily Chen) - Position #2 for Session 3
- User 7 (Michael Johnson) - Position #1 for Session 5
- User 10 (Daniel Martinez) - Position #3 for Session 3
- User 6 - CONFIRMED status (was auto-confirmed from waitlist)

---

## 🐛 Common Issues & Solutions

### Issue: "Session has available seats" error
**Solution:** The session isn't actually full. Update it:
```sql
UPDATE learning_sessions SET available_seats = 0 WHERE id = 3;
```

### Issue: "Already in waitlist" error
**Solution:** Remove existing entry:
```sql
DELETE FROM waitlist WHERE session_id = 3 AND user_id = 1;
```

### Issue: Frontend button not showing
**Solution:** 
1. Check `isSessionFull` prop is true
2. Check `isAlreadyBooked` prop is false
3. Verify API is returning data

### Issue: Auto-confirmation not working
**Solution:** Check logs:
```bash
# In backend console, look for:
# "Processing waitlist for session X"
# "Auto-confirmed user Y from waitlist"
```

---

## ✅ Verification Checklist

After testing, verify:
- [ ] Can join waitlist when session is full
- [ ] Position number displays correctly
- [ ] Can leave waitlist
- [ ] Positions reorder after someone leaves
- [ ] Auto-confirmation works on cancellation
- [ ] Waitlist display shows all people
- [ ] Toast notifications appear
- [ ] Database records are correct
- [ ] No duplicate entries allowed
- [ ] Cannot join if already booked

---

## 📸 Expected UI Behavior

### When Session is Full
```
┌──────────────────────────────────────┐
│  Learning Session Details            │
│                                      │
│  Total Seats: 10                     │
│  Available: 0 (FULL)                 │
│                                      │
│  [Join Waitlist]                     │
└──────────────────────────────────────┘
```

### When in Waitlist
```
┌──────────────────────────────────────┐
│  Learning Session Details            │
│                                      │
│  Total Seats: 10                     │
│  Available: 0 (FULL)                 │
│                                      │
│  🕒 Waitlist Position #2             │
│  [Leave Waitlist]                    │
│                                      │
│  ℹ️ You're in the waitlist. You'll   │
│     be automatically confirmed if a  │
│     seat becomes available.          │
└──────────────────────────────────────┘
```

### Admin View with Waitlist Display
```
┌──────────────────────────────────────┐
│  🕒 Waitlist (3)                     │
│  Participants waiting for seats      │
│                                      │
│  ┌────────────────────────────────┐ │
│  │ #1  👤 John Doe                │ │
│  │     ✉️  john.doe@company.com   │ │
│  │     📅 Dec 3, 10:30            │ │
│  └────────────────────────────────┘ │
│                                      │
│  ┌────────────────────────────────┐ │
│  │ #2  👤 Jane Smith              │ │
│  │     ✉️  jane.smith@company.com │ │
│  │     📅 Dec 3, 11:45            │ │
│  └────────────────────────────────┘ │
└──────────────────────────────────────┘
```

---

## 🎓 Next Steps

Once basic testing is complete:

1. **Integrate into Session Details Page**
   - Import and use `WaitlistButton` component
   - Import and use `WaitlistDisplay` component (for admins)

2. **Add to User Dashboard**
   - Show user's active waitlist positions
   - Use `useUserWaitlists(userId)` hook

3. **Admin Features**
   - Add waitlist management to admin panel
   - Show waitlist statistics on dashboard

4. **Notifications** (Future Enhancement)
   - Email notification when auto-confirmed
   - Email notification when moved up in position

---

## 📞 Support

If you encounter issues:

1. Check backend logs: `logs/spring-boot-logger.log`
2. Check browser console for frontend errors
3. Verify database migrations ran successfully
4. Ensure all dependencies are installed
5. Review the full implementation guide: `WAITLIST_IMPLEMENTATION_GUIDE.md`

---

## 🎉 Success Criteria

You've successfully implemented waitlist when:
- ✅ Users can join waitlist on full sessions
- ✅ Positions display correctly
- ✅ Auto-confirmation works on cancellations
- ✅ Users can leave waitlist
- ✅ Positions reorder automatically
- ✅ No duplicate entries possible
- ✅ Database relationships are enforced
- ✅ UI is responsive and intuitive
