package teamcherrypicker.com.ui.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    var text by remember { mutableStateOf("Search...") }
    var isRecommendationExpanded by remember { mutableStateOf(false) }
    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }
    var showMenu by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Dark Mode") },
                            onClick = {
                                onToggleDarkMode()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Credit Card") },
                            onClick = {
                                navController.navigate(Screen.AddCardScreen.route)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Profile Settings") },
                            onClick = {
                                Log.d("Settings", "Profile Settings clicked")
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Notification Settings") },
                            onClick = {
                                Log.d("Settings", "Notification Settings clicked")
                                showMenu = false
                            }
                        )
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
