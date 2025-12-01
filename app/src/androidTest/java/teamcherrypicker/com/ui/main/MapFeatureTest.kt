package teamcherrypicker.com.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import teamcherrypicker.com.api.ApiService
import teamcherrypicker.com.api.CardBenefitsResponse
import teamcherrypicker.com.api.CardsMetaDto
import teamcherrypicker.com.api.CardsResponse
import teamcherrypicker.com.api.RecommendationMetaDto
import teamcherrypicker.com.api.RecommendationRequestDto
import teamcherrypicker.com.api.RecommendationsResponse
import teamcherrypicker.com.api.ScoreSourcesDto
import teamcherrypicker.com.api.StoresResponse
import teamcherrypicker.com.data.CardsRepository
import teamcherrypicker.com.data.StoreRepository
import teamcherrypicker.com.data.UserLocation
import teamcherrypicker.com.location.LocationPermissionStatus
import teamcherrypicker.com.location.LocationUiState

class MapFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeApiService = object : ApiService {
        override suspend fun sendLocation(location: UserLocation): Response<Unit> {
            return Response.success(Unit)
        }

        override suspend fun getCards(
            limit: Int?,
            offset: Int?,
            category: String?
        ): CardsResponse {
            return CardsResponse(
                data = emptyList(),
                meta = CardsMetaDto(0, 0, 0, null, null)
            )
        }

        override suspend fun getCardBenefits(cardId: Int): CardBenefitsResponse {
            return CardBenefitsResponse(data = emptyList())
        }

        override suspend fun getNearbyStores(
            latitude: Double,
            longitude: Double,
            radius: Int?,
            categories: String?
        ): StoresResponse {
            return StoresResponse(data = emptyList())
        }

        override suspend fun searchStores(query: String, limit: Int?): StoresResponse {
            return StoresResponse(data = emptyList())
        }

        override suspend fun getRecommendations(request: RecommendationRequestDto): RecommendationsResponse {
            return RecommendationsResponse(
                data = emptyList(),
                meta = RecommendationMetaDto(
                    total = 0,
                    limit = request.limit,
                    storeId = request.storeId,
                    discover = request.discover,
                    latencyMs = 0,
                    cached = false,
                    scoreSources = ScoreSourcesDto()
                )
            )
        }
    }

    private val cardsRepository = CardsRepository(fakeApiService)
    private val cardsViewModel = CardsViewModel(cardsRepository, StoreRepository(fakeApiService))

    @Test
    fun permissionGranted_hidesBanner_showsLocationStatus() {
        val locationUiStateFlow = MutableStateFlow(
            LocationUiState(
                permissionStatus = LocationPermissionStatus.Granted,
                isLoading = false,
                lastKnownLocation = LatLng(1.35, 103.87)
            )
        )

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // Verify banner is NOT shown
        composeTestRule.onNodeWithTag("locationPermissionBanner").assertIsNotDisplayed()

        // Verify map is shown
        composeTestRule.onNodeWithTag("map").assertIsDisplayed()
    }

    @Test
    fun permissionDenied_showsBanner() {
        val locationUiStateFlow = MutableStateFlow(
            LocationUiState(
                permissionStatus = LocationPermissionStatus.Denied,
                isLoading = false
            )
        )

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // Verify banner is shown
        composeTestRule.onNodeWithTag("locationPermissionBanner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location permission needed").assertIsDisplayed()
    }

    @Test
    fun searchBar_is_always_visible() {
        val locationUiStateFlow = MutableStateFlow(LocationUiState())

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // Verify search bar is displayed
        composeTestRule.onNodeWithTag("floatingSearchBar").assertIsDisplayed()
    }

    @Test
    fun map_is_displayed_in_fallback_mode() {
        val locationUiStateFlow = MutableStateFlow(
            LocationUiState(
                permissionStatus = LocationPermissionStatus.Unknown,
                isLoading = true
            )
        )

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // Verify map is displayed even when loading/unknown permission
        composeTestRule.onNodeWithTag("map").assertIsDisplayed()
    }

    @Test
    fun recenterFab_is_hidden_initially() {
        val locationUiStateFlow = MutableStateFlow(
            LocationUiState(
                permissionStatus = LocationPermissionStatus.Granted,
                lastKnownLocation = LatLng(1.35, 103.87)
            )
        )

        composeTestRule.setContent {
            MainScreen(
                navController = rememberNavController(),
                isDarkMode = false,
                onToggleDarkMode = {},
                locationUiStateFlow = locationUiStateFlow,
                cardsViewModel = cardsViewModel
            )
        }

        // FAB should be hidden initially (no manual override yet)
        composeTestRule.onNodeWithContentDescription("Recenter map to your location").assertIsNotDisplayed()
    }
}
