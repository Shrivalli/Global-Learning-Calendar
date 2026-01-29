# Frontend Integration - Session Duration & Total Training Hours

## Quick Start

The BookingDTO now includes `sessionDurationHours` field that automatically calculates the duration of each training session.

## API Response Example

```json
GET /api/bookings/user/123/completed

{
  "status": "success",
  "data": [
    {
      "id": 1,
      "bookingReference": "BK-2025-001",
      "userId": 123,
      "userName": "John Doe",
      "userEmail": "john.doe@company.com",
      "sessionId": 45,
      "sessionCode": "JAVA-101-2025-Q1",
      "programName": "Java Programming Basics",
      "programCode": "JAVA-101",
      "sessionStartDateTime": "2025-11-15T09:00:00",
      "sessionEndDateTime": "2025-11-15T17:00:00",
      "sessionDurationHours": 8.0,
      "sessionLocationName": "Training Center A",
      "status": "COMPLETED",
      "completionStatus": "COMPLETED",
      "feedbackRating": 5,
      "createdAt": "2025-10-20T10:30:00",
      "updatedAt": "2025-11-15T17:15:00"
    },
    {
      "id": 2,
      "bookingReference": "BK-2025-015",
      "userId": 123,
      "userName": "John Doe",
      "sessionDurationHours": 4.0,
      "status": "COMPLETED",
      ...
    }
  ]
}
```

## Frontend Implementation Examples

### React/TypeScript Example

```typescript
// Define the Booking interface
interface Booking {
  id: number;
  bookingReference: string;
  programName: string;
  sessionStartDateTime: string;
  sessionEndDateTime: string;
  sessionDurationHours: number;
  status: string;
  // ... other fields
}

// Component to display total training hours
const TrainingHoursDashboard: React.FC<{ userId: number }> = ({ userId }) => {
  const [completedBookings, setCompletedBookings] = useState<Booking[]>([]);
  const [totalHours, setTotalHours] = useState(0);

  useEffect(() => {
    // Fetch completed bookings
    fetch(`/api/bookings/user/${userId}/completed`)
      .then(res => res.json())
      .then(data => {
        setCompletedBookings(data.data);
        
        // Calculate total training hours
        const total = data.data.reduce((sum: number, booking: Booking) => {
          return sum + (booking.sessionDurationHours || 0);
        }, 0);
        
        setTotalHours(total);
      });
  }, [userId]);

  return (
    <div className="training-dashboard">
      <h2>Training Statistics</h2>
      <div className="stat-card">
        <h1>{totalHours.toFixed(1)} hours</h1>
        <p>Total Training Completed</p>
      </div>
      <div className="stat-card">
        <h1>{completedBookings.length}</h1>
        <p>Sessions Completed</p>
      </div>
      <div className="stat-card">
        <h1>{(totalHours / completedBookings.length).toFixed(1)} hours</h1>
        <p>Average Session Duration</p>
      </div>
    </div>
  );
};
```

### Angular Example

```typescript
// training-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { BookingService } from './booking.service';

interface Booking {
  id: number;
  programName: string;
  sessionDurationHours: number;
  status: string;
}

@Component({
  selector: 'app-training-dashboard',
  templateUrl: './training-dashboard.component.html'
})
export class TrainingDashboardComponent implements OnInit {
  completedBookings: Booking[] = [];
  totalTrainingHours = 0;
  averageSessionDuration = 0;

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    const userId = 123; // Get from auth service
    
    this.bookingService.getCompletedBookings(userId).subscribe(response => {
      this.completedBookings = response.data;
      
      // Calculate total hours
      this.totalTrainingHours = this.completedBookings.reduce(
        (sum, booking) => sum + (booking.sessionDurationHours || 0),
        0
      );
      
      // Calculate average
      if (this.completedBookings.length > 0) {
        this.averageSessionDuration = this.totalTrainingHours / this.completedBookings.length;
      }
    });
  }
}
```

```html
<!-- training-dashboard.component.html -->
<div class="training-dashboard">
  <h2>Training Statistics</h2>
  
  <div class="stats-container">
    <div class="stat-card">
      <h1>{{ totalTrainingHours | number:'1.1-1' }}</h1>
      <p>Total Hours Completed</p>
    </div>
    
    <div class="stat-card">
      <h1>{{ completedBookings.length }}</h1>
      <p>Sessions Completed</p>
    </div>
    
    <div class="stat-card">
      <h1>{{ averageSessionDuration | number:'1.1-1' }}</h1>
      <p>Avg Session Duration</p>
    </div>
  </div>

  <div class="bookings-list">
    <h3>Completed Training Sessions</h3>
    <div *ngFor="let booking of completedBookings" class="booking-item">
      <h4>{{ booking.programName }}</h4>
      <p>Duration: {{ booking.sessionDurationHours }} hours</p>
    </div>
  </div>
</div>
```

### Vue.js Example

```vue
<template>
  <div class="training-dashboard">
    <h2>Training Statistics</h2>
    
    <div class="stats-grid">
      <div class="stat-card">
        <h1>{{ totalHours.toFixed(1) }}</h1>
        <p>Total Hours</p>
      </div>
      <div class="stat-card">
        <h1>{{ completedSessions }}</h1>
        <p>Completed Sessions</p>
      </div>
      <div class="stat-card">
        <h1>{{ averageDuration.toFixed(1) }}</h1>
        <p>Avg Duration</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TrainingDashboard',
  data() {
    return {
      bookings: []
    }
  },
  computed: {
    totalHours() {
      return this.bookings.reduce((sum, booking) => {
        return sum + (booking.sessionDurationHours || 0);
      }, 0);
    },
    completedSessions() {
      return this.bookings.length;
    },
    averageDuration() {
      return this.completedSessions > 0 
        ? this.totalHours / this.completedSessions 
        : 0;
    }
  },
  async mounted() {
    const userId = this.$route.params.userId;
    const response = await fetch(`/api/bookings/user/${userId}/completed`);
    const data = await response.json();
    this.bookings = data.data;
  }
}
</script>
```

### Plain JavaScript Example

```javascript
// Fetch and display training hours
async function displayTrainingHours(userId) {
  try {
    const response = await fetch(`/api/bookings/user/${userId}/completed`);
    const data = await response.json();
    const bookings = data.data;
    
    // Calculate total hours
    const totalHours = bookings.reduce((sum, booking) => {
      return sum + (booking.sessionDurationHours || 0);
    }, 0);
    
    // Calculate average
    const avgHours = bookings.length > 0 ? totalHours / bookings.length : 0;
    
    // Update DOM
    document.getElementById('total-hours').textContent = totalHours.toFixed(1);
    document.getElementById('completed-sessions').textContent = bookings.length;
    document.getElementById('avg-duration').textContent = avgHours.toFixed(1);
    
    // Display individual bookings
    const listContainer = document.getElementById('bookings-list');
    bookings.forEach(booking => {
      const item = document.createElement('div');
      item.className = 'booking-item';
      item.innerHTML = `
        <h4>${booking.programName}</h4>
        <p>Duration: ${booking.sessionDurationHours} hours</p>
        <p>Date: ${new Date(booking.sessionStartDateTime).toLocaleDateString()}</p>
      `;
      listContainer.appendChild(item);
    });
    
  } catch (error) {
    console.error('Error fetching training hours:', error);
  }
}

// Call the function
displayTrainingHours(123);
```

## API Endpoints

### Get User's Completed Bookings
```
GET /api/bookings/user/{userId}/completed
```

Returns all completed bookings with `sessionDurationHours` included.

### Get All User Bookings
```
GET /api/bookings/user/{userId}
```

Returns all bookings (including upcoming, confirmed, cancelled) with duration.

### Get Upcoming Bookings
```
GET /api/bookings/user/{userId}/upcoming
```

Returns upcoming bookings with scheduled duration.

## Helper Functions

### Format Duration
```javascript
// Format hours to human-readable format
function formatDuration(hours) {
  if (!hours) return 'N/A';
  
  const fullHours = Math.floor(hours);
  const minutes = Math.round((hours - fullHours) * 60);
  
  if (minutes === 0) {
    return `${fullHours}h`;
  }
  return `${fullHours}h ${minutes}m`;
}

// Usage
formatDuration(8.0);    // "8h"
formatDuration(2.5);    // "2h 30m"
formatDuration(1.75);   // "1h 45m"
```

### Calculate Monthly Training Hours
```javascript
function getMonthlyTrainingHours(bookings, month, year) {
  return bookings
    .filter(booking => {
      const date = new Date(booking.sessionStartDateTime);
      return date.getMonth() === month && date.getFullYear() === year;
    })
    .reduce((sum, booking) => sum + (booking.sessionDurationHours || 0), 0);
}

// Usage
const january2025Hours = getMonthlyTrainingHours(bookings, 0, 2025);
```

### Group Training Hours by Program
```javascript
function getHoursByProgram(bookings) {
  return bookings.reduce((acc, booking) => {
    const program = booking.programName;
    if (!acc[program]) {
      acc[program] = {
        totalHours: 0,
        sessions: 0
      };
    }
    acc[program].totalHours += booking.sessionDurationHours || 0;
    acc[program].sessions += 1;
    return acc;
  }, {});
}

// Usage
const programStats = getHoursByProgram(bookings);
console.log(programStats);
// Output:
// {
//   "Java Programming": { totalHours: 16.0, sessions: 2 },
//   "Leadership Training": { totalHours: 8.0, sessions: 1 }
// }
```

## Chart.js Example

```javascript
// Display training hours by month in a bar chart
async function renderTrainingChart(userId) {
  const response = await fetch(`/api/bookings/user/${userId}/completed`);
  const data = await response.json();
  const bookings = data.data;
  
  // Group by month
  const monthlyHours = {};
  bookings.forEach(booking => {
    const date = new Date(booking.sessionStartDateTime);
    const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
    monthlyHours[monthKey] = (monthlyHours[monthKey] || 0) + booking.sessionDurationHours;
  });
  
  // Prepare chart data
  const ctx = document.getElementById('trainingChart').getContext('2d');
  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: Object.keys(monthlyHours),
      datasets: [{
        label: 'Training Hours',
        data: Object.values(monthlyHours),
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    },
    options: {
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: 'Hours'
          }
        }
      }
    }
  });
}
```

## CSS Styling Example

```css
.training-dashboard {
  padding: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.stat-card h1 {
  font-size: 2.5rem;
  margin: 0 0 10px 0;
}

.stat-card p {
  font-size: 0.9rem;
  opacity: 0.9;
  margin: 0;
}

.booking-item {
  background: #f8f9fa;
  padding: 15px;
  margin: 10px 0;
  border-radius: 5px;
  border-left: 4px solid #667eea;
}

.booking-item h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.booking-item p {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
}
```

## Testing the Feature

### Test in Browser Console
```javascript
// Quick test
fetch('/api/bookings/user/123/completed')
  .then(r => r.json())
  .then(data => {
    const total = data.data.reduce((sum, b) => sum + b.sessionDurationHours, 0);
    console.log('Total training hours:', total);
  });
```

### Expected Response Structure
Every booking object will now include:
```json
{
  "sessionDurationHours": 8.0  // or 2.5, 4.0, etc.
}
```

## Common Issues

### Issue: sessionDurationHours is undefined
**Solution**: Ensure you're using the latest API version after the feature deployment

### Issue: Duration shows as 0
**Check**: Verify that the learning session has both start and end times set

### Issue: Need to show duration in minutes
**Solution**: Multiply by 60
```javascript
const minutes = booking.sessionDurationHours * 60;
```

---

**Feature Available**: December 2, 2025
**API Version**: All booking endpoints
**Backward Compatible**: Yes

