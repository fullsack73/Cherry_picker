package teamcherrypicker.com.ui.main.map

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapStateCoordinatorTest {
    @Test
    fun automaticLocationUpdate_emitsCameraInstructionAfterDebounce() = runTest {
        val coordinator = MapStateCoordinator(scope = this, debounceMillis = 100L, targetZoom = 15f)
        coordinator.onPermissionChanged(true)
        val instructionDeferred = async { coordinator.cameraUpdates.first() }

        coordinator.onLocationUpdate(LatLng(37.0, -122.0))
        advanceTimeBy(150L)

        val instruction = instructionDeferred.await()
        assertEquals(LatLng(37.0, -122.0), instruction.target)
        assertEquals(MapStateCoordinator.CameraUpdateReason.AUTOMATIC, instruction.reason)
        assertEquals(15f, instruction.zoom)
    }

    @Test
    fun manualOverride_blocksAutomaticUpdatesUntilRecenterRequested() = runTest {
        val coordinator = MapStateCoordinator(scope = this, debounceMillis = 100L, targetZoom = 15f)
        coordinator.onPermissionChanged(true)
        val updates = mutableListOf<MapStateCoordinator.CameraUpdateInstruction>()
        val collectJob: Job = launch { coordinator.cameraUpdates.collect { updates += it } }

        coordinator.onLocationUpdate(LatLng(1.0, 1.0))
        advanceUntilIdle()
        assertEquals(1, updates.size)

        coordinator.onUserGestureStarted()
        assertTrue(coordinator.uiState.value.showRecenterFab)

        coordinator.onLocationUpdate(LatLng(2.0, 2.0))
        advanceTimeBy(200L)
        assertEquals("No new updates should be emitted while manual override is true", 1, updates.size)

        coordinator.onRecenterRequest()
        advanceUntilIdle()
        assertEquals(2, updates.size)
        assertEquals(MapStateCoordinator.CameraUpdateReason.RECENTER_TAP, updates.last().reason)
        assertFalse(coordinator.uiState.value.showRecenterFab)

        collectJob.cancel()
    }

    @Test
    fun successiveLocationUpdates_emitOnlyLatestAfterDebounceWindow() = runTest {
        val coordinator = MapStateCoordinator(scope = this, debounceMillis = 100L, targetZoom = 15f)
        coordinator.onPermissionChanged(true)
        val emitted = mutableListOf<MapStateCoordinator.CameraUpdateInstruction>()
        val collectJob: Job = launch { coordinator.cameraUpdates.collect { emitted += it } }

        coordinator.onLocationUpdate(LatLng(10.0, 10.0))
        advanceTimeBy(80L)
        coordinator.onLocationUpdate(LatLng(20.0, 20.0))
        advanceTimeBy(120L)

        assertEquals(1, emitted.size)
        assertEquals(LatLng(20.0, 20.0), emitted.first().target)

        collectJob.cancel()
    }
}
