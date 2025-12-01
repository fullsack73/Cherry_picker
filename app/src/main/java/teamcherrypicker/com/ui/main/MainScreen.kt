package teamcherrypicker.com.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import teamcherrypicker.com.R
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.OwnedCardsStore
import teamcherrypicker.com.data.RecommendationCard
import teamcherrypicker.com.data.RecommendationScoreSource
import teamcherrypicker.com.data.Store
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState
import teamcherrypicker.com.ui.main.map.MapStateCoordinator
import kotlinx.coroutines.flow.StateFlow
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
    val storesUiState by cardsViewModel.storesUiState.collectAsState()
    val locationUiState by locationUiStateFlow.collectAsState()
    val storeSearchState by cardsViewModel.storeSearchState.collectAsState()
    val context = LocalContext.current
    val ownedCardsStore = remember { OwnedCardsStore.from(context) }
    val recommendationViewModel: RecommendationViewModel = viewModel(
        factory = RecommendationViewModel.provideFactory(ownedCardsStore = ownedCardsStore)
    )
    val recommendationUiState by recommendationViewModel.uiState.collectAsState()

    val bottomSheetState = rememberStandardBottomSheetState(
        skipHiddenState = false,
        initialValue = SheetValue.Hidden
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val snackbarHostState = remember { SnackbarHostState() }

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
    var searchAnchorMarkerDescriptor by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var userLocationMarkerDescriptor by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(context) {
        runCatching {
            MapsInitializer.initialize(context)
            searchAnchorMarkerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            userLocationMarkerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
        }.onFailure { error ->
            Log.e("MapDebug", "Failed to init map descriptor", error)
        }
    }

    // Filter State
    val categories = listOf("DINING", "CAFE", "SHOPPING")
    val selectedCategories = remember { mutableStateListOf<String>() }

    // Search Here State
    var lastSearchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showSearchHereButton by remember { mutableStateOf(false) }

    var selectedStore by remember { mutableStateOf<Store?>(null) }
    var clusterPreviewStores by remember { mutableStateOf<List<Store>>(emptyList()) }
    var appliedSearchStoreId by remember { mutableStateOf<Int?>(null) }

    fun handleStoreSelection(store: Store) {
        val isSameStore = selectedStore?.id == store.id
        selectedStore = store
        if (!isSameStore) {
            recommendationViewModel.onStoreSelected(store)
        }
    }

    val hasActiveStoreSearch = storeSearchState.results.isNotEmpty()
    val displayedStores = if (hasActiveStoreSearch) {
        storeSearchState.results
    } else {
        storesUiState.stores
    }

    // Use conditional peek height to hide sheet when no selection
    val shouldShowSheet = selectedStore != null &&
        (recommendationUiState.isLoading || recommendationUiState.cards.isNotEmpty() || recommendationUiState.errorMessage != null)
    val effectivePeek = if (shouldShowSheet) 320.dp else 0.dp

    LaunchedEffect(shouldShowSheet, selectedStore?.id) {
        if (shouldShowSheet) {
            // Force re-expansion when a different store is selected after the sheet was manually hidden
            bottomSheetState.partialExpand()
        } else {
            bottomSheetState.hide()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LocationUiState.DEFAULT_FALLBACK_LOCATION, 12f)
    }

    var currentCameraTarget by remember { mutableStateOf(cameraPositionState.position.target) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.target }
            .collect { target ->
                currentCameraTarget = target
            }
    }

    LaunchedEffect(bottomSheetState) {
        snapshotFlow { bottomSheetState.currentValue }
            .collect { value ->
                if (value == SheetValue.Hidden && selectedStore != null) {
                    selectedStore = null
                }
            }
    }

    // Update showSearchHereButton when camera moves
    LaunchedEffect(currentCameraTarget) {
        lastSearchedLocation?.let { last ->
            val dist = FloatArray(1)
            android.location.Location.distanceBetween(
                last.latitude, last.longitude,
                currentCameraTarget.latitude, currentCameraTarget.longitude,
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

    LaunchedEffect(storeSearchState.errorMessage) {
        storeSearchState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            cardsViewModel.clearSearchMessage()
        }
    }

    LaunchedEffect(storeSearchState.results.firstOrNull()?.id) {
        val store = storeSearchState.results.firstOrNull() ?: return@LaunchedEffect
        if (appliedSearchStoreId == store.id) return@LaunchedEffect
        appliedSearchStoreId = store.id

        val target = LatLng(store.latitude, store.longitude)
        val update = CameraUpdateFactory.newLatLngZoom(target, 16f)
        cameraPositionState.animate(update)
        handleStoreSelection(store)
        lastSearchedLocation = target
        cardsViewModel.loadStores(
            target.latitude,
            target.longitude,
            categories = selectedCategories.toList().takeIf { it.isNotEmpty() }
        )
        showSearchHereButton = false
    }

    LaunchedEffect(storeSearchState.results.isEmpty()) {
        if (storeSearchState.results.isEmpty()) {
            appliedSearchStoreId = null
        }
    }

    LaunchedEffect(selectedStore?.id) {
        if (selectedStore == null) {
            recommendationViewModel.clearSelection()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            if (selectedStore != null) {
                RecommendationSheetContent(
                    storeName = selectedStore!!.name,
                    uiState = recommendationUiState,
                    onRetry = { recommendationViewModel.retry() },
                    onDiscover = { recommendationViewModel.showDiscoverRecommendations() },
                    onShowOwned = { recommendationViewModel.showOwnedRecommendations() }
                )
            } else {
                // Completely empty content when no marker selected
                Box(modifier = Modifier.height(0.dp))
            }
        },
        sheetPeekHeight = effectivePeek,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) { paddingValues ->
        val scaffoldContentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = 0.dp
        )
        Box(
            modifier = Modifier
                .padding(scaffoldContentPadding)
                .fillMaxSize()
        ) {
            MapSurface(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                mapUiSettings = mapUiSettings,
                contentDescription = mapContentDescription,
                contentPadding = mapContentPadding
            ) {
                val clusterItems = remember(displayedStores) {
                    displayedStores.map { StoreClusterItem(it) }
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

                val userLocation = locationUiState.lastKnownLocation
                val userLocationMarkerState = remember(userLocation) {
                    userLocation?.let { MarkerState(position = it) }
                }

                if (userLocationMarkerState != null) {
                    val userTitle = stringResource(R.string.user_location_marker_title)
                    val userSnippet = stringResource(R.string.user_location_marker_snippet)
                    Marker(
                        state = userLocationMarkerState,
                        title = userTitle,
                        snippet = userSnippet,
                        icon = userLocationMarkerDescriptor,
                        alpha = 1f,
                        zIndex = 1.1f
                    )
                }

                val searchAnchorLocation = lastSearchedLocation
                val shouldShowSearchAnchor = remember(
                    searchAnchorLocation,
                    locationUiState.lastKnownLocation,
                    hasActiveStoreSearch
                ) {
                    if (searchAnchorLocation == null || hasActiveStoreSearch) return@remember false
                    val userLocation = locationUiState.lastKnownLocation
                    if (userLocation == null) return@remember true

                    val dist = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLocation.latitude,
                        userLocation.longitude,
                        searchAnchorLocation.latitude,
                        searchAnchorLocation.longitude,
                        dist
                    )
                    dist[0] > SEARCH_ANCHOR_VISIBILITY_THRESHOLD_METERS
                }

                val searchAnchorMarkerState = remember(searchAnchorLocation) {
                    searchAnchorLocation?.let { MarkerState(position = it) }
                }

                if (shouldShowSearchAnchor && searchAnchorMarkerState != null) {
                    val anchorTitle = stringResource(R.string.search_anchor_marker_title)
                    val anchorSnippet = stringResource(R.string.search_anchor_marker_snippet)
                    Marker(
                        state = searchAnchorMarkerState,
                        title = anchorTitle,
                        snippet = anchorSnippet,
                        icon = searchAnchorMarkerDescriptor,
                        alpha = 0.92f,
                        zIndex = 0.8f
                    )
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
                        handleStoreSelection(item.store)
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
                        handleStoreSelection(store)
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
                isLoading = if (hasActiveStoreSearch) {
                    storeSearchState.isSearching
                } else {
                    storesUiState.isLoading
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = filterRowTop, end = filterRowEnd)
                    .zIndex(1f)
            ) { category ->
                val isSelected = selectedCategories.contains(category)
                if (isSelected) selectedCategories.remove(category) else selectedCategories.add(category)

                val target = currentCameraTarget
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
            val showSearchHere = showSearchHereButton && !hasActiveStoreSearch
            AnimatedVisibility(
                visible = showSearchHere,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = mapTopPadding + 60.dp)
                    .zIndex(1f)
            ) {
                Button(
                    onClick = {
                        val target = currentCameraTarget
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
                isSearching = storeSearchState.isSearching,
                onClearSearch = {
                    cardsViewModel.clearStoreSearch()
                    clusterPreviewStores = emptyList()
                    showSearchHereButton = false
                },
                onQueryChanged = { query ->
                    cardsViewModel.searchStores(query)
                },
                onSearch = { query ->
                    cardsViewModel.searchStores(query)
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
private const val SEARCH_ANCHOR_VISIBILITY_THRESHOLD_METERS = 120f

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
    isSearching: Boolean,
    onClearSearch: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("floatingSearchBar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier.padding(start = 8.dp, end = 4.dp)
            )
            TextField(
                value = text,
                onValueChange = {
                    text = it
                    val trimmed = it.trim()
                    if (trimmed.isEmpty()) {
                        onClearSearch()
                    } else {
                        onQueryChanged(trimmed)
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for a place or store...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val query = text.trim()
                        if (query.isNotEmpty()) {
                            onSearch(query)
                        } else {
                            onClearSearch()
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(18.dp),
                    strokeWidth = 2.dp
                )
            }
            if (text.isNotBlank()) {
                IconButton(onClick = {
                    text = ""
                    onClearSearch()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
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
                            onSettingsClick()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationSheetContent(
    storeName: String,
    uiState: RecommendationUiState,
    onRetry: () -> Unit,
    onDiscover: () -> Unit,
    onShowOwned: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Recommendations for $storeName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        uiState.meta?.let { meta ->
            Text(
                text = buildString {
                    append(if (meta.discover) "Discover mode" else "My cards")
                    append(" • ${meta.total} suggestions")
                    meta.latencyMs?.let { append(" • ${it}ms") }
                    if (meta.cached) append(" • cached")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (uiState.ownedCardIds.isEmpty() && !uiState.discoverMode) {
            Text(
                text = "Add cards in Manage Cards to personalize these results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .testTag("ownedCardsInfo")
            )
        }

        uiState.fallbackBannerMessage?.let { message ->
            RecommendationFallbackBanner(message)
        }

        if (uiState.errorMessage != null && uiState.cards.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (uiState.isStreaming && uiState.cards.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Scoring ${uiState.streamProgress} cards...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (uiState.isLoading && uiState.cards.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        if (uiState.cards.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .testTag("recommendationsList"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.cards, key = { it.cardId }) { card ->
                    RecommendationCardRow(card)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isStreaming || uiState.isLoading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            if (uiState.isStreaming) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Scoring your cards...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    uiState.errorMessage != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "No recommendations yet. Try Discover to explore more cards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RecommendationSheetActions(
            discoverMode = uiState.discoverMode,
            buttonEnabled = uiState.selectedStore != null && !uiState.isLoading && !uiState.discoverInFlight && !uiState.isStreaming && uiState.discoverEnabled,
            onDiscover = onDiscover,
            onShowOwned = onShowOwned
        )
    }
}

@Composable
private fun RecommendationSheetActions(
    discoverMode: Boolean,
    buttonEnabled: Boolean,
    onDiscover: () -> Unit,
    onShowOwned: () -> Unit
) {
    val buttonLabel = if (discoverMode) "Back to my cards" else "Discover cards you don't own"
    val contentDescription = if (discoverMode) "Show saved cards" else "Discover cards you don't own"
    Button(
        onClick = if (discoverMode) onShowOwned else onDiscover,
        enabled = buttonEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("discoverButton")
            .semantics { this.contentDescription = contentDescription }
    ) {
        Text(buttonLabel, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RecommendationFallbackBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("fallbackBanner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendationCardRow(card: RecommendationCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(card.cardName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(card.issuer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ScoreBadge(card.score)
            }

            if (card.normalizedCategories.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    card.normalizedCategories.forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = category.toCategoryLabel(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            ScoreSourceBadge(card.scoreSource)

            card.rationale?.takeIf { it.isNotBlank() }?.let { rationale ->
                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = CircleShape
    ) {
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ScoreSourceBadge(source: RecommendationScoreSource) {
    val (background, content) = rememberScoreSourceColors(source)
    val label = when (source) {
        RecommendationScoreSource.LOCATION -> "Location match"
        RecommendationScoreSource.LLM -> "LLM confidence"
        RecommendationScoreSource.FALLBACK -> "Heuristic"
    }
    Surface(
        color = background,
        contentColor = content,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun rememberScoreSourceColors(source: RecommendationScoreSource): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (source) {
        RecommendationScoreSource.LOCATION -> scheme.surfaceTint.copy(alpha = 0.12f) to scheme.primary
        RecommendationScoreSource.LLM -> scheme.secondaryContainer to scheme.onSecondaryContainer
        RecommendationScoreSource.FALLBACK -> scheme.outlineVariant to scheme.onSurface
    }
}
