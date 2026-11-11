# Dialogs & Messages API Contracts

## Overview

This service handles direct messaging between users, dialog management, and message history.

## Endpoints

### 1. GET `/dialogs/recipientId/{id}` - Create or Get Dialog

**Path Parameter:**
- `id` (number) - Recipient user ID

**Response:**
```json
{
  "data": {                     // ⚠️ Nullable - Can be null if no dialog exists
    "id": 456,                  // ✅ Required - number (dialog ID)
    "recipientId": 789          // ✅ Required - number
  }
}
```

**Note:** Creates new dialog if none exists, returns existing if found

### 2. GET `/dialogs` - Get All Dialogs

**Query Parameters:**
- `page` (number, default: 0) - Page number
- `sort` (string, default: "unreadCount,desc") - Sort by unread count

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Dialog objects
  "totalPages": 2,      // ✅ Required - number
  "totalElements": 15,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page
  "size": 20            // ✅ Required - items per page
}
```

### 3. GET `/dialogs/messages` - Get Messages in Dialog

**Query Parameters:**
- `recipientId` (number) - Recipient user ID
- `page` (number, default: 0) - Page number
- `size` (number, default: 20) - Items per page
- `sort` (string) - Sort order
  - `time,desc` - Newest first (for last messages)
  - `time,asc` - Oldest first (for next/older messages)

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Message objects
  "totalPages": 5,      // ✅ Required - number
  "totalElements": 87,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page
  "size": 20,           // ✅ Required - items per page
  "unreadCount": 3      // ⚠️ Nullable - number (unread messages count)
}
```

### 4. GET `/dialogs/unread` - Get Total Unread Messages Count

**Response:**
```json
{
  "count": 12           // ✅ Required - number
}
```

**Usage:** Badge notification in header

### 5. PUT `/dialogs/{id}` - Mark Dialog as Read

**Path Parameter:**
- `id` (number) - Dialog ID to mark as read

**Response:** 200 OK

## Data Structures

### Dialog Object

```json
{
  "id": 456,                        // ✅ Required - number
  "recipientId": 789,               // ✅ Required - number
  "unreadCount": 3,                 // ✅ Required - number
  "lastMessage": {                  // ⚠️ Nullable - Message object
    "messageText": "Hello!",
    "time": 1234567890
  },
  
  // Enriched by frontend (not from API)
  "recipient": {}                   // ⚠️ Added by frontend - User object
}
```

### Message Object

```json
{
  "id": 12345,                      // ✅ Required - number
  "dialogId": 456,                  // ✅ Required - number
  "authorId": 789,                  // ✅ Required - number
  "recipientId": 123,               // ✅ Required - number
  "messageText": "Hello, world!",   // ✅ Required - string
  "time": 1234567890,               // ✅ Required - timestamp
  "readStatus": "READ",             // ✅ Required - string (READ | UNREAD)
  "conversationPartner1": 789,      // ✅ Required - number
  "conversationPartner2": 123       // ✅ Required - number
}
```

## Frontend State Management

### Dialogs Store State

```javascript
state: {
  dialogs: [],                      // ✅ Array - List of Dialog objects
  unreadedMessages: 0,              // Total unread count across all dialogs
  lastMessages: {
    messages: [],                   // ✅ Array - Most recent messages
    totalPages: null
  },
  nextMessages: [],                 // ✅ Array - Older messages (pagination)
  allMessages: [],                  // ✅ Array - Combined message history
  activeDialogId: null,             // Currently selected dialog
  dialogsLoaded: false,             // Initial load complete flag
  isHistoryEndReached: false        // No more older messages flag
}
```

### Safe Array Handling

```javascript
// Mutation: setDialogs
setDialogs: (state, dialogs) => {
  const safeDialogs = Array.isArray(dialogs) ? dialogs : [];
  state.dialogs = safeDialogs;
}

// Mutation: addLastMessages
addLastMessages: (state, { lastMessages, total }) => {
  const safeContent = Array.isArray(lastMessages.content) 
    ? lastMessages.content 
    : [];
  
  state.lastMessages.totalPages = lastMessages.totalPages;
  state.lastMessages.messages = safeContent;
  state.lastMessages.messages.sort((a, b) => a.time - b.time);
  state.allMessages = state.lastMessages.messages;
  state.totalLastMessages = total;
}

// Mutation: addNextMessages
addNextMessages: (state, { nextMessages, total }) => {
  const safeMessages = Array.isArray(nextMessages) ? nextMessages : [];
  
  state.nextMessages = safeMessages;
  state.nextMessages.sort((a, b) => a.time - b.time);
  state.allMessages = [...state.nextMessages, ...state.allMessages];
  state.totalNextMessages = total;
}
```

## Important Notes

### Array Safety
- All array fields **must be arrays**, even if empty
- `content` in responses **must be arrays**
- Frontend applies `Array.isArray()` checks before array operations

### Message Ordering
- **Last Messages** (`sort=time,desc`): Load newest messages first (initial load)
- **Next Messages** (`sort=time,asc`): Load older messages (pagination upward)
- Messages sorted by `time` field before displaying
- Combined into `allMessages` array for display

### Dialog Creation
- GET `/dialogs/recipientId/{id}` creates dialog if doesn't exist
- Returns dialog ID for subsequent message fetching
- Check `response.data` is not null before using

### Unread Tracking
- Each dialog has `unreadCount` field
- Global unread count from `/dialogs/unread`
- Mark as read when user opens dialog: `PUT /dialogs/{id}`
- Refresh dialog list after marking read

### Pagination Strategy
- **Initial load**: Get latest N messages with `sort=time,desc`
- **Load older**: Increment page, use `sort=time,asc`
- **Scroll detection**: Load more when scrolling to top
- **End reached**: Set flag when no more pages

### WebSocket Integration
- New messages arrive via WebSocket
- Append to `allMessages` array
- Update dialog's `lastMessage` field
- Increment `unreadCount` if not in active dialog

## Usage Examples

### Create or Get Dialog
```javascript
// Request
const response = await axios.get(`/dialogs/recipientId/789`);

// Safe handling
if (!response.data) {
  console.error('No dialog data returned');
  return;
}

const dialogData = response.data.data;
if (dialogData) {
  commit('selectDialog', dialogData.id);
}
```

### Get All Dialogs
```javascript
// Request
const response = await axios.get('/dialogs?page=0&sort=unreadCount,desc');

// Safe handling
const content = Array.isArray(response.data?.content) 
  ? response.data.content 
  : [];

if (content.length === 0) {
  console.log('No dialogs');
  return;
}

commit('setDialogs', content);
commit('dialogsLoaded');
```

### Load Latest Messages
```javascript
// Request (newest first)
const response = await axios.get(
  `dialogs/messages?recipientId=789&page=0&sort=time,desc`
);

// Safe handling
const content = Array.isArray(response.data.content) 
  ? response.data.content 
  : [];

if (content.length === 0) {
  console.log('No messages');
  return;
}

// Store and sort
commit('clearLastMessages');
commit('addLastMessages', {
  lastMessages: {
    content: content,
    totalPages: response.data.totalPages
  },
  total: response.unreadCount
});
```

### Load Older Messages (Pagination)
```javascript
// Request (oldest first for pagination)
const response = await axios.get(
  `dialogs/messages?recipientId=789&page=${nextPage}&sort=time,asc`
);

// Safe handling
const content = Array.isArray(response.data.content) 
  ? response.data.content 
  : [];

if (content.length === 0) {
  commit('markEndOfHistory');
  return;
}

// Prepend older messages
commit('addNextMessages', {
  nextMessages: content,
  total: response.unreadCount
});
```

### Get Unread Count
```javascript
// Request
const { data } = await axios.get('/dialogs/unread');

// Update badge
const count = data?.count || 0;
commit('setUnreadedMessages', count);

// Also fetch dialog list
await dispatch('fetchDialogs');
```

### Mark Dialog as Read
```javascript
// Request
await axios.put(`dialogs/${dialogId}`);

// Refresh dialogs to update unread counts
await dispatch('fetchDialogs');
await dispatch('apiUnreadedMessages');
```

### Send Message (WebSocket)
```javascript
// WebSocket emit (not REST API)
socket.emit('send-message', {
  dialogId: 456,
  recipientId: 789,
  messageText: 'Hello!'
});

// Optimistic update
commit('setSubmitMessage', {
  id: Date.now(), // Temporary ID
  dialogId: 456,
  authorId: currentUserId,
  recipientId: 789,
  messageText: 'Hello!',
  time: Date.now(),
  readStatus: 'UNREAD'
});
```

### Receive Message (WebSocket)
```javascript
// WebSocket listener
socket.on('message', (message) => {
  commit('setNewMessage', message);
  
  // Update dialog last message
  const dialog = state.dialogs.find(d => d.id === message.dialogId);
  if (dialog) {
    dialog.lastMessage = {
      messageText: message.messageText,
      time: message.time
    };
    if (state.activeDialogId !== message.dialogId) {
      dialog.unreadCount += 1;
    }
  }
});
```

## Component Integration

### Dialog List
- Display all dialogs sorted by unread count
- Show recipient name, photo, last message preview
- Badge with unread count per dialog
- Click opens dialog and loads messages

### Chat Window
- Load latest messages on open
- Infinite scroll upward to load older messages
- Auto-scroll to bottom on new messages
- Mark dialog as read when opened

### Message Composition
- Text input with emoji picker
- Send via WebSocket
- Optimistic UI update
- Confirmation when message delivered

### Unread Badge
- Header shows total unread count
- Update on WebSocket message
- Refresh periodically
- Clear when dialogs opened
