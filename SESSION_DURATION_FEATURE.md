# Session Duration Feature - Implementation Guide

## Overview
Added `sessionDurationHours` field to `BookingDTO` to enable frontend to display total training hours completed by users.

## Changes Made

### 1. BookingDTO.java
**File**: `src/main/java/com/learning/globallearningcalendar/dto/BookingDTO.java`

**Added Field**:
```java
private Double sessionDurationHours;  // Calculated duration in hours
```

This field stores the calculated duration of a learning session in hours (with decimal precision).

### 2. BookingServiceImpl.java
**File**: `src/main/java/com/learning/globallearningcalendar/service/impl/BookingServiceImpl.java`

**Added Calculation Logic**:
```java
// Calculate session duration in hours
if (session.getStartDateTime() != null && session.getEndDateTime() != null) {
    long minutes = java.time.Duration.between(
        session.getStartDateTime(), 
        session.getEndDateTime()
    ).toMinutes();
    dto.setSessionDurationHours(minutes / 60.0);
}
```

## How It Works

1. **Automatic Calculation**: When a `Booking` entity is converted to `BookingDTO`, the system automatically calculates the duration
2. **Based on Session Times**: Uses `LearningSession.startDateTime` and `LearningSession.endDateTime`
3. **Returns Hours**: Duration is calculated in minutes and then converted to hours (decimal format)
4. **Null Safe**: Only calculates if both start and end times are present

## Usage Examples

### Example 1: Get User's Total Training Hours
```java
// Get all completed bookings for a user
List<BookingDTO> completedBookings = bookingService.getCompletedBookingsByUser(userId);

// Calculate total training hours
double totalHours = completedBookings.stream()
    .filter(b -> b.getSessionDurationHours() != null)
    .mapToDouble(BookingDTO::getSessionDurationHours)
    .sum();

System.out.println("Total training hours: " + totalHours);
```

### Example 2: Frontend API Response
When you call the booking endpoints, each booking will now include the duration:

```json
{
  "id": 1,
  "bookingReference": "BK-2025-001",
  "userId": 123,
  "userName": "John Doe",
  "sessionCode": "JAVA-101-2025-Q1",
  "programName": "Java Programming Basics",
  "sessionStartDateTime": "2025-12-10T09:00:00",
  "sessionEndDateTime": "2025-12-10T17:00:00",
  "sessionDurationHours": 8.0,
  "status": "COMPLETED",
  ...
}
```

### Example 3: Calculate Total Hours for Completed Sessions
```javascript
// Frontend JavaScript/TypeScript example
const completedBookings = await fetch('/api/bookings/user/123/completed').then(r => r.json());

const totalTrainingHours = completedBookings.data.reduce((sum, booking) => {
  return sum + (booking.sessionDurationHours || 0);
}, 0);

console.log(`Total training hours: ${totalTrainingHours.toFixed(2)} hours`);
```

## API Endpoints That Return Duration

All booking-related endpoints now include `sessionDurationHours`:

1. **GET** `/api/bookings` - Get all bookings
2. **GET** `/api/bookings/{id}` - Get booking by ID
3. **GET** `/api/bookings/user/{userId}` - Get bookings by user
4. **GET** `/api/bookings/user/{userId}/upcoming` - Get upcoming bookings
5. **GET** `/api/bookings/user/{userId}/completed` - Get completed bookings
6. **GET** `/api/bookings/session/{sessionId}` - Get bookings by session
7. **POST** `/api/bookings` - Create booking (returns booking with duration)

## Frontend Display Examples

### Display Individual Session Duration
```html
<div class="booking-card">
  <h3>{{ booking.programName }}</h3>
  <p>Duration: {{ booking.sessionDurationHours }} hours</p>
  <p>Date: {{ booking.sessionStartDateTime | date }}</p>
</div>
```

### Display Total Training Hours (Dashboard)
```html
<div class="training-stats">
  <h2>Training Statistics</h2>
  <div class="stat-card">
    <h3>{{ totalTrainingHours.toFixed(1) }}</h3>
    <p>Total Hours Completed</p>
  </div>
  <div class="stat-card">
    <h3>{{ completedSessions }}</h3>
    <p>Sessions Completed</p>
  </div>
  <div class="stat-card">
    <h3>{{ averageSessionDuration.toFixed(1) }}</h3>
    <p>Average Session Duration</p>
  </div>
</div>
```

## Data Format

- **Field Name**: `sessionDurationHours`
- **Type**: `Double`
- **Unit**: Hours
- **Precision**: Decimal (e.g., 2.5 hours = 2 hours 30 minutes)
- **Can be null**: Yes (if session times are not available)

## Duration Calculation Examples

| Start Time | End Time | Duration (Hours) |
|------------|----------|------------------|
| 09:00 AM | 05:00 PM | 8.0 |
| 10:00 AM | 12:30 PM | 2.5 |
| 01:00 PM | 02:45 PM | 1.75 |
| 09:00 AM | 06:00 PM | 9.0 |

## Common Use Cases

### 1. User Training Dashboard
Show total training hours completed by the user.

### 2. Compliance Tracking
Track if employees meet minimum required training hours.

### 3. Reporting
Generate reports showing training hours by:
- Department
- Month/Quarter/Year
- Training program
- Location

### 4. Manager View
Show total training hours for team members.

### 5. Training Analytics
Calculate average session duration, most popular training durations, etc.

## Testing

### Test Case 1: Standard 8-hour Training
```java
LearningSession session = LearningSession.builder()
    .startDateTime(LocalDateTime.of(2025, 12, 10, 9, 0))
    .endDateTime(LocalDateTime.of(2025, 12, 10, 17, 0))
    .build();

// Expected: sessionDurationHours = 8.0
```

### Test Case 2: Half-day Training
```java
LearningSession session = LearningSession.builder()
    .startDateTime(LocalDateTime.of(2025, 12, 10, 9, 0))
    .endDateTime(LocalDateTime.of(2025, 12, 10, 13, 0))
    .build();

// Expected: sessionDurationHours = 4.0
```

### Test Case 3: 90-minute Workshop
```java
LearningSession session = LearningSession.builder()
    .startDateTime(LocalDateTime.of(2025, 12, 10, 14, 0))
    .endDateTime(LocalDateTime.of(2025, 12, 10, 15, 30))
    .build();

// Expected: sessionDurationHours = 1.5
```

## SQL Query Example

To verify in database:
```sql
-- Get total training hours for a user (completed sessions only)
SELECT 
    u.employee_id,
    u.first_name,
    u.last_name,
    COUNT(b.id) as completed_sessions,
    SUM(TIMESTAMPDIFF(MINUTE, ls.start_date_time, ls.end_date_time) / 60.0) as total_hours
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN learning_sessions ls ON b.session_id = ls.id
WHERE b.status = 'COMPLETED'
GROUP BY u.id, u.employee_id, u.first_name, u.last_name
ORDER BY total_hours DESC;
```

## Future Enhancements

Potential additions:
1. **Duration Categories**: Short (<2h), Medium (2-4h), Long (>4h)
2. **Break Time**: Subtract lunch/break time from total duration
3. **Actual Duration**: Track actual attendance time vs scheduled time
4. **Learning Credits**: Convert hours to learning credits/points
5. **Certification Hours**: Track hours eligible for professional certifications

## Notes

- Duration is calculated based on **scheduled** session time, not actual attendance time
- If you need to track actual attendance duration, consider adding `actualDurationHours` field
- The field uses `Double` to allow fractional hours (e.g., 1.5 hours)
- Frontend should format the display (e.g., "2.5 hours" or "2h 30m")

## Troubleshooting

### Issue: sessionDurationHours is null
**Cause**: Session start or end time is missing
**Solution**: Ensure all learning sessions have both `startDateTime` and `endDateTime` populated

### Issue: Negative duration
**Cause**: End time is before start time
**Solution**: Validate session times when creating/updating sessions

### Issue: Unrealistic duration (e.g., 100+ hours)
**Cause**: Data entry error or date/time confusion
**Solution**: Add validation to ensure session duration is reasonable (e.g., < 24 hours)

---

**Implemented**: December 2, 2025
**Status**: ✅ Ready to use
**Backward Compatible**: Yes (new field, doesn't affect existing functionality)

