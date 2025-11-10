package teamcherrypicker.com.ui.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            RecommendationSheetContent(cards = sampleCards)
        },
        sheetPeekHeight = 120.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            val singapore = LatLng(1.35, 103.87)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(singapore, 10f)
            }
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

            FloatingSearchBar(
                onSettingsClick = { /* Logic to open settings */ },
                navController = navController,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )

            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddCardScreen.route) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 110.dp) // Adjust to be above the peeked sheet
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Card")
            }
        }
    }
}

@Composable
fun FloatingSearchBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier.padding(start = 8.dp)
            )
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for a place or store...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Dark Mode") },
                        leadingIcon = { Icon(Icons.Filled.NightsStay, contentDescription = "Dark Mode") },
                        onClick = {
                            onToggleDarkMode()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Credit Card") },
                        leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = "Add Credit Card") },
                        onClick = {
                            navController.navigate(Screen.AddCardScreen.route)
                            showMenu = false
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Profile Settings") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Profile Settings") },
                        onClick = {
                            Log.d("Settings", "Profile Settings clicked")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Notification Settings") },
                        leadingIcon = { Icon(Icons.Filled.Notifications, contentDescription = "Notification Settings") },
                        onClick = {
                            Log.d("Settings", "Notification Settings clicked")
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationSheetContent(cards: List<RecommendedCard>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Recommendations for this area",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        cards.forEach { card ->
            RecommendedCardItem(card = card)
        }
    }
}

@Composable
fun RecommendedCardItem(card: RecommendedCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = card.matchRate.toFloat(),
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp,
                )
                Text(
                    text = "${(card.matchRate * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = card.cardName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                card.benefits.forEach { benefit ->
                    Text(
                        text = "• $benefit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
