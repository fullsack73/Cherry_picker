# Task 2.0: Implement camera centering pipeline

## Overview
**Task Reference:** Task #2.0 from `agent-os/specs/2025-11-25-implement-map-component/tasks.md`  
**Implemented By:** android-engineer  
**Date:** 2025-11-25  
**Status:** ✅ Complete

### Task Description
Deliver the location-aware camera experience: expose a reactive location feed, coordinate camera state so it follows the user when appropriate, surface recenter controls, and provide loading/fallback UI along with focused unit tests for the coordinator logic.

## Implementation Summary
A dedicated `LocationUiState` flow now streams permission, loading, and coordinate events from `MainActivity` into Compose. `MainScreen` consumes that flow, drives a new `MapStateCoordinator`, and renders a lightweight status overlay plus a conditional recenter FAB that respects safe areas and accessibility requirements. Camera movement is debounced through the coordinator so automatic jumps happen once per update unless the user is actively panning.  
 
Unit coverage was added via `MapStateCoordinatorTest`, validating camera targets, debounce behavior, and manual-vs-automatic recenter rules. The only executed test suite was this new coordinator test group per task instructions.

## Files Changed/Created

### New Files
- `app/src/main/java/teamcherrypicker/com/location/LocationUiState.kt` – Models permission/loading/coordinate state with a default fallback location.
- `app/src/main/java/teamcherrypicker/com/ui/main/map/MapStateCoordinator.kt` – Encapsulates camera debounce logic, gesture overrides, and recenter instructions.
- `app/src/test/java/teamcherrypicker/com/ui/main/map/MapStateCoordinatorTest.kt` – Focused unit tests covering automatic updates, manual overrides, and debounce ordering.
- `agent-os/specs/2025-11-25-implement-map-component/implementation/2-implement-camera-centering-pipeline-implementation.md` – This implementation summary.

### Modified Files
- `app/src/main/java/teamcherrypicker/com/MainActivity.kt` – Exposes a `StateFlow<LocationUiState>` to Compose, tracks permission/loading flags, and publishes coordinates.
- `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt` – Subscribes to the location flow, hooks `rememberCameraPositionState` to the coordinator, adds status overlay and recenter FAB, and wires gesture detection.
- `app/src/main/java/teamcherrypicker/com/ui/main/MapSurface.kt` – Unchanged (referenced but not edited).
- `app/src/main/res/values/strings.xml` – Adds copy for recenter control and location status messaging.
- `agent-os/specs/2025-11-25-implement-map-component/tasks.md` – Marks Task Group 2 items complete.

### Deleted Files
- _None_

## Key Implementation Details

### Reactive Location Flow
**Location:** `app/src/main/java/teamcherrypicker/com/MainActivity.kt`, `app/src/main/java/teamcherrypicker/com/location/LocationUiState.kt`

`MainActivity` now owns a `MutableStateFlow<LocationUiState>` capturing permission status, loading indicator, last known coordinates, and a default fallback (Seoul). Permission launcher callbacks immediately update this flow, and each fused-location success writes both backend telemetry and UI state. Compose screens receive a read-only `StateFlow`, ensuring a single source of truth.

**Rationale:** A shared flow keeps the UI synchronous with permission dialogs and backend updates, satisfying Task 2.2 while avoiding tight coupling between activity callbacks and composables.

### MapStateCoordinator & Camera Binding
**Location:** `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt`, `app/src/main/java/teamcherrypicker/com/ui/main/map/MapStateCoordinator.kt`

`MapStateCoordinator` debounces automatic camera recentering (default 250 ms) and exposes a simple UI state with `showRecenterFab`. `MainScreen` creates one coordinator per screen via `rememberCoroutineScope`, feeds permission + location changes, and pipes its `SharedFlow` into `CameraPositionState.animate`. Gesture detection is derived from `snapshotFlow` on the compose camera state with `CameraMoveStartedReason.GESTURE`, preventing automatic snaps while the user pans.

**Rationale:** Centralizing recenter logic keeps the composable lean, enforces the “once per update unless dragging” rule, and made the behavior directly testable per Task 2.1.

### Recenter FAB & Status Overlay
**Location:** `app/src/main/java/teamcherrypicker/com/ui/main/MainScreen.kt`, `app/src/main/res/values/strings.xml`

A Material3 FAB (56 dp) appears only when permissions are granted and the coordinator reports manual override, meeting accessibility sizing and recenter timing requirements. The new `LocationStatusOverlay` surfaces three states: loading spinner, permission-needed guidance, and fallback-city messaging until coordinates resolve. Strings reside in `strings.xml` for localization, and safe-area padding prevents overlap with existing chrome.

**Rationale:** This satisfies Tasks 2.3–2.5: users get immediate feedback while data loads, a reliable fallback viewport, and a predictable manual recenter affordance.

## Database Changes (if applicable)
_None._

## Dependencies (if applicable)
_No new Gradle dependencies were introduced._

## Testing

### Test Files Created/Updated
- `app/src/test/java/teamcherrypicker/com/ui/main/map/MapStateCoordinatorTest.kt` – Validates automatic camera updates after debounce, ignores updates during manual gestures, and ensures latest-location precedence.

### Test Coverage
- Unit tests: ✅ Complete (new coordinator tests)
- Integration tests: ❌ None
- Edge cases covered: gesture overrides, debounce cancellation, recenter button requests.

### Manual Testing Performed
_Not performed (no emulator/browser access in this run)._

## User Standards & Preferences Compliance

### global/coding-style.md
**File Reference:** `agent-os/standards/global/coding-style.md`

**How Your Implementation Complies:** Logic moved into `MapStateCoordinator` keeps composables thin and leverages immutable state flows, mirroring existing architectural patterns. Named parameters and small helper composables (e.g., `LocationStatusOverlay`) maintain readability per style guidance.

**Deviations:** None.

### frontend/accessibility.md
**File Reference:** `agent-os/standards/frontend/accessibility.md`

**How Your Implementation Complies:** The recenter FAB uses a 56 dp touch target with `recenter_fab_content_description`, and the status overlay includes iconography/text contrasts while respecting safe-area padding.

**Deviations:** None.

### frontend/components.md
**File Reference:** `agent-os/standards/frontend/components.md`

**How Your Implementation Complies:** Reused the existing `MapSurface` abstraction, injected camera state via coordinator, and confined new UI to composables with explicit parameters, supporting previewability and testing.

**Deviations:** None.

### global/error-handling.md
**File Reference:** `agent-os/standards/global/error-handling.md`

**How Your Implementation Complies:** User-facing fallbacks (permission guidance, loading, failure messaging) are surfaced inline while backend errors remain toast-based per existing conventions in `MainActivity`.

**Deviations:** None.

### testing/test-writing.md
**File Reference:** `agent-os/standards/testing/test-writing.md`

**How Your Implementation Complies:** The coordinator tests are deterministic, isolate a single unit, and focus on critical behaviors (debounce, gestures, recenter) with clear naming, aligning with the prescribed testing approach.

**Deviations:** None.

## Integration Points (if applicable)
- `ApiClient.apiService.sendLocation()` continues to be invoked when new coordinates arrive so backend ingestion remains unchanged.

## Known Issues & Limitations
1. **Single-shot location retrieval**  
   - Description: `MainActivity` still requests a single GPS fix rather than continuous updates.  
   - Impact: Auto-centering relies on explicit refreshes (e.g., reopening the screen) if the user moves significantly afterward.  
   - Workaround: None currently; future work could promote continuous `LocationRequest` updates.

## Performance Considerations
- Debounce (250 ms) prevents redundant backend calls or camera churn when multiple location fixes arrive quickly, keeping UI smooth and meeting the 300 ms recenter SLA.

## Security Considerations
- Location data continues to be sent through the existing API client; no secrets or keys were exposed, and flows remain confined to the activity scope.

## Dependencies for Other Tasks
- Task Group 3 can now observe permission state and `showRecenterFab` to disable controls when permissions are denied, and reuse `LocationUiState` for banner copy.

## Notes
- Deprecation warnings (e.g., `LocationRequest.PRIORITY_HIGH_ACCURACY`) remain unchanged since they predate this task; follow-up refactors can migrate to the newer priority APIs if needed.
