# Manager Approval Workflow for Training Bookings

## Overview
This document describes the manager approval workflow implemented for the Global Learning Calendar application. When an employee books a training session, the booking requires approval from their direct manager before it's confirmed.

## Business Requirement
Employees might have key deliverables on the days of training sessions. Therefore, their manager must approve the booking request to ensure it doesn't conflict with critical work commitments.

## Implementation Details

### 1. Database Changes

#### New Columns in `bookings` Table:
- `rejected_by` (BIGINT) - Foreign key to users table, stores who rejected the booking
- `rejection_date` (DATETIME) - Timestamp when booking was rejected
- `rejection_reason` (TEXT) - Reason provided by manager for rejection
- `manager_notified` (BOOLEAN) - Flag to track if manager has been notified
- `manager_notified_date` (DATETIME) - Timestamp when manager was notified

#### New Booking Statuses:
- `PENDING_APPROVAL` - Booking awaiting manager approval
- `REJECTED` - Booking rejected by manager

#### Migration Script:
Run the SQL migration script: `src/main/resources/db-migration-manager-approval.sql`

### 2. Entity Changes

**Booking.java**
- Added `rejectedBy`, `rejectionDate`, `rejectionReason` fields
- Added `managerNotified`, `managerNotifiedDate` fields
- Updated `BookingStatus` enum with `PENDING_APPROVAL` and `REJECTED`

**User.java**
- Already has `manager` relationship (Many-to-One)
- Already has `directReports` relationship (One-to-Many)

### 3. API Endpoints

#### Create Booking (Modified)
```
POST /api/v1/bookings
```
- If employee has a manager: Creates booking with `PENDING_APPROVAL` status
- If employee has no manager: Auto-approves and creates with `CONFIRMED` status
- Seat is NOT decremented until manager approves

#### Approve Booking
```
POST /api/v1/bookings/{id}/confirm?approvedById={managerId}
```
- Manager approves the booking
- Status changes from `PENDING_APPROVAL` to `CONFIRMED`
- Available seats are decremented
- Validates that approver is the employee's direct manager

#### Reject Booking
```
POST /api/v1/bookings/{id}/reject?rejectedById={managerId}&rejectionReason={reason}
```
- Manager rejects the booking
- Status changes from `PENDING_APPROVAL` to `REJECTED`
- Records rejection reason and timestamp
- Validates that rejector is the employee's direct manager

#### Get Pending Approvals for Manager
```
GET /api/v1/bookings/manager/{managerId}/pending-approvals
```
- Returns all bookings awaiting approval from specific manager
- Ordered by booking date (oldest first)

#### Get All Pending Approvals
```
GET /api/v1/bookings/pending-approvals
```
- Returns all bookings in `PENDING_APPROVAL` status
- Useful for admin/system overview

#### Mark Manager Notified
```
POST /api/v1/bookings/{id}/notify-manager
```
- Marks that manager has been notified about the booking request
- Sets `managerNotified` to true and records timestamp

### 4. Workflow Flow

```
Employee Books Training
         ↓
   Has Manager?
    ↙        ↘
  NO          YES
   ↓           ↓
Auto-Approve  Status: PENDING_APPROVAL
   ↓           ↓
CONFIRMED   Notify Manager
             ↓
      Manager Reviews
       ↙         ↘
   APPROVE      REJECT
      ↓           ↓
  CONFIRMED    REJECTED
  (Seat -1)   (No seat used)
```

### 5. Service Layer Changes

**BookingServiceImpl.java**
- `createBooking()`: Modified to set `PENDING_APPROVAL` status when manager exists
- `confirmBooking()`: Enhanced to verify manager relationship and decrement seats
- `rejectBooking()`: New method for manager to reject bookings
- `getPendingApprovalsByManager()`: New method to fetch pending approvals
- `getAllPendingApprovals()`: New method to fetch all pending approvals
- `markManagerNotified()`: New method to track notification status

### 6. Repository Changes

**BookingRepository.java**
- Added `findPendingApprovalsByManager()`: Query to find pending approvals for a manager

### 7. DTO Changes

**BookingDTO.java**
- Added manager information fields: `managerId`, `managerName`, `managerEmail`
- Added rejection fields: `rejectedById`, `rejectedByName`, `rejectionDate`, `rejectionReason`
- Added notification fields: `managerNotified`, `managerNotifiedDate`

## Usage Examples

### Example 1: Employee Books Training

**Request:**
```json
POST /api/v1/bookings
{
  "userId": 5,
  "sessionId": 10,
  "notes": "Interested in this Java training"
}
```

**Response (if employee has manager):**
```json
{
  "id": 101,
  "bookingReference": "BK-A3F8B2C1",
  "userId": 5,
  "userName": "John Doe",
  "managerId": 3,
  "managerName": "Jane Smith",
  "managerEmail": "jane.smith@company.com",
  "sessionId": 10,
  "status": "PENDING_APPROVAL",
  "managerNotified": false,
  "bookingDate": "2025-12-02T10:30:00"
}
```

### Example 2: Manager Views Pending Approvals

**Request:**
```
GET /api/v1/bookings/manager/3/pending-approvals
```

**Response:**
```json
[
  {
    "id": 101,
    "bookingReference": "BK-A3F8B2C1",
    "userId": 5,
    "userName": "John Doe",
    "userEmail": "john.doe@company.com",
    "sessionId": 10,
    "programName": "Advanced Java Programming",
    "sessionStartDateTime": "2025-12-15T09:00:00",
    "sessionEndDateTime": "2025-12-15T17:00:00",
    "status": "PENDING_APPROVAL",
    "bookingDate": "2025-12-02T10:30:00"
  }
]
```

### Example 3: Manager Approves Booking

**Request:**
```
POST /api/v1/bookings/101/confirm?approvedById=3
```

**Response:**
```json
{
  "id": 101,
  "bookingReference": "BK-A3F8B2C1",
  "status": "CONFIRMED",
  "approvedById": 3,
  "approvedByName": "Jane Smith",
  "approvalDate": "2025-12-02T14:25:00",
  "confirmationDate": "2025-12-02T14:25:00"
}
```

### Example 4: Manager Rejects Booking

**Request:**
```
POST /api/v1/bookings/101/reject?rejectedById=3&rejectionReason=Critical project deadline on that day
```

**Response:**
```json
{
  "id": 101,
  "bookingReference": "BK-A3F8B2C1",
  "status": "REJECTED",
  "rejectedById": 3,
  "rejectedByName": "Jane Smith",
  "rejectionDate": "2025-12-02T14:30:00",
  "rejectionReason": "Critical project deadline on that day"
}
```

## Validation Rules

1. **Manager Relationship**: Only the employee's direct manager can approve/reject their bookings
2. **Status Validation**: Only bookings in `PENDING_APPROVAL` status can be approved or rejected
3. **Seat Availability**: Seats are only decremented when manager approves (not during initial booking)
4. **Auto-Approval**: Employees without managers get auto-approved bookings

## Notification Integration Points

While the actual notification system is not implemented in this version, the following hooks are available:

1. **After Booking Creation**: Notify manager when `status = PENDING_APPROVAL`
2. **After Approval**: Notify employee when booking is approved
3. **After Rejection**: Notify employee with rejection reason
4. **Reminder**: Periodic reminders to managers for pending approvals

The `markManagerNotified()` endpoint can be called after sending notifications.

## Future Enhancements

1. **Email Notifications**: Integrate with email service to notify managers and employees
2. **Approval Deadline**: Add time limit for manager approval (e.g., auto-approve after 48 hours)
3. **Escalation**: Escalate to higher-level manager if no response
4. **Bulk Approval**: Allow managers to approve multiple bookings at once
5. **Approval Comments**: Allow managers to add comments during approval
6. **Notification Dashboard**: UI for managers to view and act on pending approvals
7. **Mobile Push Notifications**: Real-time notifications on mobile devices

## Testing

### Manual Testing Steps:

1. **Setup Test Data:**
   - Create a manager user (e.g., userId=1)
   - Create an employee user with manager relationship (e.g., userId=2, managerId=1)
   - Create a learning session with available seats

2. **Test Booking Creation:**
   ```
   POST /api/v1/bookings
   {
     "userId": 2,
     "sessionId": 1
   }
   ```
   - Verify status is `PENDING_APPROVAL`
   - Verify seats are NOT decremented

3. **Test Manager View:**
   ```
   GET /api/v1/bookings/manager/1/pending-approvals
   ```
   - Verify booking appears in list

4. **Test Approval:**
   ```
   POST /api/v1/bookings/{bookingId}/confirm?approvedById=1
   ```
   - Verify status changes to `CONFIRMED`
   - Verify seats are decremented

5. **Test Rejection:**
   ```
   POST /api/v1/bookings/{bookingId}/reject?rejectedById=1&rejectionReason=Test
   ```
   - Verify status changes to `REJECTED`
   - Verify reason is recorded

6. **Test Auto-Approval:**
   - Create employee without manager
   - Create booking
   - Verify status is immediately `CONFIRMED`

## Troubleshooting

### Issue: Booking auto-approved when it shouldn't be
**Solution**: Check if user has manager assigned in database

### Issue: Manager can't approve booking
**Solution**: Verify `approvedById` matches employee's `manager_id`

### Issue: Seats not available after approval
**Solution**: Check if session still has available seats when approval happens

### Issue: Database constraint error on rejected_by
**Solution**: Ensure migration script was run to add foreign key constraint

## Database Schema Reference

```sql
-- Users table (existing)
users (
  id BIGINT PRIMARY KEY,
  manager_id BIGINT REFERENCES users(id),
  ...
)

-- Bookings table (updated)
bookings (
  id BIGINT PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  session_id BIGINT REFERENCES learning_sessions(id),
  status VARCHAR(50), -- Now includes PENDING_APPROVAL, REJECTED
  approved_by BIGINT REFERENCES users(id),
  approval_date DATETIME,
  rejected_by BIGINT REFERENCES users(id),
  rejection_date DATETIME,
  rejection_reason TEXT,
  manager_notified BOOLEAN,
  manager_notified_date DATETIME,
  ...
)
```

## Support

For questions or issues related to the manager approval workflow, contact the development team or refer to the main project README.

