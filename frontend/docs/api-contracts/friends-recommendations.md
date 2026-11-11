# Friends & Recommendations API Contracts

## Overview

This service handles friend relationships, friend requests, recommendations, blocking, and subscriptions.

## Endpoints

### 1. GET `/friends` - Get Friends List

**Query Parameters:**
- `statusCode` (string) - Filter by relationship status
  - `FRIEND` - Confirmed friends
  - `REQUEST_TO` - Outgoing friend requests
  - `REQUEST_FROM` - Incoming friend requests
  - `BLOCKED` - Blocked users
  - `SUBSCRIBED` - Subscribed users
  - `WATCHING` - Users watching you
- `size` (number, default: 3) - Items per page
- `page` (number, default: 0) - Page number

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of User objects
  "totalPages": 5,      // ✅ Required - number
  "totalElements": 42,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 3             // ✅ Required - items per page
}
```

### 2. GET `/friends/request` - Get Friend Requests

**Response Structure:**
```json
[]                      // 🔄 Array - Direct array of User objects (no pagination wrapper)
```

### 3. GET `/friends/recommendations` - Get Friend Recommendations

**Response Structure:**
```json
[]                      // 🔄 Array - Array of Recommendation objects
```

### 4. GET `/friends/count` - Get Incoming Friend Requests Count

**Response:**
```json
{
  "count": 3            // ✅ Required - number
}
```

**Usage:** Badge on friends menu to show pending requests

### 5. POST `/friends/{id}/request` - Send Friend Request

**Path Parameter:**
- `id` (number) - User ID to send request to

**Response:**
```json
{
  "message": "success"  // ✅ Required - string
}
```

**Possible response messages:**
- `"success"` - Request sent successfully
- `"request already sent!"` - Already sent request to this user
- `"Blocked!"` - Target user has blocked you

### 6. PUT `/friends/{id}/approve` - Approve Friend Request

**Path Parameter:**
- `id` (number) - User ID to approve

**Response:** 200 OK

### 7. DELETE `/friends/{id}` - Remove Friend

**Path Parameter:**
- `id` (number) - User ID to unfriend

**Response:** 200 OK

### 8. PUT `/friends/block/{id}` - Block User

**Path Parameter:**
- `id` (number) - User ID to block

**Response:** 200 OK

### 9. PUT `/friends/unblock/{id}` - Unblock User

**Path Parameter:**
- `id` (number) - User ID to unblock

**Response:** 200 OK

### 10. POST `/friends/subscribe/{id}` - Subscribe to User

**Path Parameter:**
- `id` (number) - User ID to subscribe to

**Response:** 200 OK

### 11. GET `/account/search/statusCode` - Search Friends by Name

**Query Parameters:**
- `firstName` (string) - Search by first name
- `statusCode` (string) - Filter by relationship status

**Response:**
```json
[]                      // 🔄 Array - Array of User objects
```

## Data Structures

### User Object

**Used in:** Friends list, requests, search results

```json
{
  "id": 123,                    // ✅ Required - number
  "firstName": "Ivan",          // ✅ Required - string
  "lastName": "Petrov",         // ✅ Required - string
  "photo": "/avatar.jpg",       // ⚠️ Nullable - string
  "birthDate": "1990-01-15",    // ⚠️ Nullable - string (ISO date)
  "city": "Moscow",             // ⚠️ Nullable - string
  "country": "Russia",          // ⚠️ Nullable - string
  "statusCode": "FRIEND",       // ✅ Required - string (relationship status)
  "isOnline": true,             // ⚠️ Nullable - boolean
  "lastOnlineTime": 1234567890, // ⚠️ Nullable - timestamp
  "isBlocked": false,           // ⚠️ Nullable - boolean
  "isDeleted": false            // ⚠️ Nullable - boolean
}
```

### Recommendation Object

**Used in:** Friend recommendations API

```json
{
  "friendId": 456,              // ✅ Required - number (recommended user's ID)
  "rating": 8.5                 // ✅ Required - number (recommendation score 0-10)
}
```

**Note:** Frontend fetches full user details from `/account/search?ids=...` using the `friendId` values

## Frontend State Management

### Friends Store State

```javascript
state: {
  result: {
    friends: [],                // ✅ Array - Confirmed friends
    request: [],                // ✅ Array - Pending requests
    recommendations: []         // ✅ Array - Recommendation objects
  },
  friends: {                    // Organized by statusCode
    FRIEND: [],
    REQUEST_TO: [],
    REQUEST_FROM: [],
    BLOCKED: [],
    SUBSCRIBED: [],
    WATCHING: []
  },
  requestsCount: null,          // Badge count for incoming requests
  loadedCounts: {               // Pagination tracking per status
    REQUEST_FROM: 0,
    REQUEST_TO: 0,
    FRIEND: 0,
    BLOCKED: 0,
    SUBSCRIBED: 0,
    WATCHING: 0
  }
}
```

### Safe Array Handling

```javascript
// Mutation: setFriends
setFriends: (state, payload) => {
  const safePayload = Array.isArray(payload) ? payload : [];
  safePayload.forEach((user) => {
    const { statusCode } = user;
    if (!state.friends[statusCode]) {
      state.friends[statusCode] = [];
    }
    
    // Check for duplicates
    const safeFriends = Array.isArray(state.friends[statusCode]) 
      ? state.friends[statusCode] 
      : [];
    const isDouble = safeFriends.find((u) => u.id === user.id);
    if (!isDouble) {
      state.friends[statusCode].push(user);
    }
  });
}
```

## Important Notes

### Array Safety
- All `content` arrays **must always be arrays**, even if empty
- Friends requests and recommendations return direct arrays (no pagination wrapper)
- Frontend applies `Array.isArray()` checks before all array operations

### Status Codes
- `FRIEND` - Mutual friends
- `REQUEST_TO` - You sent request, waiting for approval
- `REQUEST_FROM` - They sent request, you can approve
- `BLOCKED` - You blocked them
- `REJECTING` - They blocked you (can't send requests)
- `SUBSCRIBED` - You subscribed to them
- `WATCHING` - They are watching you

### Duplicate Prevention
Frontend checks for existing users before adding:
```javascript
const isDouble = state.friends[statusCode].find(
  (currentUser) => currentUser.id === user.id
);
if (!isDouble) {
  state.friends[statusCode].push(user);
}
```

### Recommendation Flow
1. Fetch recommendations: `GET /friends/recommendations`
   - Returns: `[{ friendId: 123, rating: 8.5 }, ...]`
2. Extract friend IDs: `const ids = recommendations.map(r => r.friendId)`
3. Fetch user details: `GET /account/search?ids=123,456,789`
4. Merge rating with user data in frontend
5. Sort by rating (highest first)

### Error Messages
When adding friends, backend may return:
- `"request already sent!"` - Show alert, don't retry
- `"Blocked!"` - Show alert, user can't send request
- Success - Show success alert

## Usage Examples

### Get Friends by Status
```javascript
// Request
const query = `statusCode=FRIEND&size=6`;
const { data } = await axios.get(`friends?${query}`);

// Safe handling
const content = Array.isArray(data.content) ? data.content : [];
commit('setResult', { id: 'friends', value: content });
commit('setFriends', content);
```

### Send Friend Request
```javascript
// Check if already requested
if (statusCode === 'REQUEST_TO') {
  alert('Вы уже отправляли запрос этому пользователю!');
  return;
}

// Send request
const { data } = await axios.post(`friends/${userId}/request`);

if (data.message === 'request already sent!') {
  alert('Вы уже отправили запрос этому пользователю!');
} else if (data.message === 'Blocked!') {
  alert('Этот пользователь заблокировал Вас!');
} else {
  alert('Заявка отправлена');
}
```

### Get Recommendations with Details
```javascript
// 1. Get recommendations
const { data } = await axios.get('friends/recommendations');
const recommendations = Array.isArray(data) ? data : [];

// 2. Extract IDs
const ids = recommendations.map(r => r.friendId);

// 3. Fetch user details
const query = `ids=${ids.join(',')}&isDeleted=false&showFriends=false`;
const response = await axios.get(`account/search?${query}`);
const users = Array.isArray(response.data.content) 
  ? response.data.content 
  : [];

// 4. Merge and sort by rating
const enriched = users.map(user => ({
  ...user,
  rating: recommendations.find(r => r.friendId === user.id)?.rating || 0
}));
enriched.sort((a, b) => b.rating - a.rating);
```

### Check Incoming Requests Count
```javascript
// Get count for badge
const { data } = await axios.get('/friends/count');
const count = data.count || 0;
commit('setRequestsCount', count);
```
