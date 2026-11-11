# Geo API Contracts

## Overview

This service provides geographic data (countries and cities) for user profiles, search filters, and location selection.

## Endpoints

### 1. GET `/geo/country` - Get All Countries

**Response:**
```json
[]                      // 🔄 Array - Direct array of Country objects
```

**Note:** Returns array directly, not wrapped in pagination object

### 2. GET `/geo/country/{countryId}/city` - Get Cities by Country

**Path Parameter:**
- `countryId` (number) - Country ID to fetch cities for

**Response:**
```json
[]                      // 🔄 Array - Direct array of City objects
```

**Note:** Returns array directly, not wrapped in pagination object

## Data Structures

### Country Object

```json
{
  "id": 1,                          // ✅ Required - number
  "name": "Russia"                  // ✅ Required - string
}
```

### City Object

```json
{
  "id": 101,                        // ✅ Required - number
  "name": "Moscow",                 // ✅ Required - string
  "countryId": 1                    // ✅ Required - number (parent country)
}
```

## Frontend State Management

### Geo Store State

```javascript
state: {
  countries: [],        // ✅ Array - List of all Country objects
  cities: {}            // Object keyed by countryId, values are City arrays
}
```

**Cities Structure:**
```javascript
cities: {
  1: [                  // countryId = 1 (Russia)
    { id: 101, name: "Moscow", countryId: 1 },
    { id: 102, name: "Saint Petersburg", countryId: 1 },
    // ...
  ],
  2: [                  // countryId = 2 (USA)
    { id: 201, name: "New York", countryId: 2 },
    { id: 202, name: "Los Angeles", countryId: 2 },
    // ...
  ]
}
```

### Safe Array Handling

```javascript
// Mutation: setCountries
setCountries: (state, payload) => {
  const safeCountries = Array.isArray(payload) ? payload : [];
  state.countries = safeCountries;
}

// Mutation: setCity
setCity: (state, payload) => {
  const safeCities = Array.isArray(payload) ? payload : [];
  
  if (safeCities.length > 0) {
    const countryId = safeCities[0].countryId;
    state.cities = {
      ...state.cities,
      [countryId]: safeCities
    };
  }
}

// Action: apiGeo (load all countries and cities)
async apiGeo({ commit }) {
  // Fetch countries
  const countriesRequest = await axios.get('/geo/country');
  const countries = Array.isArray(countriesRequest.data)
    ? countriesRequest.data
    : [];
  
  commit('setCountries', countries);
  
  // Fetch cities for each country in parallel
  await Promise.all(
    countries.map(async (country) => {
      const citiesRequest = await axios.get(`/geo/country/${country.id}/city`);
      const cities = Array.isArray(citiesRequest.data)
        ? citiesRequest.data
        : [];
      commit('setCity', cities);
    })
  );
}
```

## Important Notes

### Array Safety
- Both endpoints return **direct arrays**, not pagination objects
- Arrays **must always be arrays**, even if empty
- Frontend applies `Array.isArray()` checks before all operations

### Data Loading Strategy
- Load all countries on app initialization
- Load cities for all countries in parallel (using `Promise.all()`)
- Cache in Vuex store for entire session
- No pagination needed (datasets are small)

### City Lookup
- Cities stored as object keyed by `countryId`
- Access cities for country: `state.cities[countryId]`
- If country has no cities, key won't exist (check with `state.cities[countryId]` before accessing)

### Usage in Forms
- **Country dropdown**: Populate from `state.countries` array
- **City dropdown**: Filter `state.cities[selectedCountryId]` based on selected country
- **Search filters**: Allow filtering users by country and/or city

### Performance
- All data loaded once at app start
- Stored in memory for fast access
- No need to refetch during session
- Small data size (typically < 100 countries, < 10,000 cities total)

## Usage Examples

### Load All Geo Data
```javascript
// Action in store
async apiGeo({ commit }) {
  try {
    // Fetch countries
    const countriesRequest = await axios.get('/geo/country');
    const countries = Array.isArray(countriesRequest.data)
      ? countriesRequest.data
      : [];
    
    commit('setCountries', countries);
    
    // Fetch cities for all countries in parallel
    const cityPromises = countries.map(async (country) => {
      const citiesRequest = await axios.get(`/geo/country/${country.id}/city`);
      const cities = Array.isArray(citiesRequest.data)
        ? citiesRequest.data
        : [];
      return { countryId: country.id, cities };
    });
    
    const cityResults = await Promise.all(cityPromises);
    
    cityResults.forEach(({ cities }) => {
      if (cities.length > 0) {
        commit('setCity', cities);
      }
    });
  } catch (error) {
    console.error('Failed to load geo data:', error);
  }
}

// Component: call on app mount
onMounted(() => {
  dispatch('global/geo/apiGeo');
});
```

### Get Countries for Dropdown
```javascript
// Component computed property
const countries = computed(() => {
  const list = store.state.global.geo.countries;
  return Array.isArray(list) ? list : [];
});

// Template
<select v-model="selectedCountryId">
  <option value="">Select Country</option>
  <option 
    v-for="country in countries" 
    :key="country.id" 
    :value="country.id"
  >
    {{ country.name }}
  </option>
</select>
```

### Get Cities for Selected Country
```javascript
// Component computed property
const cities = computed(() => {
  if (!selectedCountryId.value) return [];
  
  const citiesByCountry = store.state.global.geo.cities[selectedCountryId.value];
  return Array.isArray(citiesByCountry) ? citiesByCountry : [];
});

// Template
<select v-model="selectedCityId" :disabled="!selectedCountryId">
  <option value="">Select City</option>
  <option 
    v-for="city in cities" 
    :key="city.id" 
    :value="city.id"
  >
    {{ city.name }}
  </option>
</select>
```

### Search Users by Location
```javascript
// Search filter payload
const searchPayload = {
  country: selectedCountryId.value,  // Country ID
  city: selectedCityId.value,        // City ID
  firstName: searchTerm.value,
  // ... other filters
};

await dispatch('global/search/searchUsers', { payload: searchPayload });
```

### Profile Edit - Set Location
```javascript
// Get country and city names for display
const getCountryName = (countryId) => {
  const countries = Array.isArray(store.state.global.geo.countries)
    ? store.state.global.geo.countries
    : [];
  const country = countries.find(c => c.id === countryId);
  return country ? country.name : '';
};

const getCityName = (countryId, cityId) => {
  const cities = store.state.global.geo.cities[countryId];
  if (!Array.isArray(cities)) return '';
  const city = cities.find(c => c.id === cityId);
  return city ? city.name : '';
};

// Update profile
await axios.put('account/me', {
  country: getCountryName(selectedCountryId),
  city: getCityName(selectedCountryId, selectedCityId),
  // ... other fields
});
```

### Initialize Geo Data on App Load
```javascript
// In App.vue or main layout
onBeforeMount(async () => {
  // Load geo data once for entire app
  await dispatch('global/geo/apiGeo');
});
```

## Component Integration

### Location Selector Component
```vue
<template>
  <div class="location-selector">
    <select v-model="localCountry" @change="onCountryChange">
      <option value="">Select Country</option>
      <option 
        v-for="country in countries" 
        :key="country.id" 
        :value="country.id"
      >
        {{ country.name }}
      </option>
    </select>
    
    <select 
      v-model="localCity" 
      :disabled="!localCountry"
    >
      <option value="">Select City</option>
      <option 
        v-for="city in filteredCities" 
        :key="city.id" 
        :value="city.id"
      >
        {{ city.name }}
      </option>
    </select>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';

const store = useStore();
const localCountry = ref('');
const localCity = ref('');

const countries = computed(() => {
  const list = store.state.global.geo.countries;
  return Array.isArray(list) ? list : [];
});

const filteredCities = computed(() => {
  if (!localCountry.value) return [];
  const citiesByCountry = store.state.global.geo.cities[localCountry.value];
  return Array.isArray(citiesByCountry) ? citiesByCountry : [];
});

const onCountryChange = () => {
  // Reset city when country changes
  localCity.value = '';
};
</script>
```

### Search Filter - Location
- Country dropdown populated from `countries` array
- City dropdown populated from `cities[selectedCountryId]`
- Reset city when country changes
- Allow "Any" option (empty value) for both
- Submit selected IDs with search query

### User Profile Display
- Show country and city names (not IDs)
- Lookup from geo store using IDs
- Display "Not specified" if missing
- Click to view other users from same location

## Data Consistency

### Country IDs
- Consistent across all countries
- Used in User profile (`user.country`)
- Used in City object (`city.countryId`)

### City IDs
- Consistent across all cities
- Used in User profile (`user.city`)
- Unique within country

### Missing Data
- If country ID not found in store, display ID or "Unknown"
- If city ID not found, display ID or "Unknown"
- Graceful degradation for missing geo data
