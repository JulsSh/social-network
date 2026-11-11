# Account & User API Contracts

## Overview

This service handles user account information, profile management, and user details.

## Endpoints

### 1. GET `/account/me` - Get Current User Profile

**Response:**
```json
{
  "data": {                         // ⚠️ May be wrapped in data object
    "id": 123,                      // ✅ Required - number
    "firstName": "Ivan",            // ✅ Required - string
    "lastName": "Petrov",           // ✅ Required - string
    "email": "ivan@example.com",    // ✅ Required - string
    "phone": "+79001234567",        // ⚠️ Nullable - string
    "photo": "/avatar.jpg",         // ⚠️ Nullable - string
    "birthDate": "1990-01-15",      // ⚠️ Nullable - string (ISO date)
    "city": "Moscow",               // ⚠️ Nullable - string
    "country": "Russia",            // ⚠️ Nullable - string
    "about": "About me text",       // ⚠️ Nullable - string
    "isBlocked": false,             // ✅ Required - boolean
    "isDeleted": false,             // ✅ Required - boolean
    "regDate": 1234567890,          // ✅ Required - timestamp
    "lastOnlineTime": 1234567890    // ⚠️ Nullable - timestamp
  }
}
```

### 2. GET `/account/{id}` - Get User Profile by ID

**Path Parameter:**
- `id` (number) - User ID to fetch

**Response:**
```json
{
  "id": 456,                        // ✅ Required - number
  "firstName": "Maria",             // ✅ Required - string
  "lastName": "Ivanova",            // ✅ Required - string
  "photo": "/avatar.jpg",           // ⚠️ Nullable - string
  "birthDate": "1992-05-20",        // ⚠️ Nullable - string (ISO date)
  "city": "Saint Petersburg",       // ⚠️ Nullable - string
  "country": "Russia",              // ⚠️ Nullable - string
  "about": "About me",              // ⚠️ Nullable - string
  "isOnline": true,                 // ⚠️ Nullable - boolean
  "isBlocked": false,               // ⚠️ Nullable - boolean
  "isDeleted": false,               // ⚠️ Nullable - boolean
  "lastOnlineTime": 1234567890      // ⚠️ Nullable - timestamp
}
```

**Note:** Returns less information than `/account/me` (no email, phone for privacy)

### 3. PUT `/account/me` - Update Current User Profile

**Request Body:**
```json
{
  "firstName": "Ivan",              // ⚠️ Optional - string
  "lastName": "Petrov",             // ⚠️ Optional - string
  "phone": "+79001234567",          // ⚠️ Optional - string
  "photo": "/avatar.jpg",           // ⚠️ Optional - string
  "birthDate": "1990-01-15",        // ⚠️ Optional - string (ISO date)
  "city": "Moscow",                 // ⚠️ Optional - string
  "country": "Russia",              // ⚠️ Optional - string
  "about": "Updated about me"       // ⚠️ Optional - string
}
```

**Response:** 200 OK with updated user object

### 4. DELETE `/account/me` - Delete Current User Account

**Response:** 200 OK

**Note:** Soft delete - sets `isDeleted: true`

## Data Structures

### User Profile (Full)

**Used in:** Current user profile (`/account/me`)

```json
{
  "id": 123,                        // ✅ Required - number
  "firstName": "Ivan",              // ✅ Required - string
  "lastName": "Petrov",             // ✅ Required - string
  "email": "ivan@example.com",      // ✅ Required - string (only for own profile)
  "phone": "+79001234567",          // ⚠️ Nullable - string (only for own profile)
  "photo": "/avatar.jpg",           // ⚠️ Nullable - string
  "birthDate": "1990-01-15",        // ⚠️ Nullable - string (ISO date)
  "city": "Moscow",                 // ⚠️ Nullable - string
  "country": "Russia",              // ⚠️ Nullable - string
  "about": "About me text",         // ⚠️ Nullable - string
  "isBlocked": false,               // ✅ Required - boolean
  "isDeleted": false,               // ✅ Required - boolean
  "isOnline": true,                 // ⚠️ Nullable - boolean
  "regDate": 1234567890,            // ✅ Required - timestamp
  "lastOnlineTime": 1234567890,     // ⚠️ Nullable - timestamp
  
  // Computed by frontend (not from API)
  "fullName": "Ivan Petrov",        // firstName + lastName
  "ages": 34                        // Calculated from birthDate
}
```

### User Profile (Public)

**Used in:** Other user profiles (`/account/{id}`), author cache, search results

```json
{
  "id": 456,                        // ✅ Required - number
  "firstName": "Maria",             // ✅ Required - string
  "lastName": "Ivanova",            // ✅ Required - string
  "photo": "/avatar.jpg",           // ⚠️ Nullable - string
  "birthDate": "1992-05-20",        // ⚠️ Nullable - string (ISO date)
  "city": "Saint Petersburg",       // ⚠️ Nullable - string
  "country": "Russia",              // ⚠️ Nullable - string
  "about": "About me",              // ⚠️ Nullable - string
  "isOnline": true,                 // ⚠️ Nullable - boolean
  "isBlocked": false,               // ⚠️ Nullable - boolean
  "isDeleted": false,               // ⚠️ Nullable - boolean
  "lastOnlineTime": 1234567890,     // ⚠️ Nullable - timestamp
  
  // Computed by frontend
  "fullName": "Maria Ivanova",
  "ages": 32
}
```

### Author Object (Minimal)

**Used in:** Posts, comments, cached authors

```json
{
  "id": 789,                        // ✅ Required - number
  "firstName": "Alex",              // ✅ Required - string
  "lastName": "Smirnov",            // ✅ Required - string
  "photo": "/avatar.jpg"            // ⚠️ Nullable - string
}
```

## Frontend State Management

### Profile Info Store State

```javascript
// src/store/profile/info.js
state: {
  info: null                        // Current user profile object
}

// Getter with computed fields
getInfo(state) {
  if (!state.info) return;
  
  let result = { ...state.info };
  result.fullName = result.firstName + ' ' + result.lastName;
  result.ages = moment().diff(result.birthDate, 'years');
  return result;
}
```

### Users Info Store State

```javascript
// src/store/users/info.js
state: {
  info: null,                       // Current user profile
  users: null,                      // Viewed user profile
  wall: [],                         // User's wall posts
  wallPagination: { 
    total: 3, 
    page: 1, 
    size: 5 
  }
}

// Getter with computed fields
getUsersInfo(state) {
  if (!state.users) return;
  
  let result = { ...state.users };
  result.fullName = result.firstName + ' ' + result.lastName;
  result.ages = moment().diff(result.birthDate, 'years');
  return result;
}
```

### Global Authors Cache

```javascript
// src/store/global/authors.js
state: {
  authors: []                       // ✅ Array - Cached author objects (max 20)
}

// Action: addAuthor (deduplication + limit)
addAuthor: ({ state, commit }, author) => {
  // Check if already exists
  if (state.authors.find((a) => a.id === author.id)) return;
  
  // Add if under limit
  if (state.authors.length <= 20) {
    commit('addAuthor', author);
  } else {
    // Reset cache if exceeded
    commit('reloadAuthors', author);
  }
}
```

## Important Notes

### Current User vs Other Users
- `/account/me` - Returns private data (email, phone)
- `/account/{id}` - Returns public data only
- Privacy protection for other users

### Computed Fields
- `fullName` = `firstName + " " + lastName`
- `ages` = calculated from `birthDate` using moment/dayjs
- Computed in getters, not stored in state
- Always recalculated on access

### Author Caching
- Global cache stores up to 20 authors
- Prevents duplicate API calls for same author
- Used across posts, comments, notifications
- Resets if cache exceeds limit
- Minimal data: id, firstName, lastName, photo

### Age Calculation
```javascript
import moment from 'moment';
const ages = moment().diff(birthDate, 'years');
```

### Online Status
- `isOnline` boolean flag
- `lastOnlineTime` timestamp of last activity
- Can calculate: online if `now - lastOnlineTime < 60 seconds`

### Profile Photos
- `photo` field contains URL or path
- Can be null if no photo uploaded
- Show placeholder/default avatar if null
- Usually served from CDN or static storage

### Blocked/Deleted Users
- `isBlocked` - User blocked by admin
- `isDeleted` - User soft-deleted account
- Hidden from search results by default
- Admin pages may show these users

## Usage Examples

### Get Current User Profile
```javascript
// Request
const response = await axios.get('account/me');

// Handle wrapped response
const userProfile = response.data.data || response.data;

// Store
commit('setInfo', userProfile);

// Get with computed fields
const profile = getters.getInfo;
// { id: 123, fullName: "Ivan Petrov", ages: 34, ... }
```

### Get Other User Profile
```javascript
// Request
const response = await axios.get(`account/456`);
const userProfile = response.data;

// Store
commit('setUsersInfo', userProfile);

// Get with computed fields
const profile = getters.getUsersInfo;
// { id: 456, fullName: "Maria Ivanova", ages: 32, ... }
```

### Update Profile
```javascript
// Updated fields
const updates = {
  firstName: 'Ivan',
  lastName: 'Petrov',
  phone: '+79001234567',
  birthDate: '1990-01-15',
  city: 'Moscow',
  country: 'Russia',
  about: 'Software developer from Moscow'
};

// Request
await axios.put('account/me', updates);

// Refresh profile
const response = await axios.get('account/me');
commit('setInfo', response.data.data);
```

### Get Author for Post (with Cache)
```javascript
// Using getAuthor utility
import getAuthor from '@/utils/getAuthor';

// Check cache first
const author = await getAuthor(
  authorId,           // User ID
  cachedAuthors,      // Array of cached authors from store
  dispatch            // To add to cache if not found
);

// getAuthor implementation:
export default async (id, authors, dispatch) => {
  // Check cache
  let author = authors.find((a) => a.id === id);
  
  if (!author) {
    // Not in cache, fetch from API
    const response = await axios.get(`account/${id}`);
    author = response.data;
    
    // Add to cache
    dispatch('global/authors/addAuthor', author, { root: true });
  }
  
  return author;
};
```

### Delete Account
```javascript
// Request
await axios.delete('account/me');

// Clear session
commit('clearInfo');
router.push({ name: 'Login' });
```

### Check Online Status
```javascript
// Calculate online status
const isUserOnline = (user) => {
  if (user.isOnline) return true;
  
  if (user.lastOnlineTime) {
    const now = moment();
    const lastSeen = moment(user.lastOnlineTime);
    const diffSeconds = now.diff(lastSeen, 'seconds');
    return diffSeconds <= 60;  // Online if seen in last minute
  }
  
  return false;
};

// Usage
const online = isUserOnline(user);
```

### Display User Age
```javascript
// Component computed property
const userAge = computed(() => {
  if (!user.value.birthDate) return null;
  return moment().diff(user.value.birthDate, 'years');
});

// Template
<span v-if="userAge">{{ userAge }} лет</span>
```

## Component Integration

### Profile Page (Own)
- Display all fields (including email, phone)
- Edit button opens edit form
- Settings link to privacy settings
- Delete account button with confirmation

### Profile Page (Other User)
- Display public fields only
- Friend status and actions
- Send message button
- View wall posts

### Profile Edit Form
- Input fields for all editable fields
- Photo upload component
- Date picker for birthDate
- Location selectors (country, city)
- Save button
- Cancel button

### User Card (Compact)
- Avatar
- Full name
- Location (city, country)
- Online indicator
- Click to view full profile

### Author Display
- Avatar (small)
- Full name
- Link to profile
- Used in posts, comments, notifications

## Validation

### Required Fields
- `firstName` - Must not be empty
- `lastName` - Must not be empty
- `email` - Must be valid email format

### Optional Fields
- All other fields can be null/empty
- Graceful handling of missing data
- Show placeholders if fields empty

### Date Format
- `birthDate` in ISO format: "YYYY-MM-DD"
- Validate: must be in past
- Validate: user must be at least 13 years old

### Phone Format
- International format with country code
- Example: "+79001234567"
- Validate format before submitting
