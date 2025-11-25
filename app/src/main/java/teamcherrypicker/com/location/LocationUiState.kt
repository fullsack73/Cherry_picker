package teamcherrypicker.com.location

import com.google.android.gms.maps.model.LatLng

enum class LocationPermissionStatus {
    Unknown,
    Granted,
    Denied
}

data class LocationUiState(
    val permissionStatus: LocationPermissionStatus = LocationPermissionStatus.Unknown,
    val isLoading: Boolean = false,
    val lastKnownLocation: LatLng? = null,
    val fallbackLocation: LatLng = DEFAULT_FALLBACK_LOCATION,
    val lastUpdatedMillis: Long? = null
) {
    val hasLocationFix: Boolean get() = lastKnownLocation != null
    val resolvedLocation: LatLng get() = lastKnownLocation ?: fallbackLocation

    companion object {
        val DEFAULT_FALLBACK_LOCATION: LatLng = LatLng(37.5665, 126.9780)
    }
}
