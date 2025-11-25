package teamcherrypicker.com.ui.main

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.lifecycle.viewmodel.compose.viewModel
import teamcherrypicker.com.R
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.CardBenefit
import teamcherrypicker.com.data.CardSummary
import teamcherrypicker.com.data.CardsMeta

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    cardsViewModel: CardsViewModel = viewModel(factory = CardsViewModel.provideFactory())
) {
    val cardsUiState by cardsViewModel.uiState.collectAsState()
    val benefitsState by cardsViewModel.benefitsState.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true
        )
    }
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    var floatingSearchBarHeightPx by remember { mutableStateOf(0) }
    val floatingSearchBarHeight = if (floatingSearchBarHeightPx == 0) 0.dp else with(density) {
        floatingSearchBarHeightPx.toDp()
    }
    val mapTopPadding = safeDrawingPadding.calculateTopPadding() +
        floatingSearchBarHeight + MapSurfaceDefaults.searchBarClearance
    val mapContentPadding = PaddingValues(
        start = safeDrawingPadding.calculateStartPadding(layoutDirection),
        top = mapTopPadding,
        end = safeDrawingPadding.calculateEndPadding(layoutDirection),
        bottom = safeDrawingPadding.calculateBottomPadding()
    )
    val mapContentDescription = stringResource(R.string.map_content_description)

    // Add: simple model for mock store markers and selection state
    data class StoreMarker(val id: String, val position: LatLng, val title: String)

    val mockStoreMarkers = listOf(
        StoreMarker("s1", LatLng(1.3521, 103.8198), "Orchard Road - Mock Store"),
        StoreMarker("s2", LatLng(1.2833, 103.8600), "Marina Bay - Mock Store"),
        StoreMarker("s3", LatLng(1.3000, 103.8000), "Bugis - Mock Store")
    )

    var selectedMarker by remember { mutableStateOf<StoreMarker?>(null) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(cardsUiState.cards) {
        if (cardsUiState.cards.isEmpty()) {
            selectedCardId = null
        } else if (selectedCardId == null || cardsUiState.cards.none { it.id == selectedCardId }) {
            val firstCard = cardsUiState.cards.first()
            selectedCardId = firstCard.id
            cardsViewModel.selectCard(firstCard.id)
        }
    }

    // Use conditional peek height to hide sheet when no selection
    val effectivePeek = if (selectedMarker != null && (cardsUiState.isLoading || cardsUiState.cards.isNotEmpty())) 280.dp else 0.dp

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            if (selectedMarker != null) {
                RecommendationSheetContent(
                    cards = cardsUiState.cards,
                    meta = cardsUiState.meta,
                    selectedTitle = selectedMarker!!.title,
                    selectedCardId = selectedCardId,
                    onSelectCard = { card ->
                        selectedCardId = card.id
                        cardsViewModel.selectCard(card.id)
                    },
                    benefitsState = benefitsState,
                    isLoading = cardsUiState.isLoading,
                    errorMessage = cardsUiState.errorMessage,
                    onRetry = { cardsViewModel.loadCards() }
                )
            } else {
                // Completely empty content when no marker selected
                Box(modifier = Modifier.height(0.dp))
            }
        },
        sheetPeekHeight = effectivePeek,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val singapore = LatLng(1.35, 103.87)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(singapore, 10f)
            }
            MapSurface(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                mapUiSettings = mapUiSettings,
                contentDescription = mapContentDescription,
                contentPadding = mapContentPadding
            ) {
                // Render mock store markers and wire up clicks to set selectedMarker
                mockStoreMarkers.forEach { store ->
                    Marker(
                        state = MarkerState(position = store.position),
                        title = store.title,
                        snippet = "Tap for recommendations",
                        onClick = {
                            selectedMarker = store
                            true
                        }
                    )
                }

                Marker(
                    state = MarkerState(position = singapore),
                    title = "Singapore",
                    snippet = "Marker in Singapore",
                    onClick = {
                        selectedMarker = null
                        true
                    }
                )
            }

            FloatingSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = safeDrawingPadding.calculateTopPadding())
                    .zIndex(1f)
                    .onGloballyPositioned { coordinates ->
                        floatingSearchBarHeightPx = coordinates.size.height
                    },
                onSettingsClick = { /* Logic to open settings */ },
                navController = navController,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onSearch = { query ->
                    selectedCardId = null
                    cardsViewModel.loadCards(category = query.takeIf { it.isNotBlank() })
                }
            )

            // Removed the bottom-right "+" FloatingActionButton per request
        }
    }
}

@Composable
fun FloatingSearchBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onSearch: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("floatingSearchBar")
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(text) }),
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
fun RecommendationSheetContent(
    cards: List<CardSummary>,
    meta: CardsMeta?,
    selectedTitle: String,
    selectedCardId: Int?,
    onSelectCard: (CardSummary) -> Unit,
    benefitsState: BenefitsUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
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
            "Recommendations for $selectedTitle",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        meta?.let {
            Text(
                text = buildString {
                    append("Last refreshed: ")
                    append(it.lastRefreshedAt ?: "Unknown")
                    if (!it.dataSource.isNullOrBlank()) {
                        append(" • Source: ")
                        append(it.dataSource)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            cards.isEmpty() -> {
                Text(
                    text = "No cards available yet. Try refreshing soon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                cards.forEach { card ->
                    CardSummaryItem(
                        card = card,
                        isSelected = card.id == selectedCardId,
                        onClick = { onSelectCard(card) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                val isLoadingBenefits = benefitsState.isLoading && benefitsState.cardId == selectedCardId
                when {
                    selectedCardId == null -> {
                        Text(
                            text = "Select a card to view benefits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    isLoadingBenefits -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    benefitsState.errorMessage != null && benefitsState.cardId == selectedCardId -> {
                        Text(
                            text = benefitsState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    benefitsState.benefits.isEmpty() -> {
                        Text(
                            text = "No benefits found for this card.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        BenefitsList(benefits = benefitsState.benefits)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardSummaryItem(card: CardSummary, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val background = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(width = 1.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(card.issuer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (card.normalizedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    card.normalizedCategories.forEach { category ->
                        AssistChip(onClick = { onClick() }, label = { Text(category.toCategoryLabel()) })
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitsList(benefits: List<CardBenefit>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        benefits.forEach { benefit ->
            Column {
                Text(
                    text = benefit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = benefit.normalizedCategory.toCategoryLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
