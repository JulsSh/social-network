# Comments API Contracts

## Overview

This service handles comments and sub-comments (nested comments/replies) on posts.

## Endpoints

### 1. GET `/post/{postId}/comment` - Get Post Comments

**Path Parameter:**
- `postId` (number) - ID of the post

**Query Parameters:**
- `page` (number, default: 0) - Page number (0-indexed)
- `size` (number, default: 5) - Items per page
- `sort` (string, default: "time,desc") - Sort order
- `isDeleted` (boolean, default: false) - Include deleted comments

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Comment objects
  "totalPages": 3,      // ✅ Required - number
  "totalElements": 12,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 5             // ✅ Required - items per page
}
```

### 2. GET `/post/{postId}/comment/{commentId}/subcomment` - Get Sub-Comments (Replies)

**Path Parameters:**
- `postId` (number) - ID of the post
- `commentId` (number) - ID of the parent comment

**Query Parameters:**
- Same as parent comments endpoint

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Comment objects
  "totalPages": 1,      // ✅ Required - number
  "totalElements": 3,   // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 5             // ✅ Required - items per page
}
```

### 3. POST `/post/{postId}/comment` - Create Comment

**Path Parameter:**
- `postId` (number) - ID of the post to comment on

**Request Body:**
```json
{
  "commentText": "Great post!",     // ✅ Required - string
  "parentId": null                  // ⚠️ Nullable - number (null for top-level, commentId for reply)
}
```

**Response:** 200 OK (created comment object)

### 4. PUT `/post/{postId}/comment` - Edit Comment

**Path Parameter:**
- `postId` (number) - ID of the post containing the comment

**Request Body:**
```json
{
  "id": 789,                        // ✅ Required - number (comment ID)
  "commentText": "Updated text"     // ✅ Required - string
}
```

**Response:** 200 OK

### 5. DELETE `/post/{postId}/comment/{commentId}` - Delete Comment

**Path Parameters:**
- `postId` (number) - ID of the post
- `commentId` (number) - ID of the comment to delete

**Response:** 200 OK

**Note:** Soft delete - sets `is_deleted: true`, comment still retrievable

### 6. PUT `/post/{postId}/comment/{commentId}/recover` - Recover Deleted Comment

**Path Parameters:**
- `postId` (number) - ID of the post
- `commentId` (number) - ID of the comment to recover

**Response:** 200 OK

## Data Structures

### Comment Object

```json
{
  "id": 789,                        // ✅ Required - number
  "postId": 123,                    // ✅ Required - number
  "commentText": "Great post!",     // ✅ Required - string
  "authorId": 456,                  // ✅ Required - number
  "time": 1234567890,               // ✅ Required - timestamp
  "parentId": null,                 // ⚠️ Nullable - number (null = top-level, number = reply)
  "isBlocked": false,               // ✅ Required - boolean
  "is_deleted": false,              // ✅ Required - boolean
  
  // Personalization fields
  "myLike": false,                  // ⚠️ Nullable - boolean
  "likeAmount": 3,                  // ⚠️ Nullable - number
  "commentsCount": 2,               // ⚠️ Nullable - number (for sub-comments)
  
  // Enriched by frontend (not from API)
  "author": {}                      // ⚠️ Added by frontend - Author object
}
```

### Author Object (Enriched by Frontend)

```json
{
  "id": 456,                        // ✅ Required - number
  "firstName": "Ivan",              // ✅ Required - string
  "lastName": "Petrov",             // ✅ Required - string
  "photo": "/avatar.jpg"            // ⚠️ Nullable - string
}
```

## Frontend State Management

### Comments Store State

```javascript
state: {
  comments: {},         // Object keyed by postId
  subComments: {}       // Object keyed by commentId (parentId)
}
```

**Structure:**
```javascript
comments: {
  123: {                            // postId
    postId: 123,
    currentComments: [],            // ✅ Array of Comment objects
    page: 0,
    totalPages: 3,
    totalElements: 12,
    commentId: null
  }
}

subComments: {
  789: {                            // parentCommentId
    commentId: 789,
    currentComments: [],            // ✅ Array of Comment objects (replies)
    page: 0,
    totalPages: 1,
    totalElements: 3
  }
}
```

### Safe Array Handling

```javascript
// Mutation: addComments
addComments: (state, comments) => {
  const safeCurrentComments = Array.isArray(state.comments[comments.postId]?.currentComments)
    ? state.comments[comments.postId].currentComments
    : [];
  
  const newComments = Array.isArray(comments.currentComments)
    ? comments.currentComments
    : [];
  
  state.comments = {
    ...state.comments,
    [comments.postId]: {
      ...comments,
      currentComments: [...safeCurrentComments, ...newComments]
    }
  };
}

// Mutation: deleteComment
deleteComment: (state, { postId, id }) => {
  const safeComments = Array.isArray(state.comments[postId]?.currentComments)
    ? state.comments[postId].currentComments
    : [];
  
  state.comments = {
    ...state.comments,
    [postId]: {
      ...state.comments[postId],
      currentComments: safeComments.filter((comment) => comment.id !== id)
    }
  };
}
```

## Important Notes

### Array Safety
- `content` field **must always be an array**, even if empty
- `currentComments` in state **must always be an array**
- Frontend applies `Array.isArray()` checks before all array operations
- Empty array means no comments yet

### Comment Hierarchy
- **Top-level comments**: `parentId = null`
- **Replies (sub-comments)**: `parentId = {parent comment ID}`
- Sub-comments stored separately in `subComments` state
- Each level has its own pagination

### Author Enrichment
- API returns only `authorId`
- Frontend fetches author details from `/account/{id}`
- Uses global author cache to prevent duplicate requests
- Adds `author` object to comment before displaying

### Pagination
- Default page size: 5 comments
- Pages are 0-indexed
- Frontend calculates: `page=${currentPage === null ? 0 : currentPage + 1}`
- Each post has separate pagination for comments
- Each comment has separate pagination for sub-comments

### Deletion Behavior
- Soft delete: `is_deleted: true`
- Deleted comments can be recovered
- Filter by `isDeleted=false` to hide deleted comments
- Admins may see deleted comments

### Comment Actions
Frontend combines add/edit logic:
```javascript
if (payload.edit) {
  await editComment(payload);
} else {
  await newComment(payload);
}
```

After creating/editing, clears cache and refetches:
```javascript
if (payload.parentId) {
  commit('clearSubComments');
} else {
  commit('clearComments');
}
dispatch('commentsById', { postId, commentId: payload.parentId || null });
```

## Usage Examples

### Get Comments for Post
```javascript
// Request
const response = await axios.get(
  `post/123/comment?sort=time,desc&size=5&isDeleted=false&page=0`
);

// Safe handling
const content = Array.isArray(response.data.content) 
  ? response.data.content 
  : [];

// Enrich with authors
const currentComments = await content.reduce(async (acc, comment) => {
  const accumulator = await acc;
  const author = await getAuthor(comment.authorId, authors, dispatch);
  comment = { ...comment, author };
  accumulator.push(comment);
  return accumulator;
}, Promise.resolve([]));

// Store
commit('addComments', {
  postId: 123,
  currentComments,
  page: response.data.number,
  totalPages: response.data.totalPages,
  totalElements: response.data.totalElements,
  commentId: null
});
```

### Get Sub-Comments (Replies)
```javascript
// Request
const response = await axios.get(
  `post/123/comment/789/subcomment?sort=time,desc&isDeleted=false&page=0`
);

// Safe handling
const content = Array.isArray(response.data.content) 
  ? response.data.content 
  : [];

// Enrich and store
const currentComments = await enrichWithAuthors(content);
commit('addSubComments', {
  commentId: 789,
  currentComments,
  page: response.data.number,
  totalPages: response.data.totalPages,
  totalElements: response.data.totalElements
});
```

### Create Top-Level Comment
```javascript
// Request
await axios.post(`post/123/comment`, {
  parentId: null,
  commentText: 'Great post!'
});

// Refresh comments
commit('clearComments');
dispatch('commentsById', { postId: 123, commentId: null });
```

### Create Reply (Sub-Comment)
```javascript
// Request
await axios.post(`post/123/comment`, {
  parentId: 789,      // Parent comment ID
  commentText: 'I agree!'
});

// Refresh sub-comments
commit('clearSubComments');
dispatch('commentsById', { postId: 123, commentId: 789 });
```

### Edit Comment
```javascript
// Request
await axios.put(`post/123/comment`, {
  id: 789,
  commentText: 'Updated comment text'
});

// Refresh appropriate list
if (parentId) {
  commit('clearSubComments');
  dispatch('commentsById', { postId: 123, commentId: parentId });
} else {
  commit('clearComments');
  dispatch('commentsById', { postId: 123, commentId: null });
}
```

### Delete Comment
```javascript
// Request
await axios.delete(`post/123/comment/789`);

// Update state locally (optimistic update)
if (parentId) {
  commit('deleteSubComment', { id: 789, parentId });
} else {
  commit('deleteComment', { postId: 123, id: 789 });
}
```

### Recover Deleted Comment
```javascript
// Request
await axios.put(`post/123/comment/789/recover`);

// Refresh to show recovered comment
dispatch('commentsById', { postId: 123 });
```

## Component Integration

### Comment Display
- Iterate over `currentComments` array safely
- Show author name, photo, timestamp
- Display like count and user's like status
- Show reply count for comments with sub-comments
- Highlight deleted state if `is_deleted: true`

### Load More Comments
- Track current page per post
- Increment page number
- Append new comments to existing array
- Disable "Load More" when `page >= totalPages - 1`

### Nested Comments
- Top-level comments loaded first
- Click "Show replies" loads sub-comments
- Each comment ID has own sub-comment state
- Separate pagination for each level
