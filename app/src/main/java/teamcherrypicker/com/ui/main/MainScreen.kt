package teamcherrypicker.com.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.lifecycle.viewmodel.compose.viewModel
import teamcherrypicker.com.R
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.CardBenefit
import teamcherrypicker.com.data.CardSummary
import teamcherrypicker.com.data.CardsMeta
import teamcherrypicker.com.data.Store
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState
import teamcherrypicker.com.ui.main.map.MapStateCoordinator
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.Cluster
import teamcherrypicker.com.ui.main.map.StoreClusterItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    locationUiStateFlow: StateFlow<LocationUiState>,
    cardsViewModel: CardsViewModel = viewModel(factory = CardsViewModel.provideFactory())
) {
    val cardsUiState by cardsViewModel.uiState.collectAsState()
    val benefitsState by cardsViewModel.benefitsState.collectAsState()
    val storesUiState by cardsViewModel.storesUiState.collectAsState()
    val locationUiState by locationUiStateFlow.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    // Filter State
    val categories = listOf("DINING", "CAFE", "SHOPPING")
    val selectedCategories = remember { mutableStateListOf<String>() }

    // Search Here State
    var lastSearchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showSearchHereButton by remember { mutableStateOf(false) }

    var selectedStore by remember { mutableStateOf<Store?>(null) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var clusterPreviewStores by remember { mutableStateOf<List<Store>>(emptyList()) }

    LaunchedEffect(cardsUiState.cards) {
        if (cardsUiState.cards.isEmpty()) {
            selectedCardId = null
        } else if (selectedCardId == null || cardsUiState.cards.none { it.id == selectedCardId }) {
            val firstCard = cardsUiState.cards.firstOrNull()
            if (firstCard != null) {
                selectedCardId = firstCard.id
                cardsViewModel.selectCard(firstCard.id)
            }
        }
    }

    // Use conditional peek height to hide sheet when no selection
    val effectivePeek = if (selectedStore != null && (cardsUiState.isLoading || cardsUiState.cards.isNotEmpty())) 280.dp else 0.dp

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LocationUiState.DEFAULT_FALLBACK_LOCATION, 12f)
    }

    // Update showSearchHereButton when camera moves
    LaunchedEffect(cameraPositionState.position.target) {
        lastSearchedLocation?.let { last ->
            val dist = FloatArray(1)
            android.location.Location.distanceBetween(
                last.latitude, last.longitude,
                cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude,
                dist
            )
            if (dist[0] > 500) { // Show if moved > 500m
                showSearchHereButton = true
            } else {
                showSearchHereButton = false
            }
        }
    }

    // Initial Search
    LaunchedEffect(locationUiState.lastKnownLocation) {
        if (lastSearchedLocation == null && locationUiState.lastKnownLocation != null) {
            val loc = locationUiState.lastKnownLocation!!
            Log.d(
                "MapDebug",
                "Initial load lat=${loc.latitude} lon=${loc.longitude}"
            )
            cardsViewModel.loadStores(loc.latitude, loc.longitude)
            lastSearchedLocation = loc
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val mapStateCoordinator = remember(coroutineScope) {
        MapStateCoordinator(scope = coroutineScope)
    }
    val coordinatorUiState by mapStateCoordinator.uiState.collectAsState()

    LaunchedEffect(locationUiState.permissionStatus) {
        mapStateCoordinator.onPermissionChanged(locationUiState.permissionStatus == LocationPermissionStatus.Granted)
    }

    LaunchedEffect(locationUiState.lastKnownLocation) {
        locationUiState.lastKnownLocation?.let { mapStateCoordinator.onLocationUpdate(it) }
    }

    LaunchedEffect(mapStateCoordinator) {
        mapStateCoordinator.cameraUpdates.collect { instruction ->
            val update = CameraUpdateFactory.newLatLngZoom(instruction.target, instruction.zoom)
            cameraPositionState.animate(update)
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving to cameraPositionState.cameraMoveStartedReason }
            .distinctUntilChanged()
            .collect { (isMoving, reason) ->
                if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                    mapStateCoordinator.onUserGestureStarted()
                } else if (!isMoving) {
                    mapStateCoordinator.onCameraIdle()
                }
            }
    }

    LaunchedEffect(locationUiState.errorMessage) {
        locationUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            if (selectedStore != null) {
                RecommendationSheetContent(
                    cards = cardsUiState.cards,
                    meta = cardsUiState.meta,
                    selectedTitle = selectedStore!!.name,
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
            MapSurface(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                mapUiSettings = mapUiSettings,
                contentDescription = mapContentDescription,
                contentPadding = mapContentPadding
            ) {
                val clusterItems = remember(storesUiState.stores) {
                    storesUiState.stores.map { StoreClusterItem(it) }
                }

                LaunchedEffect(clusterItems) {
                    val sample = if (clusterItems.isEmpty()) {
                        "none"
                    } else {
                        clusterItems.take(5).joinToString { item ->
                            "${item.store.id}:${item.store.name}@${item.store.latitude},${item.store.longitude}"
                        }
                    }
                    Log.d("MapDebug", "Cluster update count=${clusterItems.size} sample=$sample")
                }

                Clustering(
                    items = clusterItems,
                    onClusterClick = { cluster: Cluster<StoreClusterItem> ->
                        if (cluster.items.size <= CLUSTER_DETAIL_THRESHOLD) {
                            clusterPreviewStores = cluster.items.map { it.store }
                            true
                        } else {
                            false
                        }
                    },
                    onClusterItemClick = { item ->
                        Log.d("MapDebug", "Cluster marker tapped id=${item.store.id} name=${item.store.name}")
                        clusterPreviewStores = emptyList()
                        selectedStore = item.store
                        true
                    },
                    clusterItemContent = { item ->
                        StoreMarkerIcon(
                            category = item.store.normalizedCategory,
                            storeName = item.store.name,
                            calloutText = item.store.name.takeIf { selectedStore?.id == item.store.id }
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = clusterPreviewStores.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1.5f)
            ) {
                ClusterPreviewCard(
                    stores = clusterPreviewStores,
                    onStoreSelected = { store ->
                        selectedStore = store
                        clusterPreviewStores = emptyList()
                    },
                    onDismiss = { clusterPreviewStores = emptyList() }
                )
            }

                val filterRowTop = safeDrawingPadding.calculateTopPadding() + floatingSearchBarHeight + 8.dp
                val filterRowEnd = safeDrawingPadding.calculateEndPadding(layoutDirection) + 24.dp

            CategoryQuickFilters(
                categories = categories,
                selectedCategories = selectedCategories,
                isLoading = storesUiState.isLoading,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = filterRowTop, end = filterRowEnd)
                    .zIndex(1f)
            ) { category ->
                val isSelected = selectedCategories.contains(category)
                if (isSelected) selectedCategories.remove(category) else selectedCategories.add(category)

                val target = cameraPositionState.position.target
                Log.d(
                    "MapDebug",
                    "Category toggled=$category selected=${selectedCategories.joinToString()} lat=${target.latitude} lon=${target.longitude}"
                )
                cardsViewModel.loadStores(
                    target.latitude,
                    target.longitude,
                    categories = selectedCategories.toList().takeIf { it.isNotEmpty() }
                )
                lastSearchedLocation = target
                showSearchHereButton = false
            }

            // Search Here Button
            AnimatedVisibility(
                visible = showSearchHereButton,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = mapTopPadding + 60.dp)
                    .zIndex(1f)
            ) {
                Button(
                    onClick = {
                        val target = cameraPositionState.position.target
                        Log.d(
                            "MapDebug",
                            "Search Here triggered lat=${target.latitude} lon=${target.longitude} categories=${selectedCategories.joinToString()}"
                        )
                        cardsViewModel.loadStores(
                            target.latitude,
                            target.longitude,
                            categories = selectedCategories.toList().takeIf { it.isNotEmpty() }
                        )
                        lastSearchedLocation = target
                        showSearchHereButton = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Search Here")
                }
            }

            if (coordinatorUiState.isPermissionDenied) {
                LocationPermissionBanner(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(mapContentPadding)
                        .padding(bottom = 16.dp)
                )
            } else {
                LocationStatusOverlay(
                    locationUiState = locationUiState,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(mapContentPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            AnimatedVisibility(
                visible = coordinatorUiState.showRecenterFab,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = safeDrawingPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = safeDrawingPadding.calculateBottomPadding() + 16.dp
                    )
            ) {
                FloatingActionButton(
                    onClick = { mapStateCoordinator.onRecenterRequest() },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.recenter_fab_content_description)
                    )
                }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClusterPreviewCard(
    stores: List<Store>,
    onStoreSelected: (Store) -> Unit,
    onDismiss: () -> Unit
) {
    if (stores.isEmpty()) return

    Surface(
        tonalElevation = 12.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 340.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cluster preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stores.size} places",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close cluster preview"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = stores.size > 9
            ) {
                items(stores, key = { it.id }) { store ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStoreSelected(store) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StoreMarkerIcon(
                            category = store.normalizedCategory,
                            storeName = store.name,
                            markerSize = 52.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = store.name,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryQuickFilters(
    categories: List<String>,
    selectedCategories: List<String>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onToggleCategory: (String) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategories.contains(category)
            CategoryQuickFilterChip(
                category = category,
                isSelected = isSelected,
                onClick = { onToggleCategory(category) }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun CategoryQuickFilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val style = remember(category) { markerStyleFor(category) }
    val shape = RoundedCornerShape(50)
    val baseBackground = Color.White
    val background = if (isSelected) style.backgroundColor.copy(alpha = 0.12f) else baseBackground
    val borderColor = if (isSelected) style.backgroundColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(background, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = category.toCategoryLabel(),
            tint = style.backgroundColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = category.toCategoryLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private const val CLUSTER_DETAIL_THRESHOLD = 30

private data class StoreMarkerStyle(
    val backgroundColor: Color,
    val innerBorderColor: Color,
    val ringColor: Color,
    val icon: ImageVector,
    val contentDescription: String
)

private fun markerStyleFor(category: String): StoreMarkerStyle {
    return when (category.uppercase(Locale.US)) {
        "CAFE" -> StoreMarkerStyle(
            backgroundColor = Color(0xFFFFB74D),
            innerBorderColor = Color(0xFFFFE0B2),
            ringColor = Color(0xFFFFCC80),
            icon = Icons.Rounded.LocalCafe,
            contentDescription = "Cafe"
        )
        "DINING" -> StoreMarkerStyle(
            backgroundColor = Color(0xFFFF8A65),
            innerBorderColor = Color(0xFFFFD0BD),
            ringColor = Color(0xFFFFAB91),
            icon = Icons.Rounded.Restaurant,
            contentDescription = "Dining"
        )
        "SHOPPING" -> StoreMarkerStyle(
            backgroundColor = Color(0xFFEC407A),
            innerBorderColor = Color(0xFFF8BBD0),
            ringColor = Color(0xFFF48FB1),
            icon = Icons.Rounded.ShoppingBag,
            contentDescription = "Shopping"
        )
        "CONVENIENCE" -> StoreMarkerStyle(
            backgroundColor = Color(0xFF42A5F5),
            innerBorderColor = Color(0xFFBBDEFB),
            ringColor = Color(0xFF90CAF9),
            icon = Icons.Rounded.Storefront,
            contentDescription = "Convenience store"
        )
        else -> StoreMarkerStyle(
            backgroundColor = Color(0xFF1E88E5),
            innerBorderColor = Color(0xFFBBDEFB),
            ringColor = Color(0xFF90CAF9),
            icon = Icons.Rounded.Place,
            contentDescription = "Store"
        )
    }
}

@Composable
private fun StoreMarkerIcon(
    category: String,
    storeName: String = "",
    markerSize: Dp = 56.dp,
    calloutText: String? = null
) {
    val style = remember(category) { markerStyleFor(category) }
    val density = LocalDensity.current
    val ringWidth = (markerSize.value * 0.04f).coerceAtLeast(1f).dp
    val haloPaddingDp = markerSize * 0.12f
    val badgeSize = markerSize * 0.78f
    val coreSize = badgeSize * 0.77f
    val iconSize = coreSize * 0.58f
    val glowElevation = (markerSize.value * 0.18f).coerceAtLeast(2f).dp
    val borderWidth = (markerSize.value * 0.035f).coerceAtLeast(1.5f).dp
    val ringWidthPx = with(density) { ringWidth.toPx() }
    val haloPaddingPx = with(density) { haloPaddingDp.toPx() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!calloutText.isNullOrBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    color = Color.White,
                ) {
                    Text(
                        text = calloutText,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .widthIn(max = 180.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1F2D3D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Canvas(
                    modifier = Modifier
                        .width(16.dp)
                        .height(8.dp)
                ) {
                    val triangle = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(triangle, Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Box(
            modifier = Modifier.size(markerSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f

                // Soft halo + dotted ring mimic the mock's layered marker target.
                drawCircle(
                    color = style.ringColor.copy(alpha = 0.18f),
                    radius = radius
                )
                drawCircle(
                    color = style.ringColor.copy(alpha = 0.45f),
                    radius = radius - haloPaddingPx,
                    style = Stroke(
                        width = ringWidthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(ringWidthPx * 2f, ringWidthPx * 1.4f))
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .shadow(glowElevation, CircleShape, clip = false)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(coreSize)
                        .background(style.backgroundColor, CircleShape)
                        .border(borderWidth, style.innerBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = storeName.ifBlank { style.contentDescription },
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationStatusOverlay(
    locationUiState: LocationUiState,
    modifier: Modifier = Modifier
) {
    val fallbackCity = stringResource(R.string.location_status_fallback_city)
    val statusText = when {
        locationUiState.isLoading -> stringResource(R.string.location_status_loading)
        !locationUiState.hasLocationFix && locationUiState.permissionStatus == LocationPermissionStatus.Denied ->
            stringResource(R.string.location_status_permission_denied)
        !locationUiState.hasLocationFix ->
            stringResource(R.string.location_status_fallback, fallbackCity)
        else -> null
    }

    if (statusText == null) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (locationUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null
                )
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall
            )
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
