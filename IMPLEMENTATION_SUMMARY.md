# Manager Approval Workflow - Implementation Summary

## Changes Made

### 1. **Entity Changes**

#### Booking.java
- Added new fields:
  - `rejectedBy` (User) - Manager who rejected the booking
  - `rejectionDate` (LocalDateTime) - When booking was rejected
  - `rejectionReason` (String) - Reason for rejection
  - `managerNotified` (Boolean) - Whether manager has been notified
  - `managerNotifiedDate` (LocalDateTime) - When manager was notified

- Updated BookingStatus enum:
  - Added `PENDING_APPROVAL` - Booking awaiting manager approval
  - Added `REJECTED` - Booking rejected by manager

#### User.java
- No changes needed (already has manager relationship)

### 2. **DTO Changes**

#### BookingDTO.java
- Added manager information fields:
  - `managerId`, `managerName`, `managerEmail`
- Added rejection tracking fields:
  - `rejectedById`, `rejectedByName`, `rejectionDate`, `rejectionReason`
- Added notification tracking fields:
  - `managerNotified`, `managerNotifiedDate`

### 3. **Repository Changes**

#### BookingRepository.java
- Added query method: `findPendingApprovalsByManager(Long managerId)`
  - Finds all bookings pending approval for a specific manager
  - Ordered by booking date (oldest first)

### 4. **Service Changes**

#### IBookingService.java
- Added new methods:
  - `rejectBooking(Long id, Long rejectedById, String rejectionReason)`
  - `getPendingApprovalsByManager(Long managerId)`
  - `getAllPendingApprovals()`
  - `markManagerNotified(Long id)`

#### BookingServiceImpl.java
- **Modified `createBooking()`**:
  - If employee has a manager: Creates booking with `PENDING_APPROVAL` status
  - If employee has no manager: Auto-approves with `CONFIRMED` status
  - Seat is NOT decremented until manager approves

- **Modified `confirmBooking()`**:
  - Now handles both `PENDING` and `PENDING_APPROVAL` statuses
  - Validates that approver is the employee's direct manager
  - Decrements available seats upon approval
  - Checks if session still has available seats

- **New `rejectBooking()`**:
  - Allows manager to reject booking with reason
  - Validates manager relationship
  - Records rejection details

- **New `getPendingApprovalsByManager()`**:
  - Returns all pending approvals for a specific manager

- **New `getAllPendingApprovals()`**:
  - Returns all pending approvals (admin view)

- **New `markManagerNotified()`**:
  - Marks manager as notified for tracking purposes

- **Updated `toDTO()`**:
  - Includes manager information
  - Includes rejection information

### 5. **Controller Changes**

#### BookingController.java
- **Modified endpoints**:
  - `POST /api/v1/bookings/{id}/confirm` - Enhanced with manager validation

- **New endpoints**:
  - `POST /api/v1/bookings/{id}/reject` - Reject a booking
  - `GET /api/v1/bookings/manager/{managerId}/pending-approvals` - Get pending approvals for manager
  - `GET /api/v1/bookings/pending-approvals` - Get all pending approvals
  - `POST /api/v1/bookings/{id}/notify-manager` - Mark manager as notified

### 6. **Database Changes**

#### Migration Script: `db-migration-manager-approval.sql`
- New columns added to `bookings` table:
  - `rejected_by` (BIGINT, FK to users)
  - `rejection_date` (DATETIME)
  - `rejection_reason` (TEXT)
  - `manager_notified` (BOOLEAN)
  - `manager_notified_date` (DATETIME)

- New indexes for performance:
  - `idx_bookings_user_manager_status` on (user_id, status)
  - `idx_bookings_manager_notified` on (manager_notified, status)

## Workflow

```
1. Employee creates booking
   ↓
2. System checks if employee has manager
   ↓
   ├── NO → Auto-approve (CONFIRMED)
   ↓
   └── YES → Set status to PENDING_APPROVAL
       ↓
   3. Manager receives notification
       ↓
   4. Manager reviews booking
       ↓
       ├── APPROVE → Status: CONFIRMED, Seat -1
       ↓
       └── REJECT → Status: REJECTED, No seat used
```

## Testing Instructions

### Prerequisites
1. Start the application: `mvn spring-boot:run`
2. Application runs on: `http://localhost:8080`
3. Swagger UI: `http://localhost:8080/swagger-ui.html`

### Test Scenario 1: Booking with Manager Approval

**Step 1: Create test users**
```bash
# Create a manager
POST http://localhost:8080/api/v1/users
{
  "employeeId": "MGR001",
  "email": "manager@company.com",
  "firstName": "Jane",
  "lastName": "Manager",
  "roleId": 2,
  "businessUnitId": 1
}

# Create an employee with manager
POST http://localhost:8080/api/v1/users
{
  "employeeId": "EMP001",
  "email": "employee@company.com",
  "firstName": "John",
  "lastName": "Employee",
  "roleId": 1,
  "managerId": 1,  // Set the manager ID
  "businessUnitId": 1
}
```

**Step 2: Create a learning session**
```bash
POST http://localhost:8080/api/v1/sessions
{
  "sessionCode": "SESS001",
  "programId": 1,
  "locationId": 1,
  "startDateTime": "2025-12-15T09:00:00",
  "endDateTime": "2025-12-15T17:00:00",
  "totalSeats": 20,
  "availableSeats": 20,
  "status": "SCHEDULED"
}
```

**Step 3: Employee books training**
```bash
POST http://localhost:8080/api/v1/bookings
{
  "userId": 2,  // Employee ID
  "sessionId": 1
}

# Expected Response:
{
  "status": "PENDING_APPROVAL",
  "managerId": 1,
  "managerName": "Jane Manager",
  "managerNotified": false
  // ... other fields
}
```

**Step 4: Manager views pending approvals**
```bash
GET http://localhost:8080/api/v1/bookings/manager/1/pending-approvals

# Should return the booking created in Step 3
```

**Step 5: Manager approves booking**
```bash
POST http://localhost:8080/api/v1/bookings/1/confirm?approvedById=1

# Expected Response:
{
  "status": "CONFIRMED",
  "approvedById": 1,
  "approvedByName": "Jane Manager",
  "approvalDate": "2025-12-02T...",
  "confirmationDate": "2025-12-02T..."
}
```

### Test Scenario 2: Booking Rejection

```bash
# Create booking (same as Step 3 above)
POST http://localhost:8080/api/v1/bookings
{
  "userId": 2,
  "sessionId": 1
}

# Manager rejects booking
POST http://localhost:8080/api/v1/bookings/1/reject?rejectedById=1&rejectionReason=Critical project deadline

# Expected Response:
{
  "status": "REJECTED",
  "rejectedById": 1,
  "rejectedByName": "Jane Manager",
  "rejectionDate": "2025-12-02T...",
  "rejectionReason": "Critical project deadline"
}
```

### Test Scenario 3: Employee Without Manager (Auto-Approval)

```bash
# Create employee without manager
POST http://localhost:8080/api/v1/users
{
  "employeeId": "EMP002",
  "email": "employee2@company.com",
  "firstName": "Alice",
  "lastName": "Independent",
  "roleId": 1,
  "businessUnitId": 1
  // No managerId
}

# Create booking
POST http://localhost:8080/api/v1/bookings
{
  "userId": 3,  // Employee without manager
  "sessionId": 1
}

# Expected Response:
{
  "status": "CONFIRMED",  // Auto-approved
  "confirmationDate": "2025-12-02T..."
  // No approval or manager fields
}
```

## Key Features Implemented

✅ Manager relationship validation
✅ Conditional approval workflow (based on manager existence)
✅ Seat reservation management (decrement only on approval)
✅ Rejection with reason tracking
✅ Manager notification tracking
✅ Pending approval queries for managers
✅ Auto-approval for employees without managers
✅ Database migration script
✅ Complete API documentation
✅ Postman collection for testing

## Files Created/Modified

### Created
- `MANAGER_APPROVAL_WORKFLOW.md` - Comprehensive documentation
- `IMPLEMENTATION_SUMMARY.md` - This file
- `postman-collection-manager-approval.json` - API test collection
- `db-migration-manager-approval.sql` - Database migration

### Modified
- `entity/Booking.java` - Added rejection and notification fields
- `dto/BookingDTO.java` - Added manager and rejection fields
- `service/IBookingService.java` - Added new method signatures
- `service/impl/BookingServiceImpl.java` - Implemented approval workflow
- `controller/BookingController.java` - Added new endpoints
- `repository/BookingRepository.java` - Added pending approvals query

## Build Status

✅ Compilation successful
✅ No blocking errors
⚠️ Only IDE warnings (database connection warnings - expected in development)

## Next Steps (Optional Enhancements)

1. **Email Notifications**: Integrate email service for manager notifications
2. **SMS/Push Notifications**: Real-time alerts for managers
3. **Approval Reminders**: Scheduled reminders for pending approvals
4. **Bulk Actions**: Allow managers to approve/reject multiple bookings
5. **Approval Delegation**: Allow managers to delegate approval authority
6. **Approval History**: Track approval history and audit trail
7. **UI Dashboard**: Create a manager dashboard for approvals
8. **Approval Timeout**: Auto-approve or escalate after X hours
9. **Multi-level Approval**: Require approval from multiple managers
10. **Calendar Integration**: Sync with manager's calendar for availability

## Support

For questions or issues:
1. Check `MANAGER_APPROVAL_WORKFLOW.md` for detailed documentation
2. Import Postman collection for API testing
3. Review entity classes for data model
4. Check service layer for business logic

---

**Implementation Date**: December 2, 2025  
**Status**: ✅ Complete and Ready for Testing

