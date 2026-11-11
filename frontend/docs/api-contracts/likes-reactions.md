# Likes & Reactions API Contracts

## Overview

This service handles likes and emoji reactions on posts and comments.

## Endpoints

### 1. POST `/post/{postId}/like` - Add Like to Post

**Path Parameter:**
- `postId` (number) - ID of the post to like

**Request Body:**
```json
{
  "reactionType": "heart"       // ⚠️ Optional - string (heart|funny|wow|delight|sadness|malice)
}
```

**Response:** 200 OK

### 2. DELETE `/post/{postId}/like` - Remove Like from Post

**Path Parameter:**
- `postId` (number) - ID of the post to unlike

**Response:** 200 OK

### 3. POST `/post/{postId}/comment/{commentId}/like` - Add Like to Comment

**Path Parameters:**
- `postId` (number) - ID of the post containing the comment
- `commentId` (number) - ID of the comment to like

**Request Body:**
```json
{
  "reactionType": "heart"       // ⚠️ Optional - string
}
```

**Response:** 200 OK

### 4. DELETE `/post/{postId}/comment/{commentId}/like` - Remove Like from Comment

**Path Parameters:**
- `postId` (number) - ID of the post containing the comment
- `commentId` (number) - ID of the comment to unlike

**Response:** 200 OK

## Data Structures

### Reaction Types

Available reaction types (emoji reactions):
- `heart` - ❤️ Нравится (Like)
- `funny` - 😄 Смешно (Funny)
- `wow` - 😮 Ого! (Wow)
- `delight` - 😍 Восторг (Delight)
- `sadness` - 😢 Печаль (Sadness)
- `malice` - 😡 &$#%! (Malice)

### Reaction Info Object

**Used in:** Post and Comment objects, `reactionsInfo` prop

```json
{
  "reactionType": "heart",      // ✅ Required - string
  "count": 5                    // ✅ Required - number (how many users reacted with this)
}
```

### Like Status in Post/Comment

When querying posts or comments, the response includes:

```json
{
  "id": 123,
  "myLike": false,              // ⚠️ Nullable - boolean (did current user like?)
  "myReaction": "heart",        // ⚠️ Nullable - string (current user's reaction type)
  "likeAmount": 15,             // ⚠️ Nullable - number (total like count)
  "reactionType": []            // 🔄 Array - Must always be array of ReactionInfo objects
}
```

## Frontend Implementation

### Reaction State Management

The frontend tracks reactions locally with these patterns:

```javascript
// Safe array handling for reactions
const reactions = Array.isArray(localReactionsInfo.value) 
  ? localReactionsInfo.value 
  : [];

// Check if reaction exists
const hasReaction = reactions.filter(
  type => type.reactionType === selectedReaction
).length > 0;

// Find specific reaction
const reaction = reactions.find(
  r => r.reactionType === reactionType
);
```

### Adding Reaction

**Frontend Logic:**
1. User clicks reaction emoji
2. If user already has a reaction:
   - Decrement count for old reaction
   - Increment count for new reaction (or add if doesn't exist)
3. If user doesn't have a reaction:
   - Increment count for selected reaction (or add if doesn't exist)
4. Emit event to update backend

**Backend Request:**
```javascript
// Add/change reaction
axios.post(`post/${postId}/like`, { reactionType: 'heart' })
```

### Removing Reaction

**Frontend Logic:**
1. User clicks their current reaction
2. Decrement count for that reaction
3. Remove reaction if count reaches 0
4. Emit event to update backend

**Backend Request:**
```javascript
// Remove reaction
axios.delete(`post/${postId}/like`)
```

## Important Notes

### Array Safety
- `reactionType` array **must always be an array**, even if empty `[]`
- Frontend applies `Array.isArray()` checks before all array operations
- Empty reactions list means no one has reacted yet

### Personalization
- `myLike` indicates if current user liked the item (boolean)
- `myReaction` indicates which emoji current user used (string)
- Both can be `null` if user hasn't interacted

### Reaction Count Management
- Frontend optimistically updates counts before API call
- Each `ReactionInfo` object tracks count per reaction type
- When count reaches 0, remove the reaction from array
- Backend is source of truth for final counts

### Animation Classes
Frontend uses CSS classes for visual feedback:
- `added` - Grow animation when adding reaction
- `deleted` - Shrink animation when removing reaction
- `selected` - Highlight current user's reaction

## Usage Examples

### Display Reactions
```javascript
// Component props
props: {
  reactionsInfo: {
    type: Array,
    default: () => []  // ✅ Safe default
  },
  reaction: String,    // Current user's reaction
  quantity: Number     // Like count
}

// Safe iteration
const reactions = Array.isArray(props.reactionsInfo) 
  ? props.reactionsInfo 
  : [];
  
reactions.forEach(react => {
  console.log(`${react.reactionType}: ${react.count}`);
});
```

### Add Reaction to Post
```javascript
// Request
const reactionType = 'heart';
await axios.post(`post/${postId}/like`, { reactionType });

// Update local state
const reactions = Array.isArray(localReactions) ? localReactions : [];
const existing = reactions.find(r => r.reactionType === reactionType);
if (existing) {
  existing.count += 1;
} else {
  reactions.push({ reactionType, count: 1 });
}
```

### Remove Reaction from Comment
```javascript
// Request
await axios.delete(`post/${postId}/comment/${commentId}/like`);

// Update local state
const reactions = Array.isArray(localReactions) ? localReactions : [];
const index = reactions.findIndex(r => r.reactionType === currentReaction);
if (index !== -1) {
  reactions[index].count -= 1;
  if (reactions[index].count === 0) {
    reactions.splice(index, 1);
  }
}
```

## Component Integration

### PostReactions Component
Props:
- `reactionsInfo: Array` (default: `[]`) - List of reactions with counts
- `reaction: String` - Current user's reaction type
- `quantity: Number` - Total like count
- `active: Boolean` - Whether current user has liked

Events:
- `@reaction-added` - Emitted when user adds/changes reaction
- `@reaction-deleted` - Emitted when user removes reaction

Safe usage:
```javascript
const localReactionsInfo = ref(
  Array.isArray(props.reactionsInfo) 
    ? JSON.parse(JSON.stringify(props.reactionsInfo))
    : []
);
```
