# 📢 Frontend Team - Manager Approval Feature Implementation

## 🎯 What You Need to Know

The backend has implemented a **manager approval workflow** for training bookings. This document tells you exactly what to build in React.

---

## ⚡ TL;DR (Too Long; Didn't Read)

### What Changed:
- Employees who have managers need approval before booking is confirmed
- Managers get a dashboard to approve/reject booking requests
- New booking status: `PENDING_APPROVAL`
- 5 new API endpoints to integrate

### What You Build:
1. **Manager Dashboard** - To view and approve/reject requests
2. **Status Badges** - Yellow "Pending Approval", Green "Confirmed", Red "Rejected"
3. **Approval Modal** - For managers to approve with one click
4. **Rejection Modal** - For managers to reject with a reason
5. **Enhanced Booking Cards** - Show approval status to employees

---

## 📚 Documentation Files

| File | Purpose | Who Reads It |
|------|---------|--------------|
| **FRONTEND_INTEGRATION_GUIDE.md** | Complete integration guide with React code examples | All Frontend Devs |
| **UI_MOCKUPS.md** | Visual mockups of all screens | UI/UX Devs, Designers |
| **QUICK_REFERENCE.md** | Quick API reference | Backend Integration Dev |
| **WORKFLOW_DIAGRAMS.md** | Flow diagrams | Tech Lead, Architects |

---

## 🚀 Quick Start for Frontend Team

### Step 1: Read the Integration Guide
👉 **Start here**: `FRONTEND_INTEGRATION_GUIDE.md`

This file contains:
- ✅ Complete React component examples (copy-paste ready)
- ✅ API endpoint details with request/response examples
- ✅ State management examples (Redux)
- ✅ Error handling code
- ✅ Testing checklist

### Step 2: Check the UI Mockups
👉 **Visual reference**: `UI_MOCKUPS.md`

This file shows:
- ✅ ASCII mockups of every screen
- ✅ Color codes for status badges
- ✅ Icon references
- ✅ Mobile responsive layouts
- ✅ Button states (hover, disabled, loading)

### Step 3: Integrate the APIs
👉 **API details**: `QUICK_REFERENCE.md`

Quick reference for:
- ✅ All 5 new endpoints
- ✅ HTTP methods and parameters
- ✅ Example requests
- ✅ Example responses
- ✅ Common error scenarios

---

## 🎨 UI Components You Need to Build

### 1. BookingStatusBadge Component
**File**: `BookingStatusBadge.jsx`

Displays colored badges for booking status:
- 🟡 `PENDING_APPROVAL` - Yellow
- 🟢 `CONFIRMED` - Green  
- 🔴 `REJECTED` - Red
- 🔵 `WAITLISTED` - Blue
- ⚫ `CANCELLED` - Gray
- 🟣 `COMPLETED` - Purple

**Code**: See `FRONTEND_INTEGRATION_GUIDE.md` Section "UI Components to Build" → Item 1

---

### 2. ManagerApprovalDashboard Component
**File**: `ManagerApprovalDashboard.jsx`

Main screen for managers to:
- View all pending approval requests
- See employee details and training info
- Approve bookings (green button)
- Reject bookings (red button)
- Shows count badge

**Code**: See `FRONTEND_INTEGRATION_GUIDE.md` Section "UI Components to Build" → Item 2

---

### 3. ApprovalModal Component
**File**: `ApprovalModal.jsx`

Modal dialog for:
- Confirming approval (simple)
- Rejecting with reason (textarea required)

**Code**: See `FRONTEND_INTEGRATION_GUIDE.md` Section "UI Components to Build" → Item 3

---

### 4. Enhanced BookingCard Component
**File**: `MyBookings.jsx` or `BookingCard.jsx`

Shows booking status to employees:
- If `PENDING_APPROVAL`: Show yellow box "Awaiting manager approval"
- If `CONFIRMED`: Show green box "Approved by [Manager Name]"
- If `REJECTED`: Show red box with rejection reason

**Code**: See `FRONTEND_INTEGRATION_GUIDE.md` Section "UI Components to Build" → Item 4

---

### 5. PendingApprovalsBadge Component
**File**: `NavigationBadge.jsx`

Red notification badge in navigation:
- Shows count of pending approvals
- Only visible to managers
- Updates every 60 seconds

**Code**: See `FRONTEND_INTEGRATION_GUIDE.md` Section "UI Components to Build" → Item 5

---

## 📡 API Endpoints to Integrate

### 1. Create Booking (Modified)
```
POST /api/v1/bookings
```
**What Changed**: Response now includes `managerId`, `managerName`, and status might be `PENDING_APPROVAL`

---

### 2. Get Pending Approvals (NEW)
```
GET /api/v1/bookings/manager/{managerId}/pending-approvals
```
**Purpose**: Manager sees their pending approval requests

---

### 3. Approve Booking (NEW)
```
POST /api/v1/bookings/{bookingId}/confirm?approvedById={managerId}
```
**Purpose**: Manager approves a booking

---

### 4. Reject Booking (NEW)
```
POST /api/v1/bookings/{bookingId}/reject?rejectedById={managerId}&rejectionReason={reason}
```
**Purpose**: Manager rejects a booking with reason

---

### 5. Mark Notified (NEW)
```
POST /api/v1/bookings/{bookingId}/notify-manager
```
**Purpose**: Track that notification was sent (optional, for future use)

---

## 🎯 User Stories to Implement

### Story 1: Employee Books Training
```
AS AN employee
WHEN I book a training session
THEN I see "Pending Approval" status if I have a manager
AND I see "Confirmed" status if I don't have a manager
AND I see my manager's name in the booking details
```

### Story 2: Manager Views Pending Approvals
```
AS A manager
WHEN I navigate to the Approvals section
THEN I see a list of all pending approval requests from my team
AND each request shows employee name, training details, and request date
AND I see a count badge in the navigation
```

### Story 3: Manager Approves Booking
```
AS A manager
WHEN I click "Approve" on a pending request
THEN I see a confirmation modal
AND when I confirm, the booking status changes to "Confirmed"
AND the request is removed from my pending list
AND the employee sees the approved status
```

### Story 4: Manager Rejects Booking
```
AS A manager  
WHEN I click "Reject" on a pending request
THEN I see a modal asking for rejection reason
AND when I submit with reason, the booking status changes to "Rejected"
AND the employee sees the rejection with reason
```

### Story 5: Employee Sees Approval Status
```
AS AN employee
WHEN I view my bookings
THEN I see different messages based on status:
- Pending: "Awaiting approval from [Manager Name]"
- Approved: "Approved by [Manager Name] on [Date]"
- Rejected: "Rejected by [Manager Name]. Reason: [Reason]"
```

---

## ✅ Implementation Checklist

### Phase 1: Basic Display (Week 1)
- [ ] Create `BookingStatusBadge` component
- [ ] Update booking creation to handle `PENDING_APPROVAL` status
- [ ] Show pending status message to employees
- [ ] Display manager name in booking details

### Phase 2: Manager Dashboard (Week 2)
- [ ] Create `ManagerApprovalDashboard` component
- [ ] Integrate `GET /pending-approvals` API
- [ ] Display list of pending requests
- [ ] Add navigation badge with count
- [ ] Show empty state when no pending requests

### Phase 3: Approval Actions (Week 2)
- [ ] Create `ApprovalModal` component for approve/reject
- [ ] Integrate `POST /confirm` API (approve)
- [ ] Integrate `POST /reject` API (reject)
- [ ] Show success/error messages
- [ ] Refresh pending list after action

### Phase 4: Enhanced Employee View (Week 3)
- [ ] Show approval status in booking cards
- [ ] Display rejection reason to employee
- [ ] Show approver name and date
- [ ] Add appropriate styling/colors

### Phase 5: Polish & Testing (Week 3)
- [ ] Add loading states
- [ ] Implement error handling
- [ ] Add animations (optional)
- [ ] Test on mobile devices
- [ ] Accessibility testing
- [ ] Integration testing with backend

---

## 🎨 Design Tokens

### Colors (Tailwind CSS)
```javascript
const statusColors = {
  PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  WAITLISTED: 'bg-blue-100 text-blue-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
  COMPLETED: 'bg-purple-100 text-purple-800'
};

const buttonColors = {
  approve: 'bg-green-500 hover:bg-green-600',
  reject: 'bg-red-500 hover:bg-red-600',
  cancel: 'bg-gray-500 hover:bg-gray-600'
};
```

### Icons
Use these emoji or replace with your icon library:
- ⏳ Pending
- ✅ Approved
- ❌ Rejected
- 📅 Date
- 🕐 Time
- 📍 Location
- 💬 Notes
- 👤 User

---

## 🧪 Testing Scenarios

### Test 1: Employee Without Manager
1. Login as employee with no manager
2. Book a training
3. **Expected**: Status immediately shows "Confirmed"

### Test 2: Employee With Manager  
1. Login as employee with manager
2. Book a training
3. **Expected**: Status shows "Pending Approval"
4. **Expected**: Shows manager name

### Test 3: Manager Approves
1. Login as manager
2. Go to Approvals section
3. See pending request
4. Click "Approve"
5. **Expected**: Booking confirmed, removed from list

### Test 4: Manager Rejects
1. Login as manager
2. Go to Approvals section
3. Click "Reject"
4. Enter reason
5. **Expected**: Booking rejected with reason

### Test 5: Badge Count
1. Login as manager
2. Check navigation badge
3. **Expected**: Shows count of pending approvals
4. Approve one request
5. **Expected**: Count decreases by 1

---

## 🆘 Need Help?

### Backend is Ready!
- ✅ All APIs are implemented and tested
- ✅ Swagger UI available: `http://localhost:8080/swagger-ui.html`
- ✅ Postman collection available: `postman-collection-manager-approval.json`

### Documentation Available:
1. **Frontend Guide**: `FRONTEND_INTEGRATION_GUIDE.md` (Complete React examples)
2. **UI Mockups**: `UI_MOCKUPS.md` (Visual designs)
3. **API Reference**: `QUICK_REFERENCE.md` (Quick API lookup)
4. **Workflow**: `WORKFLOW_DIAGRAMS.md` (Flow diagrams)

### Questions?
- Check Swagger UI for live API documentation
- Import Postman collection to test APIs manually
- Review code examples in `FRONTEND_INTEGRATION_GUIDE.md`

---

## 📝 Notes from Backend Team

1. **Manager Validation**: The backend validates that only the direct manager can approve/reject
2. **Seat Management**: Seats are only decremented when manager approves (not on booking)
3. **Auto-Approval**: Employees without managers get auto-approved immediately
4. **Status Enum**: Make sure to handle the new `PENDING_APPROVAL` and `REJECTED` statuses
5. **Manager Info**: All booking responses now include manager details if applicable

---

## 🚢 Ready to Ship!

Backend is **DONE** and waiting for frontend! 🎉

Follow this plan:
1. ✅ Week 1: Basic status display
2. ✅ Week 2: Manager dashboard + approval actions
3. ✅ Week 3: Polish + testing

**Estimated Effort**: 2-3 weeks for complete implementation

---

## 📞 Contact

For questions about:
- **Backend APIs**: Check `QUICK_REFERENCE.md` or Swagger UI
- **UI Design**: Check `UI_MOCKUPS.md`
- **React Implementation**: Check `FRONTEND_INTEGRATION_GUIDE.md`
- **Workflow Logic**: Check `WORKFLOW_DIAGRAMS.md`

**All documentation is in the project root folder!**

---

*Happy Coding! 🚀*

