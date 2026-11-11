# Notifications API Contracts

## Overview

This service handles user notifications for various events (posts, comments, friend requests, messages, etc.).

## Endpoints

### 1. GET `/notifications/page` - Get Notifications

**Query Parameters:**
- `page` (number, default: 0) - Page number (0-indexed)
- `size` (number, default: 20) - Items per page
- `sort` (string, default: "sentTime,desc") - Sort by time sent

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Notification objects
  "totalPages": 3,      // ✅ Required - number
  "totalElements": 47,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 20            // ✅ Required - items per page
}
```

### 2. GET `/notifications/count` - Get Unread Notifications Count

**Response:**
```json
{
  "data": {
    "count": 5          // ✅ Required - number
  }
}
```

**Usage:** Badge notification in header/menu

### 3. PUT `/notifications/readed` - Mark All Notifications as Read

**Response:** 200 OK

**Note:** Marks all notifications as read, resets count to 0

### 4. GET `/notifications/settings` - Get Notification Settings

**Response:**
```json
{
  "POST": true,                     // ✅ Required - boolean
  "POST_COMMENT": true,             // ✅ Required - boolean
  "COMMENT_COMMENT": true,          // ✅ Required - boolean
  "FRIEND_REQUEST": true,           // ✅ Required - boolean
  "FRIEND_BIRTHDAY": true,          // ✅ Required - boolean
  "MESSAGE": true,                  // ✅ Required - boolean
  "FRIEND_APPROVE": true,           // ✅ Required - boolean
  "FRIEND_BLOCKED": false,          // ✅ Required - boolean
  "FRIEND_UNBLOCKED": true,         // ✅ Required - boolean
  "FRIEND_SUBSCRIBE": true,         // ✅ Required - boolean
  "USER_BIRTHDAY": true,            // ✅ Required - boolean
  "LIKE": true,                     // ✅ Required - boolean
  "SEND_EMAIL_MESSAGE": false       // ✅ Required - boolean
}
```

### 5. PUT `/notifications/settings` - Update Notification Settings

**Request Body:**
```json
{
  "POST": true,
  "POST_COMMENT": true,
  "COMMENT_COMMENT": true,
  "FRIEND_REQUEST": true,
  "FRIEND_BIRTHDAY": false,
  "MESSAGE": true,
  "FRIEND_APPROVE": true,
  "FRIEND_BLOCKED": false,
  "FRIEND_UNBLOCKED": true,
  "FRIEND_SUBSCRIBE": true,
  "USER_BIRTHDAY": false,
  "LIKE": true,
  "SEND_EMAIL_MESSAGE": false
}
```

**Response:** 200 OK

## Data Structures

### Notification Object

```json
{
  "id": 12345,                      // ✅ Required - number
  "notificationType": "POST",       // ✅ Required - string (see types below)
  "sentTime": 1234567890,           // ✅ Required - timestamp
  "data": {                         // ✅ Required - object (structure varies by type)
    "authorId": 789,                // ⚠️ Nullable - number (depends on type)
    "postId": 456,                  // ⚠️ Nullable - number (for post-related)
    "commentId": 123,               // ⚠️ Nullable - number (for comment-related)
    "messageText": "...",           // ⚠️ Nullable - string (for messages)
    
    // Enriched by frontend (not from API)
    "author": {}                    // ⚠️ Added by frontend - Author object
  },
  "isRead": false                   // ✅ Required - boolean
}
```

## Notification Types

### POST
**Trigger:** Friend publishes new post  
**Data fields:**
- `authorId` (number) - Who posted
- `postId` (number) - Post ID

**Display:** "{author} опубликовал новую запись"

### POST_COMMENT
**Trigger:** Someone comments on your post  
**Data fields:**
- `authorId` (number) - Who commented
- `postId` (number) - Post ID
- `commentId` (number) - Comment ID

**Display:** "{author} оставил комментарий"

### COMMENT_COMMENT
**Trigger:** Someone replies to your comment  
**Data fields:**
- `authorId` (number) - Who replied
- `postId` (number) - Post ID
- `commentId` (number) - Comment ID (the reply)
- `parentCommentId` (number) - Your comment ID

**Display:** "{author} ответил на ваш комментарий"

### FRIEND_REQUEST
**Trigger:** Someone sends you friend request  
**Data fields:**
- `authorId` (number) - Who sent request

**Display:** Special UI card with approve/reject buttons

### FRIEND_BIRTHDAY
**Trigger:** Friend's birthday today  
**Data fields:**
- `authorId` (number) - Friend's ID

**Display:** Birthday greeting card

### MESSAGE
**Trigger:** Receive new direct message  
**Data fields:**
- `authorId` (number) - Who sent message
- `dialogId` (number) - Dialog ID
- `messageText` (string) - Message preview

**Display:** "{author} прислал сообщение"

### FRIEND_APPROVE
**Trigger:** Someone accepted your friend request  
**Data fields:**
- `authorId` (number) - Who approved

**Display:** Special UI card

### FRIEND_BLOCKED
**Trigger:** Someone blocked you  
**Data fields:**
- `authorId` (number) - Who blocked

**Display:** Notification (rarely shown)

### FRIEND_UNBLOCKED
**Trigger:** Someone unblocked you  
**Data fields:**
- `authorId` (number) - Who unblocked

**Display:** Notification

### FRIEND_SUBSCRIBE
**Trigger:** Someone subscribed to you  
**Data fields:**
- `authorId` (number) - Who subscribed

**Display:** Notification

### USER_BIRTHDAY
**Trigger:** Your birthday today  
**Data fields:**
- (none)

**Display:** Birthday wishes

### LIKE
**Trigger:** Someone likes your post/comment  
**Data fields:**
- `authorId` (number) - Who liked
- `postId` (number) - Post ID
- `commentId` (number, optional) - Comment ID if comment was liked

**Display:** "{author} поставил лайк"

### SEND_EMAIL_MESSAGE
**Trigger:** Email notification preference  
**Data fields:**
- (configuration only)

**Display:** Not shown in UI, used for email delivery

## Frontend State Management

### Notifications Store State

```javascript
state: {
  lastNotifications: [],            // ✅ Array - Initial page of notifications
  nextNotifications: [],            // ✅ Array - Additional pages
  notificationsLength: 0,           // Unread count
  userInfo: {},                     // Cache of author details
  size: 0,                          // Offset for pagination
  allNotifications: {
    notifications: [],              // ✅ Array - Combined list
    totalPages: null,
    totalElements: null
  }
}
```

### Safe Array Handling

```javascript
// Action: fetchNotifications
async fetchNotifications({ commit, state }, payload = {}) {
  const { countPage = 0, direction = 'desc' } = payload;
  
  const response = await axios.get(
    `notifications/page?page=${countPage}&sort=sentTime,${direction}`
  );
  
  // Safe array extraction
  const notificationsList = Array.isArray(response.data.content)
    ? response.data.content
    : [];
  
  // Extract author IDs safely
  const authorIds = notificationsList
    .filter((n) => n.data && n.data.authorId)
    .map((n) => n.data.authorId);
  
  // Fetch author details
  const uniqueIds = Array.from(new Set(authorIds));
  const userInfoPromises = uniqueIds.map(async (authorId) => {
    try {
      const { data } = await axios.get(`/account/${authorId}`);
      return data;
    } catch (error) {
      console.error(`Failed to fetch author ${authorId}:`, error);
      return {};
    }
  });
  
  const userInfoResponses = await Promise.all(userInfoPromises);
  
  // Build author lookup map
  const userInfo = userInfoResponses.reduce((acc, user) => {
    if (user && user.id) {
      acc[user.id] = user;
    }
    return acc;
  }, {});
  
  commit('setUserInfo', userInfo);
  
  // Enrich notifications with author data
  const mergedNotifications = notificationsList.map((notification) => {
    const { authorId } = notification.data;
    if (authorId) {
      return {
        ...notification,
        data: {
          ...notification.data,
          author: userInfo[authorId] || {}
        }
      };
    }
    return notification;
  });
  
  // Store
  if (state.lastNotifications.length === 0) {
    commit('setLatsNotifications', {
      notifications: mergedNotifications,
      totalPages: response.data.totalPages,
      totalElements: response.data.totalElements
    });
  } else {
    commit('setNextNotifications', {
      notifications: mergedNotifications,
      totalPages: response.data.totalPages,
      totalElements: response.data.totalElements
    });
  }
}
```

## Important Notes

### Array Safety
- `content` field **must always be an array**, even if empty
- Frontend applies `Array.isArray()` checks before all array operations
- Empty array means no notifications

### Author Enrichment
- API returns only `authorId` in `data` object
- Frontend fetches author details in batch (all unique IDs)
- Uses `Promise.all()` for parallel fetching
- Caches author info in store
- Adds `author` object to notification data

### Pagination
- Default page size: 20 notifications
- Pages are 0-indexed
- First fetch: `lastNotifications`
- Subsequent fetches: `nextNotifications`
- Combined into `allNotifications.notifications`

### Notification Settings
- Per-type toggle for receiving notifications
- Email notifications separate from in-app
- Updated via PUT `/notifications/settings`
- Frontend syncs with backend state

### Read Status
- Individual notifications have `isRead` field
- Mark all as read: PUT `/notifications/readed`
- Unread count updates after marking read
- Badge displays total unread count

### Real-time Updates
- WebSocket for instant notification delivery
- New notifications prepended to list
- Increment unread count
- Play sound/show browser notification (if enabled)

## Usage Examples

### Get Notifications
```javascript
// Request (with defaults)
const response = await axios.get('notifications/page?page=0&sort=sentTime,desc');

// Safe handling
const notificationsList = Array.isArray(response.data.content)
  ? response.data.content
  : [];

// Extract unique author IDs
const authorIds = notificationsList
  .filter((n) => n.data && n.data.authorId)
  .map((n) => n.data.authorId);

const uniqueAuthorIds = Array.from(new Set(authorIds));

// Fetch authors in parallel
const authors = await Promise.all(
  uniqueAuthorIds.map(async (id) => {
    try {
      const { data } = await axios.get(`/account/${id}`);
      return data;
    } catch {
      return {};
    }
  })
);

// Build lookup map
const authorMap = authors.reduce((acc, author) => {
  if (author.id) acc[author.id] = author;
  return acc;
}, {});

// Enrich notifications
const enriched = notificationsList.map((notification) => {
  const authorId = notification.data?.authorId;
  if (authorId && authorMap[authorId]) {
    return {
      ...notification,
      data: {
        ...notification.data,
        author: authorMap[authorId]
      }
    };
  }
  return notification;
});

commit('setLatsNotifications', {
  notifications: enriched,
  totalPages: response.data.totalPages,
  totalElements: response.data.totalElements
});
```

### Get Unread Count
```javascript
// Request
const response = await axios.get('notifications/count');

// Update badge
const count = response.data?.data?.count || 0;
commit('setNotificationsLength', count);
```

### Mark All as Read
```javascript
// Request
await axios.put('/notifications/readed');

// Refresh notifications
await dispatch('fetchNotifications', { countPage: 0, direction: 'desc' });

// Update count
await dispatch('fetchNotificationsLength');
```

### Get Notification Settings
```javascript
// Request
const response = await axios.get('notifications/settings');

// Settings object
const settings = response.data;
// { POST: true, MESSAGE: true, LIKE: false, ... }
```

### Update Notification Settings
```javascript
// Request
const newSettings = {
  POST: true,
  POST_COMMENT: true,
  COMMENT_COMMENT: true,
  FRIEND_REQUEST: true,
  FRIEND_BIRTHDAY: false,      // Disabled
  MESSAGE: true,
  FRIEND_APPROVE: true,
  FRIEND_BLOCKED: false,
  FRIEND_UNBLOCKED: true,
  FRIEND_SUBSCRIBE: true,
  USER_BIRTHDAY: false,         // Disabled
  LIKE: true,
  SEND_EMAIL_MESSAGE: false     // Disabled
};

await axios.put('notifications/settings', newSettings);
```

### Display Notification
```javascript
// Get text for notification type
const getNotificationText = (type) => {
  switch (type) {
    case 'POST': return 'опубликовал новую запись';
    case 'POST_COMMENT': return 'оставил комментарий';
    case 'COMMENT_COMMENT': return 'ответил на ваш комментарий';
    case 'MESSAGE': return 'прислал сообщение';
    case 'LIKE': return 'поставил лайк';
    case 'FRIEND_REQUEST': return '';  // Special card
    case 'FRIEND_APPROVE': return '';  // Special card
    case 'FRIEND_BIRTHDAY': return ''; // Special card
    default: return '';
  }
};

// Render notification
const notification = {
  id: 123,
  notificationType: 'POST',
  sentTime: 1234567890,
  data: {
    authorId: 456,
    author: { firstName: 'Ivan', lastName: 'Petrov', photo: '/avatar.jpg' },
    postId: 789
  },
  isRead: false
};

const text = `${notification.data.author.firstName} ${notification.data.author.lastName} ${getNotificationText(notification.notificationType)}`;
// "Ivan Petrov опубликовал новую запись"
```

## Component Integration

### Notification List
- Display all notifications, newest first
- Show author avatar, name, action text, time
- Link to related post/comment/dialog
- Highlight unread notifications
- Mark as read when clicked

### Notification Badge
- Header/menu shows unread count
- Red badge with number
- Update on WebSocket message
- Clear when notifications page opened

### Notification Settings
- Toggle switches for each type
- Save button to persist changes
- Email notification separate section
- Preview notifications before enabling
