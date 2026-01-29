# Manager Approval Workflow - Quick Reference

## API Endpoints Summary

### 📝 Create Booking
```http
POST /api/v1/bookings
Content-Type: application/json

{
  "userId": 2,
  "sessionId": 1,
  "notes": "Optional notes"
}
```
**Response**: 
- If has manager: `status = "PENDING_APPROVAL"`
- If no manager: `status = "CONFIRMED"`

---

### ✅ Approve Booking (Manager)
```http
POST /api/v1/bookings/{bookingId}/confirm?approvedById={managerId}
```
**Requirements**:
- Booking must be in `PENDING_APPROVAL` status
- `approvedById` must be employee's direct manager
- Session must have available seats

---

### ❌ Reject Booking (Manager)
```http
POST /api/v1/bookings/{bookingId}/reject?rejectedById={managerId}&rejectionReason={reason}
```
**Requirements**:
- Booking must be in `PENDING_APPROVAL` status
- `rejectedById` must be employee's direct manager

---

### 📋 Get Pending Approvals (Manager)
```http
GET /api/v1/bookings/manager/{managerId}/pending-approvals
```
**Returns**: All bookings awaiting this manager's approval

---

### 📊 Get All Pending Approvals (Admin)
```http
GET /api/v1/bookings/pending-approvals
```
**Returns**: All bookings in `PENDING_APPROVAL` status

---

### 🔔 Mark Manager Notified
```http
POST /api/v1/bookings/{bookingId}/notify-manager
```
**Use**: Track notification delivery status

---

### 📖 Get Booking Details
```http
GET /api/v1/bookings/{bookingId}
```
**Returns**: Full booking details including manager info

---

## Booking Status Flow

```
PENDING_APPROVAL → (Manager Approves) → CONFIRMED
PENDING_APPROVAL → (Manager Rejects) → REJECTED
(No Manager) → Auto → CONFIRMED
```

## Database Columns Added

| Column | Type | Description |
|--------|------|-------------|
| `rejected_by` | BIGINT | FK to users table |
| `rejection_date` | DATETIME | When rejected |
| `rejection_reason` | TEXT | Rejection reason |
| `manager_notified` | BOOLEAN | Notification flag |
| `manager_notified_date` | DATETIME | When notified |

## DTO Fields Added

**BookingDTO**:
- `managerId`, `managerName`, `managerEmail`
- `rejectedById`, `rejectedByName`, `rejectionDate`, `rejectionReason`
- `managerNotified`, `managerNotifiedDate`

## Testing Commands

### 1. Start Application
```bash
cd G:\App-Dev-V3\global-learning-calendar
mvn spring-boot:run
```

### 2. Import Postman Collection
- File: `postman-collection-manager-approval.json`
- Import in Postman
- Update IDs in path variables

### 3. Access Swagger UI
```
http://localhost:8080/swagger-ui.html
```

## Common Scenarios

### Scenario 1: Normal Approval Flow
1. Employee books → `PENDING_APPROVAL`
2. Manager checks pending → `GET /manager/{id}/pending-approvals`
3. Manager approves → `POST /{id}/confirm`
4. Status → `CONFIRMED`, seat decremented

### Scenario 2: Rejection Flow
1. Employee books → `PENDING_APPROVAL`
2. Manager rejects → `POST /{id}/reject`
3. Status → `REJECTED`, seat not used
4. Employee notified with reason

### Scenario 3: No Manager (Auto-Approve)
1. Employee books → `CONFIRMED` immediately
2. Seat decremented immediately
3. No approval needed

## Validation Rules

✅ Only direct manager can approve/reject
✅ Only `PENDING_APPROVAL` bookings can be approved/rejected
✅ Seats decremented only on approval (not on initial booking)
✅ Employees without managers get auto-approved

## Error Scenarios

| Error | Cause | Solution |
|-------|-------|----------|
| "Only pending bookings can be confirmed" | Wrong status | Check booking status |
| "Only the employee's direct manager can approve" | Wrong manager | Verify manager relationship |
| "Session no longer has available seats" | Seats filled | Check session availability |
| "Only pending approval bookings can be rejected" | Wrong status | Check booking status |

## Files to Review

1. **Documentation**: `MANAGER_APPROVAL_WORKFLOW.md`
2. **Implementation Details**: `IMPLEMENTATION_SUMMARY.md`
3. **API Tests**: `postman-collection-manager-approval.json`
4. **Database Migration**: `db-migration-manager-approval.sql`

## Key Code Locations

- **Entity**: `entity/Booking.java` (lines for new fields)
- **Service**: `service/impl/BookingServiceImpl.java` (createBooking, confirmBooking, rejectBooking)
- **Controller**: `controller/BookingController.java` (new endpoints)
- **Repository**: `repository/BookingRepository.java` (findPendingApprovalsByManager)

## Tips

💡 Use Swagger UI for interactive API testing
💡 Check manager relationship in User entity before testing
💡 Verify session has available seats before approval
💡 Use rejection reason to provide clear feedback to employees
💡 Track manager notifications for follow-up reminders

---

**Quick Start**: See `IMPLEMENTATION_SUMMARY.md` for full testing instructions

