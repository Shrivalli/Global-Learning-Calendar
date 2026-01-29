# Frontend Integration Guide - Manager Approval Workflow

## Overview for React Application Team

This guide provides everything needed to integrate the manager approval workflow into your React frontend application.

---

## 🎯 Quick Start

### What You Need to Know

1. **New Booking Status**: `PENDING_APPROVAL` - Bookings awaiting manager approval
2. **New API Endpoints**: 5 new endpoints for approval workflow
3. **New UI Components Needed**: Manager Dashboard, Approval Modal, Status Badges
4. **User Roles**: Check if user is a manager (has direct reports)

---

## 📡 API Integration

### Base URL
```javascript
const API_BASE_URL = 'http://localhost:8080/api/v1';
```

### 1. Create Booking (Modified Behavior)

**Endpoint**: `POST /api/v1/bookings`

**Request:**
```javascript
const createBooking = async (userId, sessionId, notes) => {
  const response = await fetch(`${API_BASE_URL}/bookings`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      userId: userId,
      sessionId: sessionId,
      notes: notes
    })
  });
  
  return await response.json();
};
```

**Response (if employee has manager):**
```json
{
  "id": 101,
  "bookingReference": "BK-A3F8B2C1",
  "userId": 5,
  "userName": "John Doe",
  "userEmail": "john.doe@company.com",
  "managerId": 3,
  "managerName": "Jane Smith",
  "managerEmail": "jane.smith@company.com",
  "sessionId": 10,
  "programName": "Advanced Java Programming",
  "sessionStartDateTime": "2025-12-15T09:00:00",
  "status": "PENDING_APPROVAL",  // KEY: Wait for manager approval
  "managerNotified": false,
  "bookingDate": "2025-12-02T10:30:00"
}
```

**Response (if employee has NO manager):**
```json
{
  "id": 102,
  "status": "CONFIRMED",  // KEY: Auto-approved
  "confirmationDate": "2025-12-02T10:30:00"
  // ... other fields
}
```

### 2. Get Pending Approvals for Manager

**Endpoint**: `GET /api/v1/bookings/manager/{managerId}/pending-approvals`

**Request:**
```javascript
const getPendingApprovals = async (managerId) => {
  const response = await fetch(
    `${API_BASE_URL}/bookings/manager/${managerId}/pending-approvals`
  );
  
  return await response.json();
};
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
    "userEmployeeId": "EMP001",
    "sessionId": 10,
    "sessionCode": "JAVA-ADV-001",
    "programName": "Advanced Java Programming",
    "sessionStartDateTime": "2025-12-15T09:00:00",
    "sessionEndDateTime": "2025-12-15T17:00:00",
    "sessionLocationName": "Bangalore Office",
    "status": "PENDING_APPROVAL",
    "bookingDate": "2025-12-02T10:30:00",
    "notes": "Looking forward to this training"
  }
]
```

### 3. Approve Booking

**Endpoint**: `POST /api/v1/bookings/{bookingId}/confirm?approvedById={managerId}`

**Request:**
```javascript
const approveBooking = async (bookingId, managerId) => {
  const response = await fetch(
    `${API_BASE_URL}/bookings/${bookingId}/confirm?approvedById=${managerId}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to approve booking');
  }
  
  return await response.json();
};
```

**Response:**
```json
{
  "id": 101,
  "status": "CONFIRMED",
  "approvedById": 3,
  "approvedByName": "Jane Smith",
  "approvalDate": "2025-12-02T14:25:00",
  "confirmationDate": "2025-12-02T14:25:00"
  // ... other fields
}
```

### 4. Reject Booking

**Endpoint**: `POST /api/v1/bookings/{bookingId}/reject?rejectedById={managerId}&rejectionReason={reason}`

**Request:**
```javascript
const rejectBooking = async (bookingId, managerId, reason) => {
  const encodedReason = encodeURIComponent(reason);
  const response = await fetch(
    `${API_BASE_URL}/bookings/${bookingId}/reject?rejectedById=${managerId}&rejectionReason=${encodedReason}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to reject booking');
  }
  
  return await response.json();
};
```

**Response:**
```json
{
  "id": 101,
  "status": "REJECTED",
  "rejectedById": 3,
  "rejectedByName": "Jane Smith",
  "rejectionDate": "2025-12-02T14:30:00",
  "rejectionReason": "Critical project deadline on that date"
  // ... other fields
}
```

### 5. Get Booking Details

**Endpoint**: `GET /api/v1/bookings/{bookingId}`

Use this to check booking status and get complete details including manager information.

---

## 🎨 UI Components to Build

### 1. **Booking Status Badge Component**

Display different colors based on booking status:

```jsx
// BookingStatusBadge.jsx
import React from 'react';

const BookingStatusBadge = ({ status }) => {
  const statusConfig = {
    'PENDING_APPROVAL': {
      color: 'bg-yellow-100 text-yellow-800',
      icon: '⏳',
      text: 'Pending Approval'
    },
    'CONFIRMED': {
      color: 'bg-green-100 text-green-800',
      icon: '✅',
      text: 'Confirmed'
    },
    'REJECTED': {
      color: 'bg-red-100 text-red-800',
      icon: '❌',
      text: 'Rejected'
    },
    'CANCELLED': {
      color: 'bg-gray-100 text-gray-800',
      icon: '🚫',
      text: 'Cancelled'
    },
    'WAITLISTED': {
      color: 'bg-blue-100 text-blue-800',
      icon: '⏰',
      text: 'Waitlisted'
    },
    'COMPLETED': {
      color: 'bg-purple-100 text-purple-800',
      icon: '🎓',
      text: 'Completed'
    }
  };

  const config = statusConfig[status] || statusConfig['PENDING_APPROVAL'];

  return (
    <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
      <span className="mr-1">{config.icon}</span>
      {config.text}
    </span>
  );
};

export default BookingStatusBadge;
```

### 2. **Manager Approval Dashboard**

Main component for managers to view and act on pending approvals:

```jsx
// ManagerApprovalDashboard.jsx
import React, { useState, useEffect } from 'react';
import BookingStatusBadge from './BookingStatusBadge';

const ManagerApprovalDashboard = ({ managerId }) => {
  const [pendingApprovals, setPendingApprovals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [showApprovalModal, setShowApprovalModal] = useState(false);
  const [actionType, setActionType] = useState(null); // 'approve' or 'reject'

  useEffect(() => {
    fetchPendingApprovals();
  }, [managerId]);

  const fetchPendingApprovals = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `${API_BASE_URL}/bookings/manager/${managerId}/pending-approvals`
      );
      const data = await response.json();
      setPendingApprovals(data);
    } catch (error) {
      console.error('Error fetching pending approvals:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = (booking) => {
    setSelectedBooking(booking);
    setActionType('approve');
    setShowApprovalModal(true);
  };

  const handleReject = (booking) => {
    setSelectedBooking(booking);
    setActionType('reject');
    setShowApprovalModal(true);
  };

  const confirmAction = async (rejectionReason = null) => {
    try {
      if (actionType === 'approve') {
        await approveBooking(selectedBooking.id, managerId);
      } else {
        await rejectBooking(selectedBooking.id, managerId, rejectionReason);
      }
      
      // Refresh the list
      await fetchPendingApprovals();
      setShowApprovalModal(false);
      setSelectedBooking(null);
      
      // Show success message
      alert(`Booking ${actionType === 'approve' ? 'approved' : 'rejected'} successfully!`);
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">Loading...</div>;
  }

  if (pendingApprovals.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-2xl font-bold mb-4">Pending Approvals</h2>
        <div className="text-center py-8 text-gray-500">
          <p className="text-lg">🎉 No pending approvals!</p>
          <p className="text-sm mt-2">All training requests have been processed.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Pending Approvals</h2>
        <span className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
          {pendingApprovals.length} Pending
        </span>
      </div>

      <div className="space-y-4">
        {pendingApprovals.map((booking) => (
          <div key={booking.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <h3 className="text-lg font-semibold">{booking.userName}</h3>
                  <BookingStatusBadge status={booking.status} />
                </div>
                
                <div className="text-gray-600 space-y-1">
                  <p className="font-medium text-gray-900">{booking.programName}</p>
                  <p className="text-sm">
                    📅 {new Date(booking.sessionStartDateTime).toLocaleDateString('en-US', {
                      weekday: 'short',
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric'
                    })}
                  </p>
                  <p className="text-sm">
                    🕐 {new Date(booking.sessionStartDateTime).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit'
                    })} - {new Date(booking.sessionEndDateTime).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </p>
                  <p className="text-sm">📍 {booking.sessionLocationName}</p>
                  <p className="text-sm text-gray-500">
                    Requested: {new Date(booking.bookingDate).toLocaleString()}
                  </p>
                  {booking.notes && (
                    <p className="text-sm italic mt-2">💬 "{booking.notes}"</p>
                  )}
                </div>
              </div>

              <div className="flex gap-2 ml-4">
                <button
                  onClick={() => handleApprove(booking)}
                  className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                >
                  ✅ Approve
                </button>
                <button
                  onClick={() => handleReject(booking)}
                  className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg font-medium transition-colors"
                >
                  ❌ Reject
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {showApprovalModal && (
        <ApprovalModal
          booking={selectedBooking}
          actionType={actionType}
          onConfirm={confirmAction}
          onCancel={() => {
            setShowApprovalModal(false);
            setSelectedBooking(null);
          }}
        />
      )}
    </div>
  );
};

export default ManagerApprovalDashboard;
```

### 3. **Approval/Rejection Modal**

```jsx
// ApprovalModal.jsx
import React, { useState } from 'react';

const ApprovalModal = ({ booking, actionType, onConfirm, onCancel }) => {
  const [rejectionReason, setRejectionReason] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (actionType === 'reject' && !rejectionReason.trim()) {
      alert('Please provide a reason for rejection');
      return;
    }

    setIsSubmitting(true);
    try {
      await onConfirm(rejectionReason);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 className="text-xl font-bold mb-4">
          {actionType === 'approve' ? '✅ Approve Booking' : '❌ Reject Booking'}
        </h3>

        <div className="mb-4 p-4 bg-gray-50 rounded">
          <p className="font-semibold">{booking.userName}</p>
          <p className="text-sm text-gray-600">{booking.programName}</p>
          <p className="text-sm text-gray-600">
            {new Date(booking.sessionStartDateTime).toLocaleDateString()}
          </p>
        </div>

        {actionType === 'reject' && (
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Reason for Rejection *
            </label>
            <textarea
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows="4"
              placeholder="e.g., Critical project deadline, Resource constraints, etc."
              required
            />
            <p className="text-xs text-gray-500 mt-1">
              This reason will be shared with the employee.
            </p>
          </div>
        )}

        {actionType === 'approve' && (
          <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded">
            <p className="text-sm text-green-800">
              ℹ️ Approving this booking will confirm the seat reservation for this employee.
            </p>
          </div>
        )}

        <div className="flex gap-3 justify-end">
          <button
            onClick={onCancel}
            disabled={isSubmitting}
            className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={isSubmitting}
            className={`px-4 py-2 rounded-lg font-medium text-white disabled:opacity-50 ${
              actionType === 'approve'
                ? 'bg-green-500 hover:bg-green-600'
                : 'bg-red-500 hover:bg-red-600'
            }`}
          >
            {isSubmitting ? 'Processing...' : actionType === 'approve' ? 'Confirm Approval' : 'Confirm Rejection'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ApprovalModal;
```

### 4. **Employee Booking Status View**

Show booking status with appropriate messaging:

```jsx
// MyBookings.jsx - Enhanced with approval status
import React from 'react';
import BookingStatusBadge from './BookingStatusBadge';

const BookingCard = ({ booking }) => {
  return (
    <div className="border rounded-lg p-4 mb-4">
      <div className="flex justify-between items-start mb-3">
        <div>
          <h3 className="text-lg font-semibold">{booking.programName}</h3>
          <p className="text-sm text-gray-600">{booking.sessionCode}</p>
        </div>
        <BookingStatusBadge status={booking.status} />
      </div>

      {/* Status-specific messages */}
      {booking.status === 'PENDING_APPROVAL' && (
        <div className="bg-yellow-50 border border-yellow-200 rounded p-3 mb-3">
          <p className="text-sm text-yellow-800">
            ⏳ <strong>Awaiting Manager Approval</strong>
          </p>
          <p className="text-xs text-yellow-700 mt-1">
            Your booking request has been sent to <strong>{booking.managerName}</strong> for approval.
            You will be notified once they review your request.
          </p>
        </div>
      )}

      {booking.status === 'CONFIRMED' && booking.approvedByName && (
        <div className="bg-green-50 border border-green-200 rounded p-3 mb-3">
          <p className="text-sm text-green-800">
            ✅ <strong>Approved</strong>
          </p>
          <p className="text-xs text-green-700 mt-1">
            Approved by {booking.approvedByName} on{' '}
            {new Date(booking.approvalDate).toLocaleDateString()}
          </p>
        </div>
      )}

      {booking.status === 'REJECTED' && (
        <div className="bg-red-50 border border-red-200 rounded p-3 mb-3">
          <p className="text-sm text-red-800">
            ❌ <strong>Rejected</strong>
          </p>
          <p className="text-xs text-red-700 mt-1">
            Rejected by {booking.rejectedByName} on{' '}
            {new Date(booking.rejectionDate).toLocaleDateString()}
          </p>
          {booking.rejectionReason && (
            <p className="text-xs text-red-700 mt-2 italic">
              <strong>Reason:</strong> {booking.rejectionReason}
            </p>
          )}
        </div>
      )}

      {/* Booking details */}
      <div className="text-sm text-gray-600 space-y-1">
        <p>📅 {new Date(booking.sessionStartDateTime).toLocaleDateString()}</p>
        <p>🕐 {new Date(booking.sessionStartDateTime).toLocaleTimeString()} - 
           {new Date(booking.sessionEndDateTime).toLocaleTimeString()}</p>
        <p>📍 {booking.sessionLocationName}</p>
      </div>
    </div>
  );
};

export default BookingCard;
```

### 5. **Navigation Badge for Managers**

Show count of pending approvals in navigation:

```jsx
// NavigationBadge.jsx
import React, { useState, useEffect } from 'react';

const PendingApprovalsBadge = ({ managerId }) => {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const fetchCount = async () => {
      try {
        const response = await fetch(
          `${API_BASE_URL}/bookings/manager/${managerId}/pending-approvals`
        );
        const data = await response.json();
        setCount(data.length);
      } catch (error) {
        console.error('Error fetching pending count:', error);
      }
    };

    fetchCount();
    
    // Poll every 60 seconds for new requests
    const interval = setInterval(fetchCount, 60000);
    
    return () => clearInterval(interval);
  }, [managerId]);

  if (count === 0) return null;

  return (
    <span className="ml-2 bg-red-500 text-white text-xs font-bold px-2 py-1 rounded-full">
      {count}
    </span>
  );
};

// Usage in Navigation
const Navigation = ({ user }) => {
  const isManager = user.directReports && user.directReports.length > 0;

  return (
    <nav>
      {/* ... other nav items ... */}
      
      {isManager && (
        <Link to="/approvals">
          Approvals
          <PendingApprovalsBadge managerId={user.id} />
        </Link>
      )}
    </nav>
  );
};
```

---

## 🔄 State Management Considerations

### Context/Redux Store Structure

```javascript
// bookingSlice.js (Redux Toolkit example)
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchPendingApprovals = createAsyncThunk(
  'bookings/fetchPendingApprovals',
  async (managerId) => {
    const response = await fetch(
      `${API_BASE_URL}/bookings/manager/${managerId}/pending-approvals`
    );
    return response.json();
  }
);

export const approveBooking = createAsyncThunk(
  'bookings/approve',
  async ({ bookingId, managerId }) => {
    const response = await fetch(
      `${API_BASE_URL}/bookings/${bookingId}/confirm?approvedById=${managerId}`,
      { method: 'POST' }
    );
    return response.json();
  }
);

export const rejectBooking = createAsyncThunk(
  'bookings/reject',
  async ({ bookingId, managerId, reason }) => {
    const response = await fetch(
      `${API_BASE_URL}/bookings/${bookingId}/reject?rejectedById=${managerId}&rejectionReason=${encodeURIComponent(reason)}`,
      { method: 'POST' }
    );
    return response.json();
  }
);

const bookingSlice = createSlice({
  name: 'bookings',
  initialState: {
    pendingApprovals: [],
    loading: false,
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPendingApprovals.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPendingApprovals.fulfilled, (state, action) => {
        state.loading = false;
        state.pendingApprovals = action.payload;
      })
      .addCase(fetchPendingApprovals.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      });
  }
});

export default bookingSlice.reducer;
```

---

## 🚦 User Flow Examples

### Flow 1: Employee Books Training

```
1. Employee navigates to Training Catalog
2. Selects a training session
3. Clicks "Book Now"
4. System checks if employee has manager
   
   IF HAS MANAGER:
   - Show message: "Your booking request will be sent to [Manager Name] for approval"
   - Create booking with status PENDING_APPROVAL
   - Show confirmation: "Booking request submitted! You'll be notified once approved."
   
   IF NO MANAGER:
   - Create booking with status CONFIRMED
   - Show confirmation: "Booking confirmed! Check your email for details."

5. Redirect to "My Bookings" page
```

### Flow 2: Manager Reviews Approvals

```
1. Manager sees badge notification (e.g., "3" pending)
2. Clicks on "Approvals" in navigation
3. Sees list of pending approval requests
4. For each request, can see:
   - Employee name and details
   - Training program details
   - Date/time/location
   - Any notes from employee
5. Manager clicks "Approve" or "Reject"
   
   IF APPROVE:
   - Confirm approval
   - Booking status → CONFIRMED
   - Show success message
   
   IF REJECT:
   - Modal opens asking for reason
   - Manager enters reason
   - Booking status → REJECTED
   - Show success message

6. Request removed from pending list
7. Employee receives notification
```

---

## 📱 Notifications (Future Enhancement)

### When to Show Notifications

```javascript
// Notification triggers to implement
const notificationTriggers = {
  // For Employees
  bookingApproved: {
    title: 'Training Approved! ✅',
    message: 'Your manager approved your training request for {programName}'
  },
  bookingRejected: {
    title: 'Training Request Declined ❌',
    message: 'Your training request was declined. Reason: {rejectionReason}'
  },
  
  // For Managers
  newApprovalRequest: {
    title: 'New Training Approval Request',
    message: '{employeeName} requested approval for {programName}'
  },
  reminderPendingApprovals: {
    title: 'Pending Approvals Reminder',
    message: 'You have {count} pending training approval requests'
  }
};
```

---

## 🎨 Styling Recommendations

### Status Color Scheme

```css
/* Tailwind classes or custom CSS */
.status-pending-approval {
  background: #FEF3C7; /* yellow-100 */
  color: #92400E; /* yellow-800 */
}

.status-confirmed {
  background: #D1FAE5; /* green-100 */
  color: #065F46; /* green-800 */
}

.status-rejected {
  background: #FEE2E2; /* red-100 */
  color: #991B1B; /* red-800 */
}

.status-waitlisted {
  background: #DBEAFE; /* blue-100 */
  color: #1E40AF; /* blue-800 */
}

.status-cancelled {
  background: #F3F4F6; /* gray-100 */
  color: #374151; /* gray-800 */
}

.status-completed {
  background: #EDE9FE; /* purple-100 */
  color: #5B21B6; /* purple-800 */
}
```

---

## ✅ Testing Checklist

### Frontend Testing Tasks

- [ ] Employee can create booking
- [ ] Status shows "Pending Approval" correctly
- [ ] Manager sees pending approvals badge
- [ ] Manager dashboard loads correctly
- [ ] Approve button works and updates UI
- [ ] Reject button opens modal
- [ ] Reject with reason submits correctly
- [ ] Status badges display correct colors
- [ ] Employee sees updated status after approval/rejection
- [ ] Rejection reason displays to employee
- [ ] Empty state shows when no pending approvals
- [ ] Loading states display properly
- [ ] Error handling works for failed API calls
- [ ] Real-time polling updates badge count

---

## 🐛 Error Handling

### Common Error Scenarios

```javascript
// Error handling example
const handleApproveBooking = async (bookingId, managerId) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/bookings/${bookingId}/confirm?approvedById=${managerId}`,
      { method: 'POST' }
    );
    
    if (!response.ok) {
      const error = await response.json();
      
      // Handle specific error cases
      switch (response.status) {
        case 400:
          if (error.message.includes('manager')) {
            throw new Error('You are not authorized to approve this booking');
          } else if (error.message.includes('seats')) {
            throw new Error('No seats available for this session');
          } else {
            throw new Error(error.message);
          }
        case 404:
          throw new Error('Booking not found');
        default:
          throw new Error('Failed to approve booking. Please try again.');
      }
    }
    
    return await response.json();
  } catch (error) {
    console.error('Approval error:', error);
    throw error;
  }
};
```

---

## 📊 Sample Data for Testing

```javascript
// Mock data for development
const mockPendingApprovals = [
  {
    id: 1,
    bookingReference: "BK-A3F8B2C1",
    userId: 5,
    userName: "John Doe",
    userEmail: "john.doe@company.com",
    sessionId: 10,
    programName: "Advanced Java Programming",
    sessionStartDateTime: "2025-12-15T09:00:00",
    sessionEndDateTime: "2025-12-15T17:00:00",
    sessionLocationName: "Bangalore Office",
    status: "PENDING_APPROVAL",
    bookingDate: "2025-12-02T10:30:00",
    notes: "Looking forward to this training"
  },
  // Add more mock data as needed
];
```

---

## 🚀 Deployment Checklist

Before deploying to production:

- [ ] Update API_BASE_URL to production URL
- [ ] Test all API endpoints with production backend
- [ ] Verify user permissions and roles
- [ ] Test with real user data
- [ ] Ensure error messages are user-friendly
- [ ] Add loading indicators for all async operations
- [ ] Implement proper error boundaries
- [ ] Add analytics tracking for approval actions
- [ ] Test responsive design on mobile devices
- [ ] Verify accessibility (ARIA labels, keyboard navigation)

---

## 📞 Support

For backend API questions, refer to:
- `MANAGER_APPROVAL_WORKFLOW.md` - Complete backend documentation
- `QUICK_REFERENCE.md` - API quick reference
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🎓 Summary for Frontend Team

### What Changed in Backend:
1. New booking status: `PENDING_APPROVAL`
2. Bookings with manager require approval
3. Manager can approve or reject with reason
4. New API endpoints for approval workflow

### What Frontend Needs to Build:
1. **Manager Dashboard** - View and act on pending approvals
2. **Status Badges** - Visual indicators for booking status
3. **Approval Modal** - Confirm approval/rejection with reason
4. **Enhanced Booking View** - Show approval status to employees
5. **Navigation Badge** - Alert managers of pending count

### Key Integration Points:
- Check if user is manager (has direct reports)
- Handle `PENDING_APPROVAL` status in booking creation
- Display appropriate messages based on booking status
- Implement approval/rejection actions for managers
- Show rejection reasons to employees

**The backend is ready and waiting for your frontend integration!** 🚀

