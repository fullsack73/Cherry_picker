package teamcherrypicker.com.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import teamcherrypicker.com.data.RecommendedCard

@Composable
fun MainScreen() {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    var text by remember { mutableStateOf("Search...") }
    var isRecommendationExpanded by remember { mutableStateOf(false) }

    val sampleCards = listOf(
        RecommendedCard(
            cardName = "Chase Sapphire Preferred",
            matchRate = 0.95,
            benefits = listOf("5x points on travel", "3x points on dining")
        ),
        RecommendedCard(
            cardName = "Amex Gold",
            matchRate = 0.92,
            benefits = listOf("4x points on dining", "4x points at U.S. Supermarkets")
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize().testTag("map"),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = singapore),
                title = "Singapore",
                snippet = "Marker in Singapore"
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Search...") }
                )
                IconButton(onClick = { /*TODO*/ }) {
                    // Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }

            RecommendationList(
                cards = sampleCards,
                isExpanded = isRecommendationExpanded,
                onToggle = { isRecommendationExpanded = !isRecommendationExpanded }
            )
        }
    }
}
