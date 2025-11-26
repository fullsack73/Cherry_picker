# Spec Requirements: show-nearby-store-markers

## Initial Description
showing map markers of the stores at the DB that are around certain radius of the user's position, or user's designated place

## Requirements Discussion

### First Round Questions

**Q1:** I assume the "DB" refers to the `local-backend` service (Node.js/SQLite) that serves the store data. Is that correct, or should the Android app access a local database directly on the device?
**Answer:** The stores data is actually stores as .csv file, which is `merchants_db.csv` and yeah, the data is served to the app through the express js server.

**Q2:** I'm thinking a default search radius of 1km would be appropriate. Should this be hardcoded, or should we allow the user to adjust the radius via the UI?
**Answer:** I figure 1km is kinda far away for someone to walk to. we should make it 500m. and yes, it doesn't hurt to make it able for user to custom the radius.

**Q3:** regarding "user's designated place", I assume this means if the user pans the map to a different location, we should search around the map's center. Is that correct, or do you want a specific "search by address/place name" feature?
**Answer:** Yes, if user browses someplace else on the map and, for example, click search button(or make it detect the change and refresh automatically) the app should show the markers that are around that location.

**Q4:** When a user taps a store marker, I assume we should show a small info window (bottom sheet or popup) with the Store Name and Category. Should we display any other details at this stage (e.g., recommended cards)?
**Answer:** I think just showing them the name of the business at the top of the marker as a speech blob, while making other markers small and transparent should be enough.

**Q5:** I'm thinking we should limit the number of markers displayed (e.g., max 20 or 50) to prevent map clutter and performance issues. Do you have a preference for this limit?
**Answer:** In that way, user might not find the place they wanted to visit due to the limit. and if there is cluttering, we should bind multiple markers as one, and when user clicks it, it displays a small window with a list of all the clustered markers. and as for the performance issue, i don't think it would matter, since there is not much place where the stores are overly crowded to be enough to hinder the performance.

**Q6:** Should the search trigger automatically as the user moves the map, or should we provide a "Search this area" button to minimize API/DB calls?
**Answer:** I figure we should do both, search once when app opens, then search again when location is changed, then when the button is clicked.

**Q7:** Are there specific categories of stores we should prioritize or filter for, or should we display all available stores in the DB within the radius?
**Answer:** We should display everything.

### Existing Code to Reference
No similar existing features identified for reference.

### Follow-up Questions

**Follow-up 1:** Backend Implementation: I see the `local-backend` currently has a placeholder `/stores` endpoint. I assume I should implement a new endpoint (e.g., `/api/stores/nearby`) that queries the SQLite `merchants` table using the user's location and radius. Is that correct?
**Answer:** If you think that suites the app the best, then yes.

**Follow-up 2:** Category Mapping: The mockup shows 4 specific categories: Cafe (Yellow), Food (Orange), Shopping (Pink), Convenience (Blue). The `merchants_db.csv` likely has raw category names. I need to map these. Can you provide the mapping rules, or should I infer them from the `local-backend/config/category-mapping.json` file if it exists?
**Answer:** We're working on re-tagging them accordingly to the cards' benefit data. but for now, just refer to the business category at `merchants_db.csv` like "음식", "숙박", "소매".

**Follow-up 3:** Clustering: I plan to use the Google Maps Utils library for clustering markers as it's the standard way to handle "binding multiple markers" on Android. Is this acceptable?
**Answer:** Yes, you may use that.

**Follow-up 4:** Filters: Although you said "display everything", the mockup shows filter chips for the categories. Should I implement these filter chips now so users can toggle categories, or should I just display all markers with the correct colors for this iteration?
**Answer:** Oh, yes, we should implement filter chips.

## Visual Assets

### Files Provided:
- `main_screen.png`: A high-fidelity mockup showing the map interface.

### Visual Insights:
- **Map Interface:** Shows a map with a user location indicator (blue dot with radius circle).
- **Markers:** Circular markers with icons representing categories (Cafe, Food, Shopping, Convenience).
- **Clustering:** Implied need for handling multiple markers (though not explicitly shown as clustered in the traditional sense, the user mentioned it).
- **Filter Chips:** Floating action buttons or chips at the top right for filtering categories (Cafe, Food, Shopping, Convenience).
- **Search Area:** A search bar at the top.
- **Current Location:** Text indicating the current address/location.
- **Radius:** A visual circle indicating the search radius around the user.

## Requirements Summary

### Functional Requirements
- **Backend:**
    - Implement `/api/stores/nearby` endpoint in `local-backend`.
    - Query SQLite `merchants` table based on latitude, longitude, and radius.
    - Support category filtering in the API.
- **Frontend (Android):**
    - Display markers for stores within a 500m radius (default).
    - Allow user to customize the search radius.
    - "Search this area" functionality:
        - Auto-search on app open.
        - Auto-search on significant location change.
        - Manual search button/trigger when map is panned.
    - **Marker Display:**
        - Show store name in a "speech blob" above the marker when selected.
        - Make non-selected markers small/transparent.
        - Use Google Maps Utils for clustering markers to handle density.
        - Clicking a cluster expands it or shows a list.
    - **Filtering:**
        - Implement filter chips for categories (e.g., Food, Accommodation, Retail - mapped from CSV data).
    - **Visuals:**
        - Match the color coding from the mockup where possible (Cafe=Yellow, Food=Orange, Shopping=Pink, Convenience=Blue).

### Reusability Opportunities
- **Google Maps Utils:** Use for clustering.
- **Existing Map Component:** Build upon the existing map implementation.
- **Local Backend:** Extend the existing Express server and SQLite DB.

### Scope Boundaries
**In Scope:**
- Backend endpoint for nearby stores.
- Android map marker rendering.
- Marker clustering.
- "Search here" logic.
- Category filter chips.
- Basic marker interaction (show name).

**Out of Scope:**
- Detailed store info screen (beyond name).
- Complex routing/navigation to the store.
- User reviews or ratings.
- "Re-tagging" of all categories (use raw CSV categories for now).

### Technical Considerations
- **Performance:** Use clustering to handle large numbers of markers.
- **Data Source:** `merchants_db.csv` loaded into SQLite.
- **API:** REST endpoint returning JSON.
- **Radius:** Default 500m, customizable.
