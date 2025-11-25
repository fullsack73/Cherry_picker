package teamcherrypicker.com.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings

internal typealias MapSurfaceRenderer = @Composable (
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    mapUiSettings: MapUiSettings,
    contentDescription: String,
    contentPadding: PaddingValues,
    mapContent: @Composable () -> Unit
) -> Unit

internal val LocalMapSurfaceRenderer = staticCompositionLocalOf<MapSurfaceRenderer> {
    { modifier, cameraPositionState, mapUiSettings, contentDescription, contentPadding, mapContent ->
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            contentDescription = contentDescription,
            contentPadding = contentPadding,
            content = mapContent
        )
    }
}

@Composable
fun MapSurface(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    mapUiSettings: MapUiSettings,
    contentDescription: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    mapContent: @Composable () -> Unit = {}
) {
    val renderer = LocalMapSurfaceRenderer.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mapSurface")
    ) {
        renderer(
            Modifier
                .fillMaxSize()
                .testTag("map"),
            cameraPositionState,
            mapUiSettings,
            contentDescription,
            contentPadding,
            mapContent
        )
    }
}

object MapSurfaceDefaults {
    /**
     * Extra spacing that keeps map UI chrome (compass, attribution) from overlapping the floating search bar.
     */
    val searchBarClearance = 12.dp
}
