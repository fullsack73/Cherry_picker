# Task Breakdown: Map Component Implementation

## Overview
Total Tasks: 14
Assigned roles: android-engineer, testing-engineer

## Task List

### Android UI & SDK Setup

#### Task Group 1: Maps SDK & Layout Scaffolding
**Assigned implementer:** android-engineer  
**Dependencies:** None

- [x] 1.0 Prepare project for Google Maps SDK
  - [x] 1.1 Write 2-4 smoke tests (Compose screenshot or UI tests) ensuring the map container renders with the floating search bar still visible.
  - [x] 1.2 Add/verify Maps Compose and Play Services dependencies plus manifest placeholders for the API key (secure via `local.properties`).
  - [x] 1.3 Update `MainScreen` to use a dedicated `MapSurface` composable occupying space below `FloatingSearchBar`, ensuring padding respects safe areas.
  - [x] 1.4 Configure baseline `GoogleMap` component with `MapUiSettings` (disable zoom buttons, enable compass, set content description) and placeholder camera state.
  - [x] 1.5 Run ONLY the tests from 1.1 to confirm layout integrity.

**Acceptance Criteria:**
- Map renders without crashing even before location data loads.
- Floating search bar layout unchanged from mock (`planning/visuals/map_current.png`).
- API key securely referenced; no secrets committed.
- Tests from 1.1 pass.

#### Task Group 2: Location & Camera Management
**Assigned implementer:** android-engineer  
**Dependencies:** Task Group 1

- [x] 2.0 Implement camera centering pipeline
  - [x] 2.1 Write 3-5 focused unit tests for a new `MapStateCoordinator` (or equivalent) validating camera target updates, debounce behavior, and manual vs. automatic recenter rules.
  - [x] 2.2 Expose location flow from `MainActivity` (or shared ViewModel) so Compose layer observes permission + coordinate updates.
  - [x] 2.3 Wire `rememberCameraPositionState` to the coordinator, auto-centering on latest location with zoom ≈15 while allowing user gestures without immediate snap-back.
  - [x] 2.4 Add a floating recenter FAB (48dp min) that re-triggers camera animation to the user’s current position when tapped.
  - [x] 2.5 Show lightweight progress or placeholder state while awaiting first coordinate, plus fallback location if unavailable.
  - [x] 2.6 Run ONLY the tests from 2.1.

**Acceptance Criteria:**
- Camera animates to the user’s location once per update, unless user is actively dragging.
- Recenter control appears only when location permission granted and works within 300 ms after location known.
- Loading/fallback states display until coordinates resolve.
- Tests from 2.1 pass.

### Android UX & Resilience

#### Task Group 3: Permission & Error UX
**Assigned implementer:** android-engineer  
**Dependencies:** Task Group 2

- [ ] 3.0 Handle permission denial & overlays
  - [ ] 3.1 Write 2-4 UI tests validating that permission-denied banners or modals render and guide users to settings, and that fallback viewport shows.
  - [ ] 3.2 Create `LocationPermissionBanner` composable (or similar) referencing global accessibility standards (48dp targets, clear copy).
  - [ ] 3.3 Ensure map remains interactive around fallback coordinates even without permission; disable recenter FAB in this state.
  - [ ] 3.4 Provide user feedback (snackbar/toast) for location failures using global error-handling conventions.
  - [ ] 3.5 Run ONLY the tests from 3.1.

**Acceptance Criteria:**
- Denied-permission state clearly communicates next steps and prevents broken UI.
- Accessibility requirements met (content descriptions, touch targets).
- Error handling mirrors `standards/global/error-handling.md`.
- Tests from 3.1 pass.

### Testing & Verification

#### Task Group 4: Feature Test Consolidation
**Assigned implementer:** testing-engineer  
**Dependencies:** Task Groups 1-3

- [ ] 4.0 Validate end-to-end flows
  - [ ] 4.1 Review tests from Task Groups 1-3 (approx. 7-13 tests total) and note gaps.
  - [ ] 4.2 Design up to 6 additional strategic tests covering: permission toggling flow, recenter FAB interaction, fallback viewport behavior, and map rendering stability.
  - [ ] 4.3 Implement the additional tests (UI or integration) plus any fixtures needed; stay within the 10-test cap.
  - [ ] 4.4 Run ONLY the combined feature-specific tests (Groups 1-3 + new ones) and document results.

**Acceptance Criteria:**
- All feature-specific tests pass.
- Critical workflows (grant permission, deny permission, recenter) are covered.
- Total new tests authored by testing engineer ≤ 6, bringing feature total within 16-34 range.

## Execution Order
1. Task Group 1: Maps SDK & Layout Scaffolding
2. Task Group 2: Location & Camera Management
3. Task Group 3: Permission & Error UX
4. Task Group 4: Feature Test Consolidation
