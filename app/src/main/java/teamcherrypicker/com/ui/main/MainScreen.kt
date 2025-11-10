package teamcherrypicker.com.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.RecommendedCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    var text by remember { mutableStateOf("Search...") }
    var isRecommendationExpanded by remember { mutableStateOf(false) }
    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search...") }
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SettingsScreen.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isRecommendationExpanded) {
                FloatingActionButton(onClick = { navController.navigate(Screen.AddCardScreen.route) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Card")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().testTag("map"),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings
            ) {
                Marker(
                    state = MarkerState(position = singapore),
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                RecommendationList(
                    cards = sampleCards,
                    isExpanded = isRecommendationExpanded,
                    onToggle = { isRecommendationExpanded = !isRecommendationExpanded }
                )
            }
        }
    }
}
