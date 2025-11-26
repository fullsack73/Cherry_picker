# Task Breakdown: Show Nearby Store Markers

## Overview
Total Tasks: 5 Groups
Assigned roles: database-engineer, api-engineer, android-engineer, testing-engineer

## Task List

### Backend Layer

#### Task Group 1: Database Queries
**Assigned implementer:** database-engineer
**Dependencies:** None

- [x] 1.0 Implement Store Queries
  - [x] 1.1 Write 2-8 focused tests for store query logic
    - Test finding stores within a simple radius
    - Test filtering by category
    - Test handling of edge cases (no stores, invalid coords)
  - [x] 1.2 Create `local-backend/src/queries/stores.js`
    - Implement `fetchNearbyStores(db, { latitude, longitude, radius, categories })`
    - Query the `merchants` table (which is populated from `merchants_db.csv` at startup)
    - Use bounding box approximation for initial filtering if possible, or Haversine formula
    - Ensure efficient querying of the `merchants` table
  - [x] 1.3 Ensure database query tests pass
    - Run ONLY the tests written in 1.1

**Acceptance Criteria:**
- Can accurately retrieve stores within a given radius (meters)
- Can filter results by category list
- Returns correct store data structure

#### Task Group 2: API Endpoint
**Assigned implementer:** api-engineer
**Dependencies:** Task Group 1

- [x] 2.0 Implement Nearby Stores Endpoint
  - [x] 2.1 Write 2-8 focused tests for `/api/stores/nearby`
    - Test success response with valid params
    - Test validation errors (missing lat/lng)
    - Test empty results
  - [x] 2.2 Implement `GET /api/stores/nearby` in `local-backend/index.js` (or routes)
    - Validate query parameters: `latitude`, `longitude`, `radius` (default 500), `categories`
    - Call `fetchNearbyStores` from Task 1.2
    - Return JSON response with store data
  - [x] 2.3 Ensure API tests pass
    - Run ONLY the tests written in 2.1

**Acceptance Criteria:**
- Endpoint returns 200 OK with JSON array of stores
- Validates input parameters correctly
- Defaults radius to 500m if not provided

### Android Frontend Layer

#### Task Group 3: Data & Repository
**Assigned implementer:** android-engineer
**Dependencies:** Task Group 2

- [x] 3.0 Implement Android Data Layer
  - [x] 3.1 Write 2-8 focused tests for `StoreRepository`
    - Test fetching stores calls API with correct params
    - Test error handling
  - [x] 3.2 Update `ApiService`
    - Add `getNearbyStores` method with query parameters
  - [x] 3.3 Create `StoreRepository`
    - Implement function to fetch nearby stores
    - Map API response to domain models if necessary
  - [x] 3.4 Ensure data layer tests pass
    - Run ONLY the tests written in 3.1

**Acceptance Criteria:**
- `StoreRepository` successfully fetches data from the local backend
- API parameters are correctly mapped

#### Task Group 4: Map UI & Clustering
**Assigned implementer:** android-engineer
**Dependencies:** Task Group 3

- [x] 4.0 Implement Map Markers and UI
  - [x] 4.1 Write 2-8 focused tests for Map Logic (if testable via unit tests) or UI tests
    - Test state updates when "Search Here" is clicked
    - Test filtering logic updates the displayed markers
  - [x] 4.2 Add Google Maps Utils dependency
    - Add `com.google.maps.android:android-maps-utils` to `build.gradle.kts`
  - [x] 4.3 Implement Clustering Classes
    - Create `StoreClusterItem` implementing `ClusterItem`
    - Create `StoreClusterRenderer` extending `DefaultClusterRenderer`
    - Implement custom marker icons (colors by category)
  - [x] 4.4 Implement Map UI Features
    - Add "Search Here" button to the map screen
    - Implement visibility logic (show when map center != user location)
    - Add Category Filter Chips (Food, Cafe, etc.)
  - [x] 4.5 Integrate with `MapStateCoordinator` (or equivalent)
    - Manage state for "current search area" vs "user location"
    - Trigger search on app open and button click
    - Handle marker clicks (show info window/speech bubble)
  - [x] 4.6 Ensure UI tests pass
    - Run ONLY the tests written in 4.1

**Acceptance Criteria:**
- Markers appear on the map at correct locations
- Markers are colored by category
- Clustering works for dense areas
- "Search Here" button works as expected
- Filter chips toggle marker visibility

### Testing

#### Task Group 5: Test Review & Gap Analysis
**Assigned implementer:** testing-engineer
**Dependencies:** Task Groups 1-4

- [x] 5.0 Review and Fill Gaps
  - [x] 5.1 Review tests from Task Groups 1-4
    - Check coverage of backend queries, API, and Android repository
  - [x] 5.2 Analyze coverage gaps
    - Focus on end-to-end flow: App Open -> Fetch -> Display
    - Focus on "Search Here" re-fetch flow
  - [x] 5.3 Write up to 10 additional strategic tests
    - Add integration test for the full flow if possible
    - Add edge case tests for clustering or empty results
  - [x] 5.4 Run feature-specific tests
    - Run all tests created in this spec

**Acceptance Criteria:**
- Critical paths are covered by tests
- Backend and Frontend integration is verified (via mocks or integration tests)
