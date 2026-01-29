# Waitlist Functionality Implementation Guide

## Overview
This document explains the complete implementation of the waitlist functionality for the Global Learning Calendar application. When sessions are full, employees can join a waitlist and will be automatically confirmed if a seat becomes available due to cancellations.

---

## 🎯 What Was Implemented

### Backend Components (Java/Spring Boot)
1. **Database Schema** - New `waitlist` table with relationships
2. **Entity Layer** - `Waitlist.java` entity with status enum
3. **Repository Layer** - `WaitlistRepository.java` with query methods
4. **DTO Layer** - `WaitlistDTO.java` for data transfer
5. **Service Layer** - `WaitlistServiceImpl.java` with business logic
6. **Controller Layer** - `WaitlistController.java` REST API endpoints
7. **Integration** - Updated `BookingServiceImpl.java` to process waitlist on cancellations

### Frontend Components (React/TypeScript)
1. **Type Definitions** - Added waitlist types to `domain.ts`
2. **API Hooks** - Created `useWaitlist.ts` with React Query hooks
3. **UI Components** - `WaitlistButton.tsx` and `WaitlistDisplay.tsx`

---

## 📊 Database Schema

### Waitlist Table Structure

```sql
CREATE TABLE waitlist (
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
    
    -- Foreign Keys
    CONSTRAINT fk_waitlist_session FOREIGN KEY (session_id) 
        REFERENCES learning_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT uk_waitlist_session_user UNIQUE (session_id, user_id),
    
    -- Indexes
    INDEX idx_waitlist_session_status (session_id, status),
    INDEX idx_waitlist_position (session_id, position),
    INDEX idx_waitlist_user (user_id, status)
);
```

### Waitlist Status Enum
- **WAITING** - User is currently in the waitlist
- **CONFIRMED** - User was automatically moved from waitlist to confirmed booking
- **EXPIRED** - Waitlist entry expired (session started or was cancelled)
- **REMOVED** - User manually removed themselves from waitlist

---

## 🔄 Complete Workflow

### 1. User Joins Waitlist

**Trigger:** User clicks "Join Waitlist" button on a full session

**Frontend Flow:**
```typescript
// WaitlistButton.tsx
const response = await joinMutation.mutateAsync({
  sessionId,
  userId,
});
```

**API Call:**
```
POST /api/v1/waitlist/join
Body: { sessionId: 123, userId: 456, notes: "Optional notes" }
```

**Backend Processing:**
1. Validates session exists and is full
2. Validates user exists
3. Checks if user already in waitlist (prevents duplicates)
4. Checks if user already has a booking
5. Calculates next position in waitlist
6. Creates waitlist entry
7. Returns waitlist details with position

**Response:**
```json
{
  "success": true,
  "message": "Successfully joined waitlist",
  "waitlistId": 789,
  "position": 3,
  "data": { /* WaitlistDTO */ }
}
```

### 2. Booking Cancellation Triggers Waitlist Processing

**Trigger:** Someone cancels their confirmed booking

**Backend Flow:**
```java
// BookingServiceImpl.java - cancelBooking()
if (wasConfirmed) {
    session.incrementAvailableSeats();
    learningSessionRepository.save(session);
    
    // Process waitlist automatically
    waitlistService.processWaitlistForCancellation(session.getId());
}
```

**Waitlist Processing Logic:**
```java
// WaitlistServiceImpl.java - processWaitlistForCancellation()
1. Check if seats are available
2. Get waitlist entries ordered by position
3. For each available seat:
   a. Create confirmed booking for next person in line
   b. Update waitlist status to CONFIRMED
   c. Set notified timestamp
   d. Decrement available seats
4. Reorder remaining waitlist positions
```

### 3. Auto-Confirmation Process

**What Happens:**
1. First person in waitlist gets a **confirmed booking** automatically
2. Their waitlist status changes from `WAITING` to `CONFIRMED`
3. A `notifiedAt` timestamp is recorded
4. Session's available seats are decremented
5. Remaining waitlist positions are reordered (2→1, 3→2, etc.)

**Example:**
```
Before Cancellation:
- Session: 10 seats, 0 available
- Waitlist: Alice (#1), Bob (#2), Carol (#3)

After Cancellation:
- Session: 10 seats, 0 available (Alice auto-booked)
- Bookings: Alice moved to CONFIRMED
- Waitlist: Bob (#1), Carol (#2) (positions reordered)
```

### 4. User Leaves Waitlist

**Trigger:** User clicks "Leave Waitlist" button

**Frontend Flow:**
```typescript
await removeMutation.mutateAsync({
  waitlistId: entry.id,
  userId: currentUserId,
});
```

**API Call:**
```
DELETE /api/v1/waitlist/{waitlistId}?userId={userId}
```

**Backend Processing:**
1. Validates waitlist entry exists
2. Verifies user owns the entry (authorization)
3. Changes status to `REMOVED`
4. Reorders remaining waitlist positions

---

## 🔌 API Endpoints

### Join Waitlist
```http
POST /api/v1/waitlist/join
Content-Type: application/json

{
  "sessionId": 123,
  "userId": 456,
  "notes": "Optional notes"
}

Response: 201 Created
{
  "success": true,
  "message": "Successfully joined waitlist",
  "waitlistId": 789,
  "position": 3,
  "data": { /* WaitlistDTO */ }
}
```

### Remove from Waitlist
```http
DELETE /api/v1/waitlist/{waitlistId}?userId={userId}

Response: 200 OK
{
  "success": true,
  "message": "Successfully removed from waitlist"
}
```

### Get Session Waitlist
```http
GET /api/v1/waitlist/session/{sessionId}?status=WAITING

Response: 200 OK
[
  {
    "id": 1,
    "sessionId": 123,
    "userId": 456,
    "position": 1,
    "status": "WAITING",
    "joinedAt": "2025-12-03T10:30:00",
    ...
  }
]
```

### Get User's Waitlists
```http
GET /api/v1/waitlist/user/{userId}

Response: 200 OK
[
  {
    "id": 1,
    "sessionId": 123,
    "position": 2,
    "status": "WAITING",
    ...
  }
]
```

### Check Waitlist Position
```http
GET /api/v1/waitlist/position?sessionId=123&userId=456

Response: 200 OK
{
  "inWaitlist": true,
  "position": 3,
  "message": "You are at position 3 in the waitlist"
}
```

### Process Waitlist (Admin)
```http
POST /api/v1/waitlist/session/{sessionId}/process

Response: 200 OK
{
  "success": true,
  "message": "Waitlist processed successfully"
}
```

---

## 🎨 Frontend Components

### WaitlistButton Component

**Location:** `src/features/bookings/components/WaitlistButton.tsx`

**Usage:**
```tsx
<WaitlistButton
  sessionId={session.id}
  userId={currentUser.id}
  isSessionFull={session.availableSeats === 0}
  isAlreadyBooked={hasExistingBooking}
  onWaitlistChange={() => refetchSession()}
/>
```

**Features:**
- Shows "Join Waitlist" button when session is full
- Displays current position badge when in waitlist
- Allows leaving waitlist
- Shows confirmation alerts
- Handles loading states
- Displays toast notifications

### WaitlistDisplay Component

**Location:** `src/features/bookings/components/WaitlistDisplay.tsx`

**Usage:**
```tsx
<WaitlistDisplay
  sessionId={session.id}
  showTitle={true}
/>
```

**Features:**
- Displays all people in waitlist with positions
- Shows user name, email, and join time
- Responsive card layout
- Auto-hides when waitlist is empty
- Loading skeletons

---

## 🔒 Business Rules & Validations

### Join Waitlist Validations
1. ✅ Session must exist
2. ✅ User must exist
3. ✅ Session must be full (no available seats)
4. ✅ User cannot be in waitlist if already booked
5. ✅ User cannot join waitlist twice for same session
6. ✅ User should book directly if seats are available

### Auto-Confirmation Rules
1. ✅ Only triggered when confirmed booking is cancelled
2. ✅ Only processes if seats become available
3. ✅ Always confirms in FIFO (First In, First Out) order
4. ✅ Creates actual booking with CONFIRMED status
5. ✅ Records notification timestamp
6. ✅ Reorders remaining waitlist automatically

### Leave Waitlist Validations
1. ✅ Waitlist entry must exist
2. ✅ User must own the waitlist entry
3. ✅ Changes status to REMOVED (soft delete)
4. ✅ Reorders remaining positions

---

## 🗄️ Database Migration Steps

### Step 1: Run the Migration Script
```bash
# Navigate to the resources folder
cd global-learning-calendar/src/main/resources

# Execute the migration SQL
mysql -u your_username -p your_database < db-migration-waitlist.sql
```

### Step 2: Verify Tables
```sql
-- Check table was created
SHOW TABLES LIKE 'waitlist';

-- Check table structure
DESCRIBE waitlist;

-- Check sample data
SELECT * FROM waitlist;
```

### Step 3: Verify Relationships
```sql
-- Check foreign key constraints
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'waitlist' 
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## 🧪 Testing the Implementation

### Test Scenario 1: Join Waitlist
1. Find a session with 0 available seats
2. Click "Join Waitlist" button
3. Verify position badge appears
4. Check database: `SELECT * FROM waitlist WHERE session_id = ?`

### Test Scenario 2: Auto-Confirmation
1. User A is in waitlist position #1
2. Someone cancels their booking
3. Verify User A gets auto-confirmed booking
4. Check booking: `SELECT * FROM bookings WHERE user_id = ? AND session_id = ?`
5. Check waitlist status changed: `SELECT status FROM waitlist WHERE id = ?`

### Test Scenario 3: Leave Waitlist
1. Join waitlist
2. Click "Leave Waitlist"
3. Verify removed from list
4. Check status: `SELECT status FROM waitlist WHERE id = ?` → Should be 'REMOVED'

### Test Scenario 4: Position Reordering
1. Three users in waitlist: A(#1), B(#2), C(#3)
2. A leaves waitlist
3. Verify B becomes #1, C becomes #2

---

## 📝 Sample Data Queries

### View All Waitlist Entries with Details
```sql
SELECT 
    w.id,
    w.position,
    w.status,
    ls.session_code,
    u.first_name,
    u.last_name,
    u.email,
    w.joined_at
FROM waitlist w
JOIN learning_sessions ls ON w.session_id = ls.id
JOIN users u ON w.user_id = u.id
WHERE w.status = 'WAITING'
ORDER BY ls.id, w.position;
```

### Count Waitlist by Session
```sql
SELECT 
    ls.session_code,
    ls.total_seats,
    ls.available_seats,
    COUNT(w.id) as waitlist_count
FROM learning_sessions ls
LEFT JOIN waitlist w ON ls.id = w.session_id AND w.status = 'WAITING'
GROUP BY ls.id
HAVING waitlist_count > 0;
```

### Find Auto-Confirmed Bookings from Waitlist
```sql
SELECT 
    b.booking_reference,
    u.first_name,
    u.last_name,
    ls.session_code,
    b.confirmation_date,
    b.notes
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN learning_sessions ls ON b.session_id = ls.id
WHERE b.notes LIKE '%waitlist%'
ORDER BY b.confirmation_date DESC;
```

---

## 🚀 Integration Guide

### Adding Waitlist to Session Details Page

```tsx
import { WaitlistButton } from '@/features/bookings/components/WaitlistButton';
import { WaitlistDisplay } from '@/features/bookings/components/WaitlistDisplay';

function SessionDetailsPage() {
  const { data: session } = useSession(sessionId);
  const { data: currentUser } = useCurrentUser();
  
  const isSessionFull = session?.availableSeats === 0;
  const hasBooking = /* check if user has booking */;
  
  return (
    <div>
      {/* Session details */}
      
      {/* Booking button or waitlist button */}
      {!isSessionFull ? (
        <BookButton sessionId={session.id} userId={currentUser.id} />
      ) : (
        <WaitlistButton
          sessionId={session.id}
          userId={currentUser.id}
          isSessionFull={isSessionFull}
          isAlreadyBooked={hasBooking}
        />
      )}
      
      {/* Show waitlist for admins/managers */}
      {isAdmin && (
        <WaitlistDisplay sessionId={session.id} />
      )}
    </div>
  );
}
```

---

## ⚙️ Configuration

### Backend Configuration
No additional configuration needed. The waitlist service uses:
- Existing database connection
- Existing transaction management
- Existing error handling

### Frontend Configuration
Ensure API base URL is configured in:
```typescript
// src/config/index.ts
export const config = {
  apiBaseUrl: process.env.REACT_APP_API_URL || 'http://localhost:8080',
};
```

---

## 🐛 Troubleshooting

### Issue: "Already in waitlist" error
**Solution:** Check if user has existing WAITING entry:
```sql
SELECT * FROM waitlist WHERE session_id = ? AND user_id = ? AND status = 'WAITING';
```

### Issue: Auto-confirmation not working
**Solution:** Verify BookingService is calling waitlist service:
```java
// Should be in cancelBooking method
waitlistService.processWaitlistForCancellation(session.getId());
```

### Issue: Positions not reordering
**Solution:** Check reorderWaitlist method is being called after removals/confirmations

### Issue: Frontend components not showing
**Solution:** 
1. Verify API endpoints are accessible
2. Check React Query hooks are properly configured
3. Ensure components are imported and used correctly

---

## 📚 Key Files Reference

### Backend Files
```
global-learning-calendar/
├── src/main/resources/
│   └── db-migration-waitlist.sql          # Database migration
├── src/main/java/com/learning/globallearningcalendar/
│   ├── entity/
│   │   └── Waitlist.java                  # Entity definition
│   ├── repository/
│   │   └── WaitlistRepository.java        # Data access layer
│   ├── dto/
│   │   └── WaitlistDTO.java               # Data transfer object
│   ├── service/
│   │   ├── IWaitlistService.java          # Service interface
│   │   └── impl/
│   │       ├── WaitlistServiceImpl.java   # Service implementation
│   │       └── BookingServiceImpl.java    # Updated with waitlist integration
│   └── controller/
│       └── WaitlistController.java        # REST API endpoints
```

### Frontend Files
```
global-learning-calendar-frontend/
├── src/
│   ├── core/types/
│   │   └── domain.ts                      # Added waitlist types
│   └── features/bookings/
│       ├── api/
│       │   └── useWaitlist.ts             # API hooks
│       └── components/
│           ├── WaitlistButton.tsx         # Join/leave waitlist UI
│           └── WaitlistDisplay.tsx        # Show waitlist UI
```

---

## ✅ Implementation Checklist

- [x] Database table created with proper relationships
- [x] Entity class with proper annotations
- [x] Repository with custom queries
- [x] DTO for data transfer
- [x] Service interface defined
- [x] Service implementation with business logic
- [x] Controller with REST endpoints
- [x] BookingService integration for auto-confirmation
- [x] Frontend TypeScript types
- [x] Frontend API hooks with React Query
- [x] WaitlistButton component
- [x] WaitlistDisplay component
- [x] Sample data inserted
- [x] Error handling implemented
- [x] Validation rules enforced
- [x] Transaction management
- [x] Logging added

---

## 🎓 Understanding the Code Flow

### High-Level Flow Diagram

```
┌─────────────┐
│   User      │
│   Action    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Frontend (React)                   │
│  - WaitlistButton.tsx               │
│  - Calls useJoinWaitlist() hook     │
└──────┬──────────────────────────────┘
       │ HTTP POST /api/v1/waitlist/join
       ▼
┌─────────────────────────────────────┐
│  Backend Controller                 │
│  - WaitlistController.java          │
│  - Validates request                │
│  - Calls service layer              │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Service Layer                      │
│  - WaitlistServiceImpl.java         │
│  - Business logic & validations     │
│  - Calculates position              │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Repository Layer                   │
│  - WaitlistRepository.java          │
│  - Database operations              │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Database (MySQL)                   │
│  - waitlist table                   │
│  - INSERT new entry                 │
└─────────────────────────────────────┘
```

### Auto-Confirmation Flow

```
┌─────────────┐
│  Booking    │
│  Cancelled  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│  BookingServiceImpl.cancelBooking() │
│  - Updates booking status           │
│  - Increments available seats       │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  WaitlistService.process...()       │
│  - Checks available seats           │
│  - Gets first in waitlist           │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Create Confirmed Booking           │
│  - New booking with CONFIRMED       │
│  - Update waitlist to CONFIRMED     │
│  - Reorder remaining positions      │
└─────────────────────────────────────┘
```

---

## 🎯 Summary

The waitlist functionality has been fully implemented with:

1. **Robust Backend** - Complete service layer with validation and business logic
2. **Clean API** - RESTful endpoints following best practices
3. **User-Friendly Frontend** - Intuitive React components
4. **Automatic Processing** - Seamless auto-confirmation on cancellations
5. **Data Integrity** - Foreign keys, constraints, and transaction management
6. **Scalability** - Proper indexing and efficient queries

The implementation maintains existing functionality while adding powerful waitlist capabilities that enhance the user experience and maximize session utilization.
