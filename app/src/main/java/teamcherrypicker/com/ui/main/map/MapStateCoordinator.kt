package teamcherrypicker.com.ui.main.map

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapStateCoordinator(
    private val scope: CoroutineScope,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
    private val targetZoom: Float = DEFAULT_TARGET_ZOOM
) {

    data class UiState(
        val showRecenterFab: Boolean = false,
        val lastLocation: LatLng? = null
    )

    data class CameraUpdateInstruction(
        val target: LatLng,
        val zoom: Float,
        val reason: CameraUpdateReason
    )

    enum class CameraUpdateReason {
        AUTOMATIC,
        RECENTER_TAP
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _cameraUpdates = MutableSharedFlow<CameraUpdateInstruction>(extraBufferCapacity = 1)
    val cameraUpdates: SharedFlow<CameraUpdateInstruction> = _cameraUpdates.asSharedFlow()

    private var permissionGranted = false
    private var manualOverride = false
    private var lastLocation: LatLng? = null
    private var debounceJob: Job? = null

    fun onPermissionChanged(granted: Boolean) {
        permissionGranted = granted
        if (!granted) {
            manualOverride = false
            debounceJob?.cancel()
            debounceJob = null
        }
        updateUiState()
    }

    fun onLocationUpdate(location: LatLng) {
        lastLocation = location
        updateUiState()
        if (manualOverride || !permissionGranted) return

        debounceJob?.cancel()
        debounceJob = null
        debounceJob = scope.launch {
            delay(debounceMillis)
            _cameraUpdates.emit(
                CameraUpdateInstruction(
                    target = location,
                    zoom = targetZoom,
                    reason = CameraUpdateReason.AUTOMATIC
                )
            )
        }
    }

    fun onUserGestureStarted() {
        debounceJob?.cancel()
        if (!manualOverride) {
            manualOverride = true
            updateUiState()
        }
    }

    fun onCameraIdle() {
        // Intentionally left blank; we only care about gesture transitions for manual override.
    }

    fun onRecenterRequest() {
        val target = lastLocation ?: return
        manualOverride = false
        updateUiState()
        scope.launch {
            _cameraUpdates.emit(
                CameraUpdateInstruction(
                    target = target,
                    zoom = targetZoom,
                    reason = CameraUpdateReason.RECENTER_TAP
                )
            )
        }
    }

    private fun updateUiState() {
        val shouldShowRecenter = permissionGranted && manualOverride && lastLocation != null
        _uiState.value = UiState(
            showRecenterFab = shouldShowRecenter,
            lastLocation = lastLocation
        )
    }

    companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 250L
        const val DEFAULT_TARGET_ZOOM = 15f
    }
}
