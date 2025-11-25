# Task Group 1: Maps SDK & Layout Scaffolding

## Overview
**Task Reference:** Task Group 1 from `agent-os/specs/2025-11-25-implement-map-component/tasks.md`

**Implemented By:** android-engineer  
**Date:** 2025-11-25  
**Status:** ✅ Complete

### Scope
1.1 Author 2-4 smoke tests for the map/search layout.  
1.2 Add/verify Maps Compose, Play Services Maps, and secure manifest placeholders for the API key.  
1.3 Refactor `MainScreen` to host a dedicated `MapSurface` beneath the floating search bar with safe-area padding.  
1.4 Configure baseline `GoogleMap` settings (no zoom buttons, compass on, content description, placeholder camera).  
1.5 Run only the new smoke tests to validate the layout.

## Implementation Summary
- Added Google Maps Compose + Play Services Maps dependencies and wired a `GOOGLE_MAPS_API_KEY` manifest placeholder sourced from `local.properties`, ensuring no secrets are committed.  
- Introduced a reusable `MapSurface` composable that accepts `MapUiSettings`, padding, camera state, and a `LocalMapSurfaceRenderer` hook so tests can inject a fake renderer.  
- Refactored `MainScreen` to compute safe drawing insets, measure the floating search bar, and pass the resulting padding/content description down to `MapSurface`. The map now occupies the full area beneath the search bar while maintaining the mock layout.  
- Enabled compass-only `MapUiSettings`, initialized camera state over Singapore, and added placeholder mock markers to confirm interactions.  
- Authored three Compose UI smoke tests that render `MainScreen` with a fake `CardsViewModel` + fake map renderer, asserting that the map surface and floating search bar co-exist, stay visible after user input, and expose the accessibility description.

## Files Changed / Created
### Modified
- `app/build.gradle.kts` – secure API key placeholder, Maps dependencies, Robolectric resource access for tests.  
- `app/src/main/AndroidManifest.xml` – references `${GOOGLE_MAPS_API_KEY}` placeholder.  
- `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt` – integrates `MapSurface`, safe-area padding, mock markers, and `MapUiSettings`.  
- `app/src/main/res/values/strings.xml` – new `map_content_description` string.  
- `app/src/test/java/teamcherrypicker/com/MainScreenTest.kt` – Compose smoke tests with fake renderer + fake repository.

### Added
- `app/src/main/java/teamcherrypicker/com/ui/main/MapSurface.kt` – encapsulates Google Map rendering with testability hooks.

## Testing
- `./gradlew :app:testDebugUnitTest --tests teamcherrypicker.com.MainScreenTest`
  - Result: ✅ Passed (covers all three smoke tests).

## Standards & Guidelines
- **`agent-os/standards/frontend/components.md`** – `MapSurface` keeps parameters explicit and supports dependency injection for tests, aligning with the composable design guidance.  
- **`agent-os/standards/testing/test-writing.md`** – Smoke tests exercise observable UI behavior via semantics/tags without relying on implementation details; execution steps documented above.

## Known Issues & Follow-ups
- None. The layout scaffolding is stable and ready for Task Groups 2-4.
