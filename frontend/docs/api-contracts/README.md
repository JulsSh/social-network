# API Contract Documentation

This directory contains the actual API contracts used by the frontend application, based on real usage patterns found in the codebase.

## Purpose

These contracts document:
- **Expected response structures** from backend microservices
- **Required fields** that the frontend depends on
- **Data types** for each field
- **Array expectations** (must always be arrays, even if empty)
- **Nullable fields** (can be null/undefined)
- **Personalization fields** (myLike, myReaction, etc.)

## Organization

Contracts are organized by domain:

1. **[Posts, Feeds & Wall](./posts-feeds-wall.md)** - News feed, user wall posts
2. **[Likes & Reactions](./likes-reactions.md)** - Post and comment likes, emoji reactions
3. **[Friends & Recommendations](./friends-recommendations.md)** - Friend relationships, requests, recommendations
4. **[Comments](./comments.md)** - Post comments and sub-comments
5. **[Dialogs & Messages](./dialogs-messages.md)** - Direct messaging, conversations
6. **[Notifications](./notifications.md)** - User notifications (posts, comments, friend requests)
7. **[Geo](./geo.md)** - Countries and cities
8. **[Search](./search.md)** - Search users and posts
9. **[Account & User](./account-user.md)** - User profile information

## Notation

### Field Requirements
- **✅ Required** - Field must always be present
- **⚠️ Nullable** - Field can be null or undefined
- **🔄 Array** - Must always be an array (even if empty `[]`)

### Data Types
- `string` - Text value
- `number` - Numeric value (integer or float)
- `boolean` - true/false
- `timestamp` - ISO 8601 datetime or Unix timestamp
- `array` - Array of objects or primitives
- `object` - Nested object structure
- `null` - Explicitly nullable value

## Usage

Frontend code has been updated to safely handle all data from these APIs using `Array.isArray()` checks before calling array methods like `.map()`, `.filter()`, `.forEach()`, etc.

Example pattern:
```javascript
// Safe array handling
const content = Array.isArray(response.data.content) ? response.data.content : [];
const items = content.map(item => item.id);
```

## Maintenance

When backend APIs change:
1. Update the relevant contract document
2. Update frontend code to handle new structure
3. Add array safety checks for new array fields
4. Test thoroughly before deployment
