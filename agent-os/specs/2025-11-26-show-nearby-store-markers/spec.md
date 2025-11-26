# Specification: Show Nearby Store Markers

## Goal
Display map markers for stores from the database that are within a specified radius of the user's current location or a designated map center, allowing users to identify nearby merchants and their categories.

## User Stories
- As a user, I want to see markers for stores near my current location so I can find places to shop or eat.
- As a user, I want to filter stores by category (e.g., Food, Cafe) so I can find specific types of businesses.
- As a user, I want to search for stores in a different area by moving the map and clicking "Search Here" so I can plan visits.
- As a user, I want to see the name of the store when I tap a marker so I know what business it is.
- As a user, I want clustered markers to expand when clicked so I can see individual stores in crowded areas.

## Core Requirements
### Functional Requirements
- **Nearby Search:** Fetch and display stores within a default 500m radius of the target location.
- **Custom Radius:** Allow users to adjust the search radius (UI implementation details TBD, backend should support it).
- **"Search Here" Trigger:**
    - Automatically search on app launch.
    - Automatically search when user location changes significantly.
    - Provide a manual "Search Here" button when the user pans the map away from the current location.
- **Marker Display:**
    - Display markers with color-coding based on category (Cafe=Yellow, Food=Orange, Shopping=Pink, Convenience=Blue).
    - Show store name in a speech bubble/info window upon selection.
    - Dim/make transparent non-selected markers.
- **Clustering:** Group nearby markers using Google Maps Utils to prevent clutter.
- **Filtering:** Filter chips to toggle visibility of store categories (Food, Accommodation, Retail, etc.).

### Non-Functional Requirements
- **Performance:** Efficiently handle hundreds of markers using clustering.
- **Responsiveness:** API response time should be under 500ms for radius queries.

## Visual Design
- **Mockup Reference:** `planning/visuals/main_screen.png`
- **Key Elements:**
    - Category Filter Chips (top right).
    - "Search Here" Button (appears on map pan).
    - Custom Marker Icons (colored circles with category icons).
    - Selected Marker State (Speech bubble with name).

## Reusable Components
### Existing Code to Leverage
- **Backend:**
    - `local-backend/index.js`: Express server entry point.
    - `local-backend/src/db.js`: Database connection.
    - `merchants` table in SQLite (already populated).
- **Frontend:**
    - `teamcherrypicker.com.api.ApiService`: Retrofit interface.
    - `teamcherrypicker.com.ui.main.map.MapStateCoordinator`: Existing map state management.

### New Components Required
- **Backend:**
    - `local-backend/src/queries/stores.js`: Logic for querying merchants by geolocation and radius.
    - `/api/stores/nearby` endpoint.
- **Frontend:**
    - `StoreClusterItem`: Implementation of `ClusterItem` for Google Maps Utils.
    - `StoreClusterRenderer`: Custom renderer for marker styling.
    - `StoreRepository`: Repository for fetching store data.
    - `MapMarkerManager`: Helper to manage clustering and marker states.

## Technical Approach
- **Database:** Use SQLite `merchants` table. Query using bounding box approximation or Haversine formula (if supported by SQLite extension, otherwise filter in JS after bounding box select).
- **API:**
    - `GET /api/stores/nearby`
    - Query Params: `latitude`, `longitude`, `radius` (meters), `categories` (comma-separated).
    - Response: JSON array of store objects.
- **Frontend:**
    - Add `getNearbyStores` to `ApiService`.
    - Integrate `com.google.maps.android:android-maps-utils` dependency.
    - Implement `ClusterManager` in the map view/composable.
    - Add state for "Map Center" vs "User Location" to toggle "Search Here" button visibility.

## Out of Scope
- Detailed store information screen (hours, phone, etc.).
- Navigation/Routing to the store.
- User reviews.
- Complex category re-mapping (use raw CSV categories for now).

## Success Criteria
- Users can see markers appear around their location upon opening the app.
- Moving the map and clicking "Search Here" loads stores for the new area.
- Filtering by category updates the visible markers.
- Clustering works smoothly for dense areas.
