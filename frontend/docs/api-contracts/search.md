# Search API Contracts

## Overview

This service provides search functionality for users and posts across the platform.

## Endpoints

### 1. GET `/account/search` - Search Users

**Query Parameters:**
- `author` (string, optional) - Search by name (firstName or lastName)
- `firstName` (string, optional) - Filter by first name
- `lastName` (string, optional) - Filter by last name
- `ageFrom` (number, optional) - Minimum age filter
- `ageTo` (number, optional) - Maximum age filter
- `country` (string, optional) - Filter by country
- `city` (string, optional) - Filter by city
- `ids` (string, optional) - Comma-separated user IDs
- `showFriends` (boolean, default: true) - Include friends in results
- `isDeleted` (boolean, default: false) - Include deleted users
- `page` (number, default: 0) - Page number (0-indexed)
- `size` (number, default: 20) - Items per page

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of User objects
  "totalPages": 5,      // ✅ Required - number
  "totalElements": 87,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 20            // ✅ Required - items per page
}
```

### 2. GET `/post` - Search Posts

**Query Parameters:**
- `text` (string, optional) - Search in post title and content
- `author` (string, optional) - Filter by author name
- `tags` (string, optional) - Filter by tag IDs (comma-separated)
- `dateFrom` (string, optional) - Filter by date (ISO format)
- `dateTo` (string, optional) - Filter by date (ISO format)
- `sort` (string, default: "time,desc") - Sort order
- `isDeleted` (boolean, default: false) - Include deleted posts
- `page` (number, default: 0) - Page number
- `size` (number, default: 10) - Items per page

**Response Structure:**
```json
{
  "content": [],        // 🔄 Array - Must always be array of Post objects
  "totalPages": 3,      // ✅ Required - number
  "totalElements": 27,  // ✅ Required - number
  "number": 0,          // ✅ Required - current page number
  "size": 10            // ✅ Required - items per page
}
```

## Data Structures

### User Object (Search Result)

```json
{
  "id": 123,                        // ✅ Required - number
  "firstName": "Ivan",              // ✅ Required - string
  "lastName": "Petrov",             // ✅ Required - string
  "photo": "/avatar.jpg",           // ⚠️ Nullable - string
  "birthDate": "1990-01-15",        // ⚠️ Nullable - string (ISO date)
  "city": "Moscow",                 // ⚠️ Nullable - string
  "country": "Russia",              // ⚠️ Nullable - string
  "statusCode": "FRIEND",           // ⚠️ Nullable - string (if queried with relationship context)
  "isOnline": true,                 // ⚠️ Nullable - boolean
  "isBlocked": false,               // ⚠️ Nullable - boolean
  "isDeleted": false                // ⚠️ Nullable - boolean
}
```

### Post Object (Search Result)

Same as [Posts API](./posts-feeds-wall.md#2-post-object-schema), enriched with author data by frontend.

## Frontend State Management

### Search Store State

```javascript
state: {
  lastSearchUsersRequest: '',       // Last user search params
  lastSearchNewsRequest: '',        // Last post search params
  usersQueryParams: {},             // Current user search filters
  usersQueryParamsRecomend: {},     // Recommendation search filters
  newsQueryParams: {},              // Current post search filters
  searchText: null,                 // Global search text
  usersPagination: { 
    total: 20, 
    page: 1, 
    size: 5 
  },
  usersPaginationRecomend: { 
    total: 20, 
    page: 1, 
    size: 5 
  },
  newsPagination: { 
    total: 20, 
    page: 1, 
    size: 5 
  },
  tabs: [
    { id: 'users', text: 'Люди', textEng: 'People' },
    { id: 'news', text: 'Новости', textEng: 'News' }
  ],
  tabSelect: 'users',               // Current active tab
  result: {
    users: [],                      // ✅ Array - User search results
    news: []                        // ✅ Array - Post search results
  },
  recomendationUsers: [],           // ✅ Array - Recommendation results
  status: ''                        // Loading/error status
}
```

### Safe Array Handling

```javascript
// Action: searchUsers
async searchUsers({ commit }, { payload }) {
  const query = createQuery(payload);
  const response = await axios.get(`account/search?${query.join('&')}&isDeleted=false`);
  
  const content = Array.isArray(response.data.content)
    ? response.data.content
    : [];
  
  commit('setResult', { id: 'users', value: content });
  commit('setUsersQueryParams', payload);
  commit('setLastSearchUsersRequest', payload);
  
  const pagination = createPagination(response);
  commit('setUsersPagination', pagination);
}

// Action: searchNews
async searchNews({ commit, getters, dispatch }, payload) {
  const query = createQuery(payload);
  const response = await axios.get(`post?${query.join('&')}&sort=time,desc&isDeleted=false`);
  
  const content = Array.isArray(response.data.content)
    ? response.data.content
    : [];
  
  // Enrich with authors
  const fullData = await content.reduce(async (acc, post) => {
    const accumulator = await acc;
    const author = await getAuthor(post.authorId, getters.authors, dispatch);
    post = { ...post, author };
    accumulator.push(post);
    return accumulator;
  }, Promise.resolve([]));
  
  // Filter out posts without authors
  const result = fullData.filter((feed) => feed.author !== undefined);
  
  commit('setResult', { id: 'news', value: result });
  commit('setNewsQueryParams', payload);
  commit('setLastSearchNewsRequest', `post?${query.join('&')}`);
  
  const pagination = createPagination(response);
  commit('setNewsPagination', pagination);
}

// Action: searchRecomendations
async searchRecomendations({ commit }, { payload }) {
  const query = createQuery(payload);
  const response = await axios.get(`account/search?${query.join('&')}&isDeleted=false&showFriends=false`);
  
  const data = response.data || {};
  commit('setRecomendations', data);
  commit('setUsersQueryParamsRecomend', payload);
  commit('setLastSearchUsersRequest', payload);
  
  const pagination = createPagination(response);
  commit('setUsersPaginationRecomend', pagination);
}
```

## Important Notes

### Array Safety
- All `content` arrays **must always be arrays**, even if empty
- Frontend applies `Array.isArray()` checks before all operations
- Empty results return `[]`, not null

### Query Building
- Uses `createQuery()` utility to build query strings
- Skips null/undefined values: `if (value) query.push(\`${key}=${value}\`)`
- Joins with `&`: `query.join('&')`
- Supports both string and array query formats

### Author Enrichment (Posts)
- Search returns posts with only `authorId`
- Frontend fetches author details from cache/API
- Uses async reduce to enrich each post
- Filters out posts where author fetch failed
- Cached authors prevent duplicate API calls

### Pagination
- Default sizes: 5 (users), 10 (posts)
- Pages are 0-indexed in API, 1-indexed in UI
- Frontend converts: `page: uiPage - 1` for API
- Stores last query params for "load more"

### Search Tabs
- Two main tabs: Users, News
- Tab selection updates URL query: `?tab=users` or `?tab=news`
- Search text preserved when switching tabs
- Separate pagination state per tab

### Recommendation Search
- Special search for friend recommendations
- Uses `showFriends=false` to exclude existing friends
- Filters by list of recommended user IDs
- Sorted by recommendation rating in frontend

### Last Search Request
- Stores last search params for refresh/load more
- Used when adding/removing friends to refresh results
- Allows returning to same search state

## Usage Examples

### Search Users by Name
```javascript
// Search query
const payload = {
  author: 'Ivan',       // Search by name
  page: 0,
  size: 20
};

// Request
const query = createQuery(payload);
const response = await axios.get(`account/search?${query.join('&')}&isDeleted=false`);

// Safe handling
const content = Array.isArray(response.data.content)
  ? response.data.content
  : [];

// Store results
commit('setResult', { id: 'users', value: content });
```

### Search Users with Filters
```javascript
// Advanced search
const payload = {
  firstName: 'Ivan',
  lastName: 'Petrov',
  ageFrom: 25,
  ageTo: 35,
  country: 'Russia',
  city: 'Moscow',
  page: 0,
  size: 20
};

// Build query: firstName=Ivan&lastName=Petrov&ageFrom=25&ageTo=35&country=Russia&city=Moscow&page=0&size=20
const query = createQuery(payload);
const response = await axios.get(`account/search?${query.join('&')}&isDeleted=false`);

// Handle results...
```

### Search Users by IDs (Recommendations)
```javascript
// Get specific users by ID list
const ids = [123, 456, 789];
const payload = {
  ids: ids.join(','),   // "123,456,789"
  page: 0,
  size: 20,
  showFriends: false    // Exclude friends
};

const query = createQuery(payload);
const response = await axios.get(`account/search?${query.join('&')}&isDeleted=false`);

const content = Array.isArray(response.data.content)
  ? response.data.content
  : [];
```

### Search Posts by Text
```javascript
// Search query
const payload = {
  text: 'programming',  // Search in title and content
  page: 0,
  size: 10
};

// Request
const query = createQuery(payload);
const response = await axios.get(`post?${query.join('&')}&sort=time,desc&isDeleted=false`);

// Safe handling and author enrichment
const content = Array.isArray(response.data.content)
  ? response.data.content
  : [];

const enrichedPosts = await content.reduce(async (acc, post) => {
  const accumulator = await acc;
  const author = await getAuthor(post.authorId, authors, dispatch);
  post = { ...post, author };
  accumulator.push(post);
  return accumulator;
}, Promise.resolve([]));

// Filter out failed author fetches
const validPosts = enrichedPosts.filter(post => post.author !== undefined);

commit('setResult', { id: 'news', value: validPosts });
```

### Search Posts with Filters
```javascript
// Advanced post search
const payload = {
  text: 'Vue.js',
  author: 'Ivan',
  tags: '1,5,7',        // Tag IDs
  dateFrom: '2024-01-01',
  dateTo: '2024-12-31',
  page: 0,
  size: 10
};

const query = createQuery(payload);
const response = await axios.get(`post?${query.join('&')}&sort=time,desc&isDeleted=false`);

// Handle and enrich results...
```

### Load More Results
```javascript
// Increment page and use last search params
const currentPage = state.usersPagination.page;
const nextPage = currentPage + 1;

const payload = {
  ...state.usersQueryParams,  // Reuse last filters
  page: nextPage - 1,         // Convert to 0-indexed
  size: state.usersPagination.size
};

await dispatch('searchUsers', { payload });
```

### Search All (Users + Posts)
```javascript
// Search both tabs with same text
async searchAll({ dispatch, state }, text) {
  // User search
  const searchQueryUsers = {
    ...state.usersQueryParams,
    author: text || '',
    size: state.usersPagination.size,
    page: state.usersPagination.page - 1
  };
  await dispatch('searchUsers', { payload: searchQueryUsers });
  
  // Post search
  const searchQueryNews = {
    ...state.newsQueryParams,
    text: text || '',
    size: state.newsPagination.size,
    page: state.newsPagination.page - 1
  };
  await dispatch('searchNews', searchQueryNews);
}
```

## Component Integration

### Search Page
- Input field for search text
- Tabs for Users/News
- Results list with pagination
- Advanced filters (collapsible)
- "Load More" button

### Search Filters Component
```vue
<template>
  <div class="search-filters">
    <input v-model="filters.firstName" placeholder="First Name" />
    <input v-model="filters.lastName" placeholder="Last Name" />
    <input v-model="filters.ageFrom" type="number" placeholder="Age From" />
    <input v-model="filters.ageTo" type="number" placeholder="Age To" />
    <select v-model="filters.country">
      <option value="">Any Country</option>
      <!-- countries -->
    </select>
    <select v-model="filters.city">
      <option value="">Any City</option>
      <!-- cities -->
    </select>
    <button @click="onSearch">Search</button>
  </div>
</template>
```

### Search Results
- User card: Avatar, name, location, friend status
- Post card: Title, excerpt, author, date, tags
- Infinite scroll or "Load More" pagination
- Empty state: "No results found"
- Loading state during search

### Global Search
- Header search bar
- Quick results dropdown
- "See all results" link to search page
- Recent searches history
