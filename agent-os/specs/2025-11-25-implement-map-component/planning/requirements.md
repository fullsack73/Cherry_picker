# Spec Requirements: implement-map-component

## Initial Description
now i need to implement map component, which is currently just empty

## Requirements Discussion

### First Round Questions

**Q1:** I’m assuming the map should auto-center on the user’s current GPS location when the screen opens, with their position pinned. Is that correct, or should it default to the last searched/selected area instead?
**Answer:** It should auto center on the user, but the user must be able to pan around freely afterward.

**Q2:** I’m thinking of integrating Google Maps SDK for Android since it aligns with our mobile stack; would you prefer that, or do we need to use a different provider?
**Answer:** Use Google Maps SDK for Android.

**Q3:** I assume we’ll plot nearby stores/merchants as markers sourced from the store data integration work. Should each marker include category info and the top recommended card in its callout, or keep it to store name only?
**Answer:** For this iteration just implement the Google Maps component; plotting store markers comes later.

**Q4:** Should we highlight the best-match store with a distinctive marker or treat all stores uniformly?
**Answer:** No store markers yet; focus solely on the map component and movement.

**Q5:** I’m assuming tapping a marker should open a bottom sheet or panel with card details. Is that the interaction you want, or should marker taps navigate elsewhere?
**Answer:** Not applicable for now since markers are deferred.

**Q6:** For filtering/search, do you want map-level controls (category chips, search bar) or should the map respond to filters set elsewhere?
**Answer:** Defer to future work; for now just implement the map component without filters.

**Q7:** To keep performance reasonable, I’m planning to cluster markers when zoomed out. Is clustering acceptable, or do you need every store visible at all zoom levels?
**Answer:** Marker behavior is out of scope for this iteration.

**Q8:** Are there any behaviors or data layers you explicitly want to exclude from this first iteration (e.g., indoor maps, route drawing, AR overlays)?
**Answer:** None at the moment beyond deferring markers and advanced layers.

### Existing Code to Reference
No similar existing features identified for reference.

### Follow-up Questions

**Follow-up 1:** I’m unable to open `map_current.png` from this environment. Could you briefly describe the layout elements you expect on that screen so I can capture them accurately in the requirements?
**Answer:** Provided `map_current.png`, which shows a full-screen map surface with a rounded search bar anchored near the top (search icon on the left, placeholder text "Search for a place or store...", overflow/menu icon on the right, drop shadow). No other overlays besides the Google watermark near the bottom-left.

## Visual Assets

### Files Provided:
- `map_current.png`: Screenshot of the current map screen showing a blank map background with only a top search bar and the Google watermark; no map tiles rendered yet.

### Visual Insights:
- Single rounded rectangle search bar with left search icon and right overflow menu, floating near the top edge without touching status bar.
- Map canvas takes the entire screen beneath the search bar; currently beige/blank due to missing map implementation.
- No markers, controls, or floating buttons besides the watermark, indicating a minimalist starting point.
- Fidelity: High-fidelity screenshot of the live app shell.

## Requirements Summary

### Functional Requirements
- Initialize and render a Google Maps view that auto-centers on the user’s current GPS position and displays the map tiles.
- Allow standard map gestures (pan, pinch-zoom, tilt if supported) without snapping back after user movement.
- Ensure location permissions and current-location acquisition flow exist or gracefully handle absence (e.g., prompt or fallback location).
- Maintain the existing floating search bar layout while ensuring map controls do not overlap it.

### Reusability Opportunities
- No explicit existing components identified, but consider matching the search bar styling already present on this screen when wiring behaviors later.

### Scope Boundaries
**In Scope:**
- Embedding Google Maps SDK for Android.
- Auto-centering on user location and supporting free-form navigation/gestures.
- Preserving the visual shell (search bar overlay and blank map area).

**Out of Scope:**
- Displaying store markers, clusters, or recommendation overlays.
- Marker tap interactions, filtering controls, or recommendation bottom sheets.
- Advanced layers (indoor maps, routes, AR) for this iteration.

### Technical Considerations
- Integrate Google Maps SDK and obtain/secure the necessary API key per Android guidelines.
- Handle runtime location permissions and fallbacks if permission is denied.
- Ensure lifecycle management (map view setup/teardown) follows Android best practices to avoid memory leaks.
- Prepare hooks for future store marker rendering but keep them disabled for now.
