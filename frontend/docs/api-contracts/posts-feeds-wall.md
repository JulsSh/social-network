# Posts, Feeds & Wall API Contracts

## Overview

This service handles news feed posts, user wall posts, and post management (create, edit, delete).

## Endpoints

### 1. GET `/post` - Get Posts / Wall

**Query Parameters:**
- `accountIds` (string, optional) - Filter by author account ID(s)
- `withFriends` (boolean, optional) - Include friends' posts
- `page` (number, optional) - Page number (0-indexed)
- `size` (number, default: 3-5) - Items per page
- `sort` (string, default: "time,desc") - Sort order
- `isDeleted` (boolean, default: false) - Include deleted posts

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array
  "totalPages": 0,      // ✅ Required - number
  "totalElements": 0,   // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 5             // ✅ Required - items per page
}
```

### 2. Post Object Schema

**Used in:** Feeds, Wall, Search results

```json
{
  "id": 123,                    // ✅ Required - number
  "title": "Post title",        // ✅ Required - string
  "postText": "Content...",     // ✅ Required - string
  "authorId": 456,              // ✅ Required - number
  "time": 1234567890,           // ✅ Required - timestamp
  "timeChanged": 1234567890,    // ⚠️ Nullable - timestamp (only if edited)
  "imagePath": "/path.jpg",     // ⚠️ Nullable - string
  "publishDate": 1234567890,    // ⚠️ Nullable - timestamp
  "tags": [],                   // 🔄 Array - Must always be array of Tag objects
  "type": "POSTED",             // ✅ Required - string (POSTED | QUEUED)
  "isBlocked": false,           // ✅ Required - boolean
  "isDeleted": false,           // ✅ Required - boolean
  
  // Personalization fields
  "myLike": false,              // ⚠️ Nullable - boolean (user's like status)
  "myReaction": "heart",        // ⚠️ Nullable - string (user's reaction type)
  "likeAmount": 5,              // ⚠️ Nullable - number (total likes)
  "commentsCount": 3,           // ⚠️ Nullable - number (total comments)
  "reactionType": [],           // 🔄 Array - Must always be array of Reaction objects
  
  // Enriched by frontend (not from API)
  "author": {}                  // ⚠️ Added by frontend - Author object
}
```

### 3. Tag Object Schema

```json
{
  "id": 1,                      // ✅ Required - number
  "name": "programming"         // ✅ Required - string
}
```

### 4. Reaction Object Schema

**Used in:** reactionType array in Post object

```json
{
  "reactionType": "heart",      // ✅ Required - string (heart|funny|wow|delight|sadness|malice)
  "count": 5                    // ✅ Required - number
}
```

## Mutations

### 5. POST `/post` - Create Post

**Request Body:**
```json
{
  "title": "New Post",          // ✅ Required - string
  "postText": "Content...",     // ✅ Required - string
  "publishDate": 1234567890,    // ⚠️ Nullable - timestamp
  "tags": [],                   // 🔄 Array - Array of Tag objects (can be empty)
  "imagePath": "/path.jpg"      // ⚠️ Nullable - string
}
```

**Frontend normalization:**
```javascript
const safeTags = Array.isArray(payload.tags) ? payload.tags : [];
data.tags = safeTags.map(tag => ({ id: tag.id, name: tag.name }));
```

### 6. PUT `/post/{id}` - Update Post

**Request Body:** Same as POST, plus:
```json
{
  "id": 123                     // ✅ Required - number
}
```

### 7. DELETE `/post/{id}` - Delete Post

**Path Parameter:**
- `id` (number) - Post ID to delete

**Response:** 200 OK (no body)

## Important Notes

### Array Safety
- `content` field **must always be an array**, even if empty
- `tags` field **must always be an array**, even if empty  
- `reactionType` field **must always be an array**, even if empty
- Frontend applies `Array.isArray()` checks before all `.map()`, `.filter()`, `.reduce()` operations

### Personalization
- `myLike`, `myReaction` indicate current user's interaction with post
- Used for highlighting UI elements
- Can be `null` if user hasn't interacted

### Author Enrichment
- API returns only `authorId`
- Frontend fetches and caches author details from `/account/{id}`
- Adds `author` object to post before storing in Vuex

### Pagination
- Pages are 0-indexed
- Default page size: 3-5 items
- Frontend extracts: `{ total, size, page, totalElements }` using `createPagination()` utility

## Usage Examples

### Get News Feed
```javascript
// Request
axios.get('post?withFriends=true&sort=time,desc&isDeleted=false&size=5')

// Safe handling
const content = Array.isArray(response.data.content) ? response.data.content : [];
const feeds = await content.reduce(async (acc, post) => {
  // Enrich with author...
}, []);
```

### Get User Wall
```javascript
// Request
axios.get('post?accountIds=123&sort=time,desc&isDeleted=false')

// Safe handling
const content = Array.isArray(response.data.content) ? response.data.content : [];
const wall = content.map(post => ({ ...post, author }));
```

### Create Post
```javascript
// Request with safe tags
const safeTags = Array.isArray(payload.tags) ? payload.tags : [];
const data = {
  title: payload.title,
  postText: payload.postText,
  tags: safeTags.map(tag => ({ id: tag.id, name: tag.name })),
  imagePath: payload.imagePath
};
axios.post('post', data);
```
