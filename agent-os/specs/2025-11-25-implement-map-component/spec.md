# Specification: Map Component Implementation

## Goal
Deliver a fully interactive Google Maps surface within the map screen that auto-centers on the user’s location, respects the existing floating search bar layout, and allows users to freely explore nearby areas.

## User Stories
- As a location-aware shopper, I want the map to center on my current position so I immediately understand where I am in relation to nearby stores.
- As a curious user, I want to freely pan and zoom the map without it snapping back so I can inspect other neighborhoods.
- As a privacy-conscious user, I want clear prompts and fallbacks when location permission is denied so the app still behaves predictably.

## Core Requirements
### Functional Requirements
- Render a Google Maps view (via Maps Compose) that fills the screen beneath the floating search bar and displays tiles immediately after initialization.
- Automatically position the camera on the user’s most recent GPS fix (from `MainActivity.kt`) with a reasonable default zoom (≈15) and allow camera gestures (pan/zoom/tilt) without forced recentering.
- Surface current-location availability (loading spinner or subtle toast) while coordinates are being fetched; if unavailable, fall back to the last known location or a neutral default (e.g., Seoul)
- Keep the existing `FloatingSearchBar` overlay intact, ensuring map UI controls don’t overlap it (adjust map padding if needed).
- Provide an unobtrusive control (e.g., FAB in bottom-right) that recenters the map on the user after manual panning, but do not show store markers yet.
- Gracefully handle lack of permissions by showing an inline state (text overlay or modal) guiding users to enable location.

### Non-Functional Requirements
- Map tiles should appear within 2 seconds on broadband connections; recenter action should respond within 300 ms once location is known.
- Follow accessibility guidance from `standards/frontend/accessibility.md`: minimum 48dp touch target for recenter control, descriptive `contentDescription`s for screen readers.
- Preserve global styling conventions (`standards/global/coding-style.md`): Compose code organized into small composables with clear parameters, and error handling via user-friendly toasts/snackbars.
- Secure the Google Maps API key via gradle `local.properties` or manifest placeholders; never hardcode keys in source.

## Visual Design
- Mockup reference: `planning/visuals/map_current.png`
- Key UI elements: floating rounded search bar near top, full-bleed map canvas, Google watermark at bottom-left, optional recenter control near bottom-right that matches existing FAB styling.
- Responsive considerations: portrait mobile first; ensure safe-area padding for devices with camera cutouts and future landscape rotation support.

## Reusable Components
### Existing Code to Leverage
- Components: `FloatingSearchBar` in `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt` already matches the visual shell; reuse as-is.
- Services: Location retrieval & permission handling in `app/src/main/java/teamcherrypicker/com/MainActivity.kt` (fused location client, permission launcher) can provide coordinates and permission status hooks.
- Patterns: Existing `GoogleMap` scaffolding inside `MainScreen` (lines 102-170) demonstrates Compose Maps setup and UI settings – adapt/extend instead of rebuilding.

### New Components Required
- `MapStateCoordinator` (Compose state holder or ViewModel method) to manage camera position, loading state, and recenter requests without introducing markers yet.
- `LocationPermissionBanner` lightweight composable for denied-permission messaging; needed because current UI only toasts errors.
- Reason: No existing composable currently separates map state & permission UI, and recenter control does not exist.

## Technical Approach
- Database: No schema changes required; map view consumes runtime location only.
- API: Continue using `ApiClient.apiService.sendLocation()` when coordinates update so backend stays informed; ensure calls are debounced to avoid spam.
- Frontend: 
  - Integrate `GoogleMap` from `com.google.maps.android:maps-compose` with `rememberCameraPositionState` tied to user location state.
  - Hook into `MainActivity` location updates via shared `ViewModel` or `rememberUpdatedState`, exposing flows for coordinates and permission state.
  - Implement gesture-friendly map padding and `MapUiSettings` (disable default zoom controls, enable compass) to avoid overlap with search bar.
  - Add recenter FAB using Material3 `FloatingActionButton` anchored above Google watermark.
- Testing: 
  - Unit test `MapStateCoordinator` to ensure camera target updates when new coordinates arrive and ignores updates while user panning (using fake state).
  - Instrumentation test verifying the `GoogleMap` composable renders (via `SemanticsNodeInteractions` and `testTag("map")`) and recenter FAB appears when permission granted.
  - Manual test checklist covering permission grant/denial flows per `standards/testing/test-writing.md`.

## Out of Scope
- Rendering store markers, clustering, or recommendation overlays.
- Bottom sheet interactions tied to marker taps.
- Advanced map layers (traffic, indoor, AR) or routing visualizations.
- Search suggestions or autocomplete integration for the search bar.

## Success Criteria
- Map loads with tiles visible and camera centered on current location (or fallback) on first open.
- User can pan/zoom freely and tap a recenter control to jump back to current location without restarting screen.
- When location permission is denied, the user sees actionable guidance and the map remains functional around a fallback viewport.
- No crashes or ANRs observed during 5-minute exploratory testing while repeatedly toggling permissions and moving the camera.
